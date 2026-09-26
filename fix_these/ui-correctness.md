# UI Correctness Bugs

## 🔴 Daily screen saves text to the wrong day
- **Where:** `ui/DailyScreen.kt:112-124`, `ui/DailyViewModel.kt:92`
- **Problem:** The debounced saves (500/800 ms) read `_targetDate.value` when they fire. Type, then change the date within the delay, and the text is written into the new day. If the new day has no record, the reset cancels the save and the text is lost.
- **Fix:**
  - [ ] Capture the date at edit time and pass it to `updateDailyRecord(date = …)`.
  - [ ] Or flush the pending save before the date changes.

## 🔴 Daily screen shows the previous day's text
- **Where:** `ui/DailyScreen.kt:94-99`
- **Problem:** `whatRead` and `prayerTime` only reset when `!record.readToday || whatRead.isEmpty()`. Moving to a day with `readToday=true` keeps the old text, and the next edit saves it there.
- **Fix:**
  - [ ] Always reset the local fields when `targetDate` / `currentRecord?.id` changes.

## 🔴 Onboarding tutorial never works
- **Where:** `ui/TutorialOverlay.kt`, `MainActivity.kt:103`
- **Problem:** The first run starts at `ADD_THEME_FAB`, but nothing registers that `tutorialTarget`. So no tooltip appears, `onboarding_complete` is never set, and a full-screen offscreen `Canvas` stays composed forever.
- **Fix:**
  - [ ] Add `.tutorialTarget(TutorialStep.ADD_THEME_FAB)` to the VerseScreen FAB, or start at `ADD_CATEGORY_BTN`.
  - [ ] Don't compose the overlay when `targetRect == null`.

## 🔴 Empty notes created on back press
- **Where:** `ui/notes/NoteEditor.kt:126-131`, `ui/NotesScreen.kt:246`
- **Problem:** Back always calls `onSave`: FAB → back inserts an empty note, and every close re-saves and re-syncs unchanged notes.
- **Fix:**
  - [ ] In `dismissAndSave`, skip if unchanged.
  - [ ] Never save a new note with a blank title and content.

## 🟠 Developer error text shown to users
- **Where:** `ui/LoginScreen.kt:78,84,89`
- **Problem:** Messages like "Check SHA-1 and Client ID" and raw `e.localizedMessage` reach users. Cancelling the picker shows an error.
- **Fix:**
  - [ ] Show friendly strings.
  - [ ] Ignore `GetCredentialCancellationException`.
  - [ ] Handle `NoCredentialException` (lint `CredentialManagerMisuse`).

## 🟠 State lost on rotation / process death
- **Where:** `ui/NotesScreen.kt:83,120`, `ui/VerseScreen.kt:56`, `ui/notes/NoteUndoRedoManager.kt:199`
- **Problem:** `noteToEdit` and `selectedNoteWithVerses` use plain `remember`. Rotation closes the editor, drops the last ~500 ms of edits and loses undo history.
- **Fix:**
  - [ ] `rememberSaveable` the IDs and reload.
  - [ ] Flush the save in `DisposableEffect.onDispose`.

## 🟡 Other bugs
- [ ] **Drag-reorder jump** (`ui/VerseScreen.kt:199-242`): after a swap, subtract the step from `dragOffset` too, not just `accumulatedDrag`.
- [ ] **Stale "today"** (`DailyScreen.kt:76`, `DailyViewModel.kt:19`): past midnight, the forward arrow and today's highlight are wrong. Recompute on `ON_RESUME`.
- [ ] **Crash risk** (`ui/notes/MarkdownUtils.kt:133`): `text[selection.start - 1]` needs a `selection.start > 0` guard.
- [ ] **Checklist continuation** (`MarkdownUtils.kt:155-171`): the bullet regex matches `- [ ] ` first. Check the checklist regex first.
- [ ] **Seal badge** (`DailyScreen.kt:228,398`): says "Sealed for Today" on past dates, and sealing doesn't lock editing. Decide the intended behaviour.
- [ ] **Blank-input confirm buttons** (`VerseDialogs.kt:236`, `NotesDialogs.kt:87`, `RenameDialog.kt:60`): set `enabled = name.isNotBlank()`.
- [ ] **`catch (e: Exception)` swallows `CancellationException`** (`AuthViewModel.kt:50`). Rethrow it.
