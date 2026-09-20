# Graph Report - .  (2026-05-20)

## Corpus Check
- 57 files · ~55,544 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 307 nodes · 315 edges · 37 communities detected
- Extraction: 86% EXTRACTED · 13% INFERRED · 1% AMBIGUOUS · INFERRED: 42 edges (avg confidence: 0.89)
- Token cost: 0 input · 0 output

## Community Hubs (Navigation)
- [[_COMMUNITY_VerseDao Data Access|VerseDao Data Access]]
- [[_COMMUNITY_Docs & Product Overview|Docs & Product Overview]]
- [[_COMMUNITY_UI Design Mockups|UI Design Mockups]]
- [[_COMMUNITY_Daily Walk Tracker UI|Daily Walk Tracker UI]]
- [[_COMMUNITY_Personal Note Repository|Personal Note Repository]]
- [[_COMMUNITY_Notes Screen Composables|Notes Screen Composables]]
- [[_COMMUNITY_Verse ViewModel|Verse ViewModel]]
- [[_COMMUNITY_Verse Repository|Verse Repository]]
- [[_COMMUNITY_MainActivity & Updates|MainActivity & Updates]]
- [[_COMMUNITY_Notes ViewModel|Notes ViewModel]]
- [[_COMMUNITY_Verse Screen UI|Verse Screen UI]]
- [[_COMMUNITY_In-App Updates|In-App Updates]]
- [[_COMMUNITY_Auth Repository|Auth Repository]]
- [[_COMMUNITY_Tutorial Overlay|Tutorial Overlay]]
- [[_COMMUNITY_Auth ViewModel|Auth ViewModel]]
- [[_COMMUNITY_Daily ViewModel|Daily ViewModel]]
- [[_COMMUNITY_Bible Database Helper|Bible Database Helper]]
- [[_COMMUNITY_Daily Screen UI|Daily Screen UI]]
- [[_COMMUNITY_Main Navigation Container|Main Navigation Container]]
- [[_COMMUNITY_Room App Database|Room App Database]]
- [[_COMMUNITY_Verse Data Models|Verse Data Models]]
- [[_COMMUNITY_Launcher Icon Branding|Launcher Icon Branding]]
- [[_COMMUNITY_Instrumented Tests|Instrumented Tests]]
- [[_COMMUNITY_Personal Note Models|Personal Note Models]]
- [[_COMMUNITY_Sync Worker|Sync Worker]]
- [[_COMMUNITY_Login Screen|Login Screen]]
- [[_COMMUNITY_Unit Tests|Unit Tests]]
- [[_COMMUNITY_Bible Static Data|Bible Static Data]]
- [[_COMMUNITY_Daily Record Entity|Daily Record Entity]]
- [[_COMMUNITY_Supabase Client|Supabase Client]]
- [[_COMMUNITY_Compose Theme|Compose Theme]]
- [[_COMMUNITY_Root Gradle Build|Root Gradle Build]]
- [[_COMMUNITY_Settings Gradle|Settings Gradle]]
- [[_COMMUNITY_App Gradle Build|App Gradle Build]]
- [[_COMMUNITY_Calendar Screen|Calendar Screen]]
- [[_COMMUNITY_Theme Colors|Theme Colors]]
- [[_COMMUNITY_Typography Theme|Typography Theme]]

## God Nodes (most connected - your core abstractions)
1. `VerseDao` - 34 edges
2. `PersonalNoteRepository` - 13 edges
3. `VerseViewModel` - 13 edges
4. `VerseRepository` - 12 edges
5. `MainActivity` - 11 edges
6. `NotesViewModel` - 11 edges
7. `Refined Daily Tracker Screen` - 10 edges
8. `FaithFlow` - 9 edges
9. `Notes Dashboard Screen` - 8 edges
10. `DailyViewModel` - 7 edges

## Surprising Connections (you probably didn't know these)
- `Google Sign-In & Firebase Auth Data` --semantically_similar_to--> `Google Sign-In Authentication`  [INFERRED] [semantically similar]
  docs/privacy_policy.html → README.md
- `Google Sign-In Account Terms` --semantically_similar_to--> `Google Sign-In Authentication`  [INFERRED] [semantically similar]
  docs/terms_and_conditions.html → README.md
- `In-App Account Deletion` --semantically_similar_to--> `Full Account Deletion`  [INFERRED] [semantically similar]
  docs/data_deletion.html → README.md
- `Seal Today's Walk Button` --semantically_similar_to--> `Seal Today's Walk Action`  [INFERRED] [semantically similar]
  designs/Refined_Daily_Tracker.html → README.md
- `Supabase User Content Storage` --semantically_similar_to--> `Supabase Postgrest Sync`  [INFERRED] [semantically similar]
  docs/privacy_policy.html → README.md

