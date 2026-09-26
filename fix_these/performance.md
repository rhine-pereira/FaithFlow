# Performance & Smoothness

## 🔴 Bible DB copy and queries on the main thread
- **Where:** `ui/verse/VerseDialogs.kt:203`, `ui/notes/NoteEditor.kt:178`, `data/BibleDatabaseHelper.kt`
- **Problem:** The first call copies the 7.8 MB `bible.db` and then queries SQLite in a click handler. Jank, with ANR risk on low-end devices.
- **Fix:**
  - [ ] Run it in `scope.launch { withContext(Dispatchers.IO) { … } }` or move it into a ViewModel.
  - [ ] Pre-copy the DB at startup on IO.

## 🟠 Note editor lags on long notes
- **Where:** `ui/notes/MarkdownUtils.kt:47-126`, `ui/notes/NoteEditor.kt:102-124,263`
- **Problem:**
  - A new `MarkdownVisualTransformation` is created on every recomposition.
  - Each keystroke runs a case-insensitive regex with ~150 book names over the whole note.
  - `bibleMatches.find{}` runs for every character, which is O(n×m).
  - Reference detection splits the whole text before the cursor on every keystroke.
- **Fix:**
  - [ ] `remember(boldColor) { MarkdownVisualTransformation(...) }`.
  - [ ] Walk the matches with an index pointer and cache the parse result by text.
  - [ ] Find the current line with `lastIndexOf('\n')` and debounce reference detection by ~300 ms.

## 🟠 Daily screen recomposes everything per keystroke
- **Where:** `ui/DailyScreen.kt:67-69,188,195`
- **Problem:** Text state is read at the top level, so the 42-cell `CalendarGrid` recomposes (its `Calendar`, `SimpleDateFormat` and `Map` params are unstable) and two `SimpleDateFormat`s are allocated per frame.
- **Fix:**
  - [ ] Move the text fields into child composables.
  - [ ] `remember` the formatters.
  - [ ] Pass stable/immutable params to `CalendarGrid`.

## 🟠 Tutorial overlay costs a layer forever
- See `ui-correctness.md`: an offscreen `Canvas` stays composed permanently, and every `tutorialTarget` keeps reporting positions.

## 🟡 Other
- [ ] **New Room Flow per recomposition** (`ui/VerseScreen.kt:253`): `remember(noteId) { viewModel.getVersesForNote(noteId) }`.
- [ ] **Notes list rebuild** (`ui/NotesScreen.kt:72-82`): previews and dates for *all* notes are rebuilt on the main thread whenever one changes. Build them in the ViewModel with `.map{}.flowOn(Dispatchers.Default)`.
- [ ] **Shared-text parsing** (`ui/verse/VerseDialogs.kt:385-391`): the regex is compiled every recomposition. `remember(sharedText)` it.
- [ ] **Lifecycle-unaware collection** (`ui/FlowCollectors.kt`, `MainActivity.kt:83`): use `collectAsStateWithLifecycle` so Room flows stop in the background.
- [ ] **Hoist regexes** in `MarkdownUtils.kt` to top-level `val`s.
- [ ] **Dark-mode white flash** after the splash screen: the `res/values/themes.xml` parent is `Theme.Material.Light`. Use a DayNight parent or add `values-night`.
- [ ] **Hidden tabs** (`ui/MainContainer.kt:37-50`) stay composed and hit-testable at `alpha=0`. Consider a `SaveableStateHolder` so only the selected tab is composed.

## ✅ Fine
- All lazy lists and pagers have `key`s.
- R8 full optimization and resource shrinking are on in release.
