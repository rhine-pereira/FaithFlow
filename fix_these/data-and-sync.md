# Data & Sync

## 🔴 Account deletion is unsafe
- **Where:** `ui/AuthViewModel.kt:56`, `data/AuthRepository.kt:67-84`, `MainActivity.kt:121`
- **Problem:**
  - The local DB is wiped *before* the remote delete.
  - Supabase delete errors are swallowed, then `user.delete()` runs anyway, leaving orphaned cloud data.
  - `user.delete()` often throws `FirebaseAuthRecentLoginRequiredException`, leaving the user signed in with an empty local DB.
  - No progress or error is shown to the user.
- **Fix:**
  - [ ] Order: remote table deletes (abort on failure) → reauthenticate if needed → `user.delete()` → `clearAllTables()` → cancel sync.
  - [ ] Delete `verses` before `notes` in case the remote FK doesn't cascade.
  - [ ] Expose a loading/error state and show a snackbar on failure.
  - [ ] Pass `applicationContext`, not the Activity.

## 🔴 DailyRecord unique index is not per-user
- **Where:** `data/DailyRecord.kt` (`Index(value = ["date"], unique = true)`)
- **Problem:** With `REPLACE`, saving a day for user B overwrites user A's record for the same date.
- **Fix:**
  - [ ] Change to `Index(value = ["userId", "date"], unique = true)`.
  - [ ] Bump the DB to v10 with a real migration.

## 🟠 Missing migrations + destructive fallback
- **Where:** `data/AppDatabase.kt`
- **Problem:** Only 4→5 and 6→7 exist; `fallbackToDestructiveMigration()` silently wipes unsynced data on any other upgrade. Released builds are on v9, so this bites on the *next* schema change.
- **Fix:**
  - [ ] Set `exportSchema = true` and commit the schemas.
  - [ ] Write a migration for every future bump.
  - [ ] Limit destructive fallback to `fallbackToDestructiveMigrationFrom(1,2,3,5,7,8)`.

## 🟠 Remote deletions never propagate
- **Where:** `SyncWorker.syncEntities`, `VerseRepository.fetchAndUpsert`
- **Problem:** Remote deletes are hard deletes and fetching only inserts or updates. An item deleted on device 1 stays on device 2, and editing it there brings it back.
- **Fix:**
  - [ ] Soft-delete remotely (upsert with `is_deleted = true`) and apply that on fetch.
  - [ ] Or, on fetch, delete local synced rows whose IDs are missing remotely.

## 🟡 Daily record race condition
- **Where:** `ui/DailyViewModel.kt:83-121`
- **Problem:** Read-modify-write with no lock. Concurrent toggles, custom minutes and debounced text saves overwrite each other.
- **Fix:**
  - [ ] Guard with a `Mutex`, or use targeted DAO `UPDATE … SET col = :v` queries.

## 🟡 SyncWorker retries forever when signed out
- **Where:** `sync/SyncWorker.kt:25`
- **Fix:**
  - [ ] Return `Result.success()` (or `failure()`) when `currentUserId == null`.

## 🟡 Sync error handling
- [ ] One failing row aborts the whole batch and retries everything. Consider per-row try/catch.
- [ ] Pull-to-refresh (`NotesViewModel.syncFromCloud`) returns immediately, so errors are never shown. Make it `suspend` and expose `isRefreshing` and the error.
- [ ] `fetchFromSupabase` swallows all errors, so the user can't tell that sync is broken.

## 🟡 bible.db asset
- **Where:** `data/BibleDatabaseHelper.kt`
- [ ] It's copied only if missing, so a future updated `bible.db` in assets will never replace the old copy. Version it (e.g. `bible_v2.db`) or compare a stored version.
- [ ] Pre-copy it on a background thread at startup.

## 🟢 Dead code
- [ ] `PersonalNoteRepository.syncFromCloud` is unused. Delete it.