## Hyperedges (group relationships)
- **FaithFlow Core User Features** — readme_verse_themes, readme_personal_notes, readme_daily_walk_tracker, readme_seal_todays_walk [EXTRACTED 1.00]
- **Cloud Identity and Storage Stack** — readme_google_sign_in_auth, privacy_policy_google_firebase_auth, readme_supabase_sync, privacy_policy_supabase_storage [INFERRED 0.85]
- **In-App Update Control Pipeline** — plan_mainactivity_entry_point, plan_app_update_manager, plan_remote_config_version_gates, plan_immediate_update_flow, plan_flexible_update_flow, plan_onresume_recheck [EXTRACTED 1.00]
- **Shared spiritual category tabs across Notes and Themes designs** — notes_dashboard_category_tabs, verse_themes_category_tabs, notes_dashboard_personal_tab [EXTRACTED]
- **Consistent dark mode serif headings and gold accent CTAs** — notes_dashboard_dark_gold_theme, refined_daily_tracker_screen, verse_themes_screen [INFERRED]
- **Two tab Notes Themes bottom nav in notes and themes mockups** — notes_dashboard_bottom_nav, verse_themes_bottom_nav, faithflow_main_container_nav [INFERRED]
- **FaithFlow Brand Icon Identity** — ic_launcher_playstore_faithflow_launcher_icon, ic_launcher_playstore_icon_theme, ic_launcher_playstore_brand_colors, ic_launcher_playstore_christian_symbolism [EXTRACTED 1.00]

## Communities

### Community 0 - "VerseDao Data Access"
Cohesion: 0.06
Nodes (1): VerseDao

### Community 1 - "Docs & Product Overview"
Cohesion: 0.09
Nodes (30): Email Data Deletion Request, In-App Account Deletion, Data Deletion Request Page, Right to Request Data Deletion, Google Sign-In & Firebase Auth Data, Privacy Policy, Supabase User Content Storage, Full Account Deletion (+22 more)

### Community 2 - "UI Design Mockups"
Cohesion: 0.13
Nodes (22): FaithFlow Android App, MainContainer Three Tab Nav, Personal Notes Workspace Feature, Verse Themes Feature, Notes Themes Bottom Navigation, Category Tab Navigation, Add Note Floating Action Button, FaithFlow Brand Header (+14 more)

### Community 3 - "Daily Walk Tracker UI"
Cohesion: 0.15
Nodes (20): DailyRecord Room Entity, Daily Walk Tracker Feature, Dark Mode Gold Accent Theme, Daily Walk Tracker, Seal Today's Walk Action, Bible Reading Tracker Section, Five Item Bottom Navigation, Tuesday October 24 Date Header (+12 more)

### Community 4 - "Personal Note Repository"
Cohesion: 0.14
Nodes (1): PersonalNoteRepository

### Community 5 - "Notes Screen Composables"
Cohesion: 0.14
Nodes (2): BibleRef, MarkdownVisualTransformation

### Community 6 - "Verse ViewModel"
Cohesion: 0.14
Nodes (1): VerseViewModel

### Community 7 - "Verse Repository"
Cohesion: 0.15
Nodes (1): VerseRepository

### Community 8 - "MainActivity & Updates"
Cohesion: 0.17
Nodes (1): MainActivity

### Community 9 - "Notes ViewModel"
Cohesion: 0.17
Nodes (1): NotesViewModel

### Community 10 - "Verse Screen UI"
Cohesion: 0.18
Nodes (0): 

### Community 11 - "In-App Updates"
Cohesion: 0.24
Nodes (11): AppUpdateManager (Play Core), Remote Config Fetch Interval Zero (Testing), Flexible Update Flow, Immediate Update Flow, In-App Update + Remote Config Feature, MainActivity Entry Point, onResume Update Re-Check, Remote Config Version Gates (+3 more)

### Community 12 - "Auth Repository"
Cohesion: 0.2
Nodes (5): Authenticated, AuthRepository, AuthStatus, Loading, Unauthenticated

### Community 13 - "Tutorial Overlay"
Cohesion: 0.2
Nodes (3): TutorialContent, TutorialState, TutorialStep

### Community 14 - "Auth ViewModel"
Cohesion: 0.22
Nodes (5): AuthState, AuthViewModel, Loading, SignedIn, SignedOut

### Community 15 - "Daily ViewModel"
Cohesion: 0.25
Nodes (1): DailyViewModel

### Community 16 - "Bible Database Helper"
Cohesion: 0.29
Nodes (1): BibleDatabaseHelper

### Community 17 - "Daily Screen UI"
Cohesion: 0.29
Nodes (0): 

### Community 18 - "Main Navigation Container"
Cohesion: 0.33
Nodes (4): Daily, PersonalNotes, Screen, Themes

