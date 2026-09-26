# UX, Visual & Accessibility

## 🟠 TalkBack reads all tabs at once
- **Where:** `ui/MainContainer.kt:37-50`
- **Problem:** Unselected tabs are hidden with `graphicsLayer { alpha = 0 }` only; their semantics stay active and taps can reach them.
- **Fix:**
  - [ ] Add `Modifier.semantics { invisibleToUser() }` / `clearAndSetSemantics {}` to hidden tabs.
  - [ ] Or compose only the selected tab.

## 🟠 Double insets / edge-to-edge problems
- [ ] **Nested Scaffold** (`ui/VerseScreen.kt:88`): it pads for the system bars twice. Set `contentWindowInsets = WindowInsets(0)` and `TopAppBar(windowInsets = WindowInsets(0))`.
- [ ] **Editor toolbar** (`ui/notes/NoteEditor.kt:121-125,343`): sits under the nav bar when the keyboard is closed. Use `windowInsetsPadding(WindowInsets.ime.union(WindowInsets.navigationBars))`.
- [ ] **AddVerseDialog** (`ui/verse/VerseDialogs.kt:120`): doesn't scroll, so it gets clipped with the keyboard open. Add `verticalScroll(rememberScrollState())`.

## 🟠 Contrast failures
- [ ] **Light theme** (`ui/theme/Theme.kt:26-36`): `primary = #D4AF37` with a white `onPrimary` is ~2.1:1. Use a darker gold (~`#8B6F1E`) or a black `onPrimary`.
- [ ] **Bible-reference colour** (`ui/notes/MarkdownUtils.kt:19`): `#FFD700` is nearly invisible on light backgrounds.
- [ ] **Calendar cells** (`ui/DailyScreen.kt:40-42,534,647`): white on `#FBC02D` fails, and the switch thumb is hardcoded to `Color.White`. Derive colours from `MaterialTheme.colorScheme`.
- [ ] **Dynamic colour** (`Theme.kt:42`): `dynamicColor = true` replaces the brand gold on Android 12+ while the hardcoded golds remain. Default to `false`.

## 🟠 No localization
- **Where:** all of `ui/`; `res/values/strings.xml` contains only `app_name`.
- **Fix:**
  - [ ] Move every user-facing string into `strings.xml` and use `stringResource`.

## 🟡 Missing feedback / states
- [ ] Account deletion: no progress or error UI (see `data-and-sync.md`).
- [ ] A theme with no verses shows a blank screen (`ui/VerseScreen.kt:257-269`). Add an empty state.
- [ ] Pull-to-refresh spinner just blinks and never shows errors (`ui/NotesScreen.kt:94-100`).

## 🟡 Accessibility details
- [ ] **Calendar cells** (`ui/DailyScreen.kt:487-528`): no label, and status is shown by colour only. Add `semantics { contentDescription = "March 12, completed"; role = Role.Button }`.
- [ ] **Radio labels** (`ui/verse/VerseDialogs.kt:412,448`): make the whole row `Modifier.selectable(role = Role.RadioButton)`.
- [ ] **Weekday headers** (`DailyScreen.kt:463`): hardcoded Sunday-first "S M T W…". Use `DateFormatSymbols` and `Calendar.firstDayOfWeek`.
- [ ] **Card ripple** (`ui/NotesScreen.kt:381-388`): not clipped to the card shape. Use `Card(onClick = …)`.
- [ ] **Deprecated M2 `pullRefresh`**: migrate to M3 `PullToRefreshBox`.

## 🟢 Cleanup
- [ ] Delete the empty `ui/CalendarScreen.kt`.
- [ ] Delete the template Purple/Pink colours in `ui/theme/Color.kt` and the purple/teal in `res/values/colors.xml`.
- [ ] Remove the unused `screenHeight`, magic `1000f` and `onFinished` in `TutorialOverlay.kt`.
- [ ] Remove the redundant `addNoteWithInitialVerse` wrapper in `VerseViewModel.kt:80`.
- [ ] Remove the unused imports in `AuthViewModel.kt`.
- [ ] Lint `MonochromeLauncherIcon`: add a `<monochrome>` layer for themed icons.

## ✅ Fine
- Icon buttons have `contentDescription`s.
- Destructive actions have confirmation dialogs.
- M3 icon buttons get 48dp touch targets.
