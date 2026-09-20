# Play Store Rollout Guide

Follow this guide to release FaithFlow to the Google Play Store.

## 1. Play Console Setup
1. **Create App**: Create a new app in the Google Play Console.
2. **Internal Testing**: Upload your first AAB to the "Internal testing" track.
3. **App Signing**: Enroll in Play App Signing.
4. **Store Listing**:
   - Copy descriptions from `store-listing/short_description.txt` and `full_description.txt`.
   - Upload screenshots and the feature graphic as described in `store-listing/SCREENSHOTS.md`.
   - Use `app/src/main/ic_launcher-playstore.png` for the app icon.

## 2. Legal & Compliance
1. **Privacy Policy**: Set the URL to `https://rhine-pereira.github.io/FaithFlow/privacy_policy.html`.
2. **Data Safety**: Complete the Data Safety form. Declare:
   - **Email/Name**: Collected for account management (Google Auth).
   - **User Content**: Saved verses, notes, and records.
   - **App Interactions**: Anonymized usage via Firebase Analytics.
   - **Crash Logs**: Diagnostic data via Firebase Crashlytics.
   - **Encryption**: Data is encrypted in transit (HTTPS).
   - **Deletion**: Users can delete data in-app or via the web request page.
3. **Content Rating**: Complete the IARC questionnaire.

## 3. Firebase & Google Cloud
1. **SHA Fingerprints**:
   - Get the SHA-1 and SHA-256 fingerprints of your **release keystore**.
   - Add them to your Firebase project settings.
   - Add them to the Google Cloud Console OAuth 2.0 Client ID for Android.
2. **Remote Config**:
   - Set `latest_version_code` to the current `versionCode`.
   - Set `min_supported_version_code` if an update is mandatory.
   - Set `force_update` to `true` if you want to block older versions.

## 4. Rollout Strategy
1. **Internal Testing**: Verify with a small group of testers.
2. **Closed Testing**: Expand to more testers.
3. **Production**:
   - Start with a staged rollout (e.g., 10% of users).
   - Monitor Crashlytics and Analytics for issues.
   - Gradually increase to 100%.

## 5. Post-Release
- Update `latest_version_code` in Firebase Remote Config after each production release.
- Respond to user reviews and monitor performance in the Play Console.
