# Security

## 🔴 BLOCKER: Supabase has no per-user authorization
- **Where:** `data/SupabaseClient.kt`, `data/VerseRepository.kt`, `sync/SyncWorker.kt`, `data/AuthRepository.kt`
- **Problem:** The client sends only the anon key; the Firebase ID token is never passed. User isolation relies on client-side `eq("user_id", …)` filters, so RLS must be off or permissive for sync to work. Anyone who extracts the key from the APK can read, modify or delete every user's notes. `SyncWorker` deletes by `id` alone.
- **Verify first:** in the Supabase dashboard, confirm the shipped key is `anon` (not `service_role`) and check RLS on `notes`, `verses`, `personal_notes`, `personal_note_categories`, `daily_records`.
- **Fix:**
  - [ ] Supabase → Authentication → Third-party auth → add Firebase project.
  - [ ] Pass the token in `createSupabaseClient`:
    ```kotlin
    accessToken = { FirebaseAuth.getInstance().currentUser?.getIdToken(false)?.await()?.token }
    ```
  - [ ] Enable RLS on all 5 tables with policies `using (user_id = auth.jwt()->>'sub') with check (user_id = auth.jwt()->>'sub')`.
  - [ ] Test with two accounts that neither can read or delete the other's rows.

## 🟠 Cross-account data leak on shared devices
- **Where:** `AuthRepository.signOut()`, `VerseDao.getUnsynced*`, `SyncWorker`
- **Problem:** Sign-out doesn't clear Room or cancel sync. The unsynced queries don't filter by user, and `SyncWorker` overwrites `userId` with the current user, so user A's unsynced data uploads into user B's account after B signs in.
- **Fix:**
  - [ ] On sign-out: `AppDatabase.clearAllTables()` + `WorkManager.cancelUniqueWork(SyncScheduler.SYNC_WORK_NAME)`.
  - [ ] Filter unsynced queries by `userId`, and stop rewriting `userId` in `withUserAndSynced` (reject rows that belong to a different user).

## 🟡 Minor
- [ ] `printStackTrace()` calls are not stripped by R8 (only `Log.*` is). Replace them with `Log.e` or Crashlytics `recordException`. Files: `AuthRepository`, `AuthViewModel`, `SyncWorker`, `VerseRepository`, `PersonalNoteRepository`, `BibleDatabaseHelper`.
- [ ] `LoginScreen.kt` logs the credential type. Harmless in release (stripped), but remove it.
- [ ] `AD_ID` permission is probably merged in by Firebase Analytics. Remove it via `tools:node="remove"` in the manifest, or disclose it (see `legal.md`).

## ✅ Already fine
- Secrets, `google-services.json` and client secrets are gitignored and absent from history.
- The only permission is INTERNET, and backup/device transfer exclude the DB and prefs.
- Release builds use R8, minify and resource shrinking; CI signs the build.
- SQL uses bound parameters.