### Community 19 - "Room App Database"
Cohesion: 0.4
Nodes (1): AppDatabase

### Community 20 - "Verse Data Models"
Cohesion: 0.5
Nodes (3): Note, NoteWithVerses, Verse

### Community 21 - "Launcher Icon Branding"
Cohesion: 0.83
Nodes (4): Brand Colors: Deep Royal Blue, Gold, White Glow, Light Blue Cyan, Christian Symbolism: Open Bible, Ornate Cross, Flowing Living Water, FaithFlow Play Store Launcher Icon, Spiritual Icon Theme: Faith, Scripture, and Living Water Flow

### Community 22 - "Instrumented Tests"
Cohesion: 0.67
Nodes (1): ExampleInstrumentedTest

### Community 23 - "Personal Note Models"
Cohesion: 0.67
Nodes (2): PersonalNote, PersonalNoteCategory

### Community 24 - "Sync Worker"
Cohesion: 0.67
Nodes (1): SyncWorker

### Community 25 - "Login Screen"
Cohesion: 0.67
Nodes (0): 

### Community 26 - "Unit Tests"
Cohesion: 0.67
Nodes (1): ExampleUnitTest

### Community 27 - "Bible Static Data"
Cohesion: 1.0
Nodes (1): BibleData

### Community 28 - "Daily Record Entity"
Cohesion: 1.0
Nodes (1): DailyRecord

### Community 29 - "Supabase Client"
Cohesion: 1.0
Nodes (1): SupabaseConfig

### Community 30 - "Compose Theme"
Cohesion: 1.0
Nodes (0): 

### Community 31 - "Root Gradle Build"
Cohesion: 1.0
Nodes (0): 

### Community 32 - "Settings Gradle"
Cohesion: 1.0
Nodes (0): 

### Community 33 - "App Gradle Build"
Cohesion: 1.0
Nodes (0): 

### Community 34 - "Calendar Screen"
Cohesion: 1.0
Nodes (0): 

### Community 35 - "Theme Colors"
Cohesion: 1.0
Nodes (0): 

### Community 36 - "Typography Theme"
Cohesion: 1.0
Nodes (0): 

## Ambiguous Edges - Review These
- `Auth-Gated App Flow` → `MainActivity Entry Point`  [AMBIGUOUS]
  plans/in app update/plan.md · relation: conceptually_related_to
- `Five Item Bottom Navigation` → `MainContainer Three Tab Nav`  [AMBIGUOUS]
  designs/Refined_Daily_Tracker.png · relation: semantically_similar_to

## Knowledge Gaps
- **40 isolated node(s):** `AuthStatus`, `Loading`, `Unauthenticated`, `Authenticated`, `BibleData` (+35 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **Thin community `Bible Static Data`** (2 nodes): `BibleData.kt`, `BibleData`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Daily Record Entity`** (2 nodes): `DailyRecord.kt`, `DailyRecord`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Supabase Client`** (2 nodes): `SupabaseClient.kt`, `SupabaseConfig`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Compose Theme`** (2 nodes): `Theme.kt`, `FaithFlowTheme()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Root Gradle Build`** (1 nodes): `build.gradle.kts`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Settings Gradle`** (1 nodes): `settings.gradle.kts`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `App Gradle Build`** (1 nodes): `build.gradle.kts`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Calendar Screen`** (1 nodes): `CalendarScreen.kt`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Theme Colors`** (1 nodes): `Color.kt`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Typography Theme`** (1 nodes): `Type.kt`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **What is the exact relationship between `Auth-Gated App Flow` and `MainActivity Entry Point`?**
  _Edge tagged AMBIGUOUS (relation: conceptually_related_to) - confidence is low._
- **What is the exact relationship between `Five Item Bottom Navigation` and `MainContainer Three Tab Nav`?**
  _Edge tagged AMBIGUOUS (relation: semantically_similar_to) - confidence is low._
- **Why does `Daily Walk Tracker` connect `Daily Walk Tracker UI` to `Docs & Product Overview`?**
  _High betweenness centrality (0.035) - this node is a cross-community bridge._
- **Why does `Refined Daily Tracker Screen` connect `Daily Walk Tracker UI` to `UI Design Mockups`?**
  _High betweenness centrality (0.034) - this node is a cross-community bridge._
- **What connects `AuthStatus`, `Loading`, `Unauthenticated` to the rest of the system?**
  _40 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `VerseDao Data Access` be split into smaller, more focused modules?**
  _Cohesion score 0.06 - nodes in this community are weakly interconnected._
- **Should `Docs & Product Overview` be split into smaller, more focused modules?**
  _Cohesion score 0.09 - nodes in this community are weakly interconnected._