# Legal & Play Compliance

The canonical legal pages are `docs/*.html` on GitHub Pages, because the APK hardcodes `https://rhine-pereira.github.io/FaithFlow/*.html` (`app/build.gradle.kts:71-73`).

## 🔴 Legal pages may not be deploying
- **Where:** `.github/workflows/pages.yml:5-6`
- **Problem:** It triggers on `main`, but the repo branch is `master`, so doc changes never auto-deploy.
- **Fix:**
  - [ ] Change the trigger to `master`.
  - [ ] Confirm Pages source = GitHub Actions.
  - [ ] Check that all 3 URLs return 200.

## 🔴 False claim on the data-deletion page
- **Where:** `docs/data_deletion.html:54`, `website/app/data-deletion/page.tsx:82`
- **Problem:** It says deletion purges "diagnostic or usage data linked to your identity". Analytics and Crashlytics data are **not** deleted.
- **Fix:**
  - [ ] Remove the claim, or state the retention periods (Analytics 2/14 months as configured, Crashlytics 90 days).
- Also: the "permanently erased" promise is currently false because of the account deletion bug (see `data-and-sync.md`).

## 🟠 Privacy policy missing disclosures
- **Where:** `docs/privacy_policy.html`, `website/app/privacy/page.tsx`
- [ ] Firebase Remote Config (Firebase Installation ID).
- [ ] Google Play In-App Updates.
- [ ] Analytics is pseudonymous, not "anonymized" (app-instance ID, device info, IP-derived location, `login_success` / `account_deleted` events).
- [ ] Advertising ID: remove `com.google.android.gms.permission.AD_ID` with `tools:node="remove"`, or disclose it.
- [ ] Retention periods, GDPR/legal basis and user rights, children's policy, international transfers, and links to the Google and Supabase privacy policies.
- [ ] Make the **Play Console Data Safety form** match exactly.

## 🟠 Terms template leftovers
- **Where:** `docs/terms_and_conditions.html`, `website/app/terms/page.tsx`
- [ ] "laws of both the Country and foreign countries" (line 50 / 86) is an unfilled placeholder. Add a real governing-law jurisdiction.
- [ ] "monitor and edit all Content" (line 38 / 56) contradicts the private-journal positioning. Remove it.
- [ ] Add a minimum-age clause.

## 🟡 Duplicate legal copy
- `docs/*.html` and `website/app/{privacy,terms,data-deletion}` are identical today but will drift.
- [ ] Keep a single source: have the website link or redirect to github.io, or generate `docs/` from the website.
