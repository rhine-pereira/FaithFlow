# Release & Build

Current state: 16/16 unit tests pass; `lintDebug` reports 0 errors and 44 warnings.

## 🔴 Must do before upload
- [ ] **Bump `versionCode`** (`app/build.gradle.kts`): it's still `7`, the same as the shipped v1.1.0, so Play will reject the upload. Bump `versionName` too.
- [ ] **Local release signing:** `local.properties` has no `RELEASE_*` keys, so local `bundleRelease` can't sign. That's fine if you only release via CI (`.github/workflows/release.yml`); otherwise add them.
- [ ] **Test the minified release build on a device:** sign-in, sync, fetch, delete account. R8 plus kotlinx-serialization and Supabase are the likely breakage points.

## 🟠 Repo hygiene
- [ ] Remove `app/release/app-release.aab` (12 MB) from git and add `app/release/` to `.gitignore`.
- [ ] Decide whether `graphify-out/` (cache and chunk JSON) belongs in the repo.
- [ ] The release workflow uses unquoted `echo $SECRET | base64 --decode`. Quote the variables (`"$VAR"`).

## 🟡 Dependencies (lint `GradleDependency` ×20, `NewerVersionAvailable` ×6)
- [ ] Compose BOM `2024.09.00` is old; update it and retest.
- [ ] Kotlin `2.0.21` / KSP `2.0.21-1.0.27`: update together.
- [ ] Room `2.6.1` → 2.7.x; Firebase BOM `33.9.0` → latest; Supabase `3.0.1` / Ktor `3.0.1` → latest 3.x.
- [ ] Drop `firebase-auth-ktx` (the KTX modules are merged into the main artifacts in newer BOMs) and migrate the `com.google.firebase.ktx.*` imports.
- [ ] The `secrets-gradle-plugin` is applied but values are read manually. Keep one mechanism.

## 🟡 ProGuard
- **Where:** `app/proguard-rules.pro`
- [ ] Blanket `-keep class io.ktor.** { *; }`, `com.google.firebase.** { *; }` and `io.github.jan_tennert.supabase.**` defeat shrinking. Also, the Supabase package is `io.github.jan.supabase`, so that rule matches nothing. Rely on the libraries' consumer rules plus keeping your `@Serializable` data classes.
- [ ] Add `-assumenosideeffects` for `Throwable.printStackTrace()`, or replace those calls.

## 🟡 Tests
- [ ] Only pure-logic tests exist. Add at least:
  - a sync test (SyncWorker with a fake client), covering sign-out / account switch;
  - a Room migration test once `exportSchema = true`;
  - a Daily-screen save-date test.
- [ ] Delete the placeholder `ExampleUnitTest` and `ExampleInstrumentedTest`.

## 🟢 Nice to have
- [ ] Baseline Profile (`androidx.profileinstaller` plus a macrobenchmark module) for faster cold start and smoother first scrolls.
- [ ] Enable Crashlytics `recordException` in the catch blocks that currently swallow errors.
