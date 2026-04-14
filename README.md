# FaithFlow

FaithFlow is a local-first Android app for tracking your scripture life: verse themes, personal notes, and daily walk records, with cloud sync and offline support.

## 🚀 Features

### 🔐 Authentication & account
- Google Sign-In via Credential Manager + Firebase Auth.
- Auth-gated app flow (signed-out login screen vs signed-in app shell).
- Sign out and full account deletion (clears local data and remote user data).

### 📚 Verse themes
- Create, rename, reorder (drag-and-drop), and delete verse themes.
- Add, edit, and delete verses inside each theme.
- Theme cards show recent verse previews.
- Soft-delete + sync-safe IDs (`UUID`) to avoid cross-device conflicts.

### 📝 Verse input and import
- Manual verse entry with searchable Catholic book suggestions (including deuterocanonical books).
- Optional in-app verse fetch from local Bible database (`bible.db`) for chapter/verse ranges.
- Android Share Sheet import (`ACTION_SEND` for plain text):
  - Parses reference + content.
  - Removes trailing Bible-version tags (e.g. `RSV-C`, `NIV`).
  - Strips URL lines.
  - Lets you import into an existing theme or create a new one.

### 📒 Personal notes workspace
- Category-based notes (create, rename, reorder, delete categories).
- Keep-style note grid with full-screen editor.
- Rich-text style markers (bold, italic, numbered lists) with live rendering.
- Smart Bible reference detection inside note editor with one-tap verse insertion + undo.
- Pull-to-refresh sync from cloud.

### 📅 Daily walk tracker
- Calendar-driven daily records with visual day status.
- Track Bible reading, what was read, prayer activity, prayer duration, and prophetic insights.
- Future dates are view-only/locked.
- “Seal Today’s Walk” action marks the day as sealed and triggers sync.

### 🔄 Sync, offline, and reliability
- Room as the local source of truth.
- Supabase Postgrest sync for themes, verses, personal note categories/notes, and sealed daily records.
- WorkManager one-time background sync with network constraints and exponential backoff.
- Local-first behavior: data remains usable offline and syncs when connectivity returns.

### 🎯 Onboarding & updates
- In-app tutorial overlay for key actions on first run.
- Play Core in-app updates (flexible + immediate modes) controlled by Firebase Remote Config.

## 🛠 Technical Details

### Stack
- Kotlin + Jetpack Compose (Material 3)
- Room
- Supabase (`supabase-kt` Postgrest + Ktor Android client)
- Firebase Auth, Firebase Remote Config
- Android Credential Manager + Google Identity
- WorkManager
- Kotlin Coroutines / Flow
- Kotlinx Serialization

### Architecture highlights
- Repository + ViewModel layering with `StateFlow`.
- Soft-delete sync model (`isDeleted` + `isSynced`) to safely handle remote deletes.
- Auth-scoped queries for per-user local and remote data separation.

## 📦 Setup & Installation

1. Configure `local.properties` in the project root:
   ```properties
   SUPABASE_URL=your_supabase_url
   SUPABASE_KEY=your_supabase_anon_key
   GOOGLE_CLIENT_ID=your_google_web_client_id
   ```
2. Add Firebase config (`app/google-services.json`) for your project.
3. Ensure Supabase tables exist for:
   - `notes`
   - `verses`
   - `personal_note_categories`
   - `personal_notes`
   - `daily_records`
4. Environment:
   - Minimum SDK: 24
   - Target/Compile SDK: 36
   - Kotlin: 2.0.21
   - Gradle: 8.13.2

---
Developed as a Catholic-friendly tool for scripture study, prayer, and reflection.
