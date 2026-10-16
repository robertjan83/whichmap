# WhichMap for Android

Android app + home screen widget for the Warzone Resurgence map rotation.
The app shows the same page as the website (copied into the app by the build),
the widget shows the current map with a live countdown (Solo, Duos or Quads, chosen when you add it).

## Install
1. Open the **Releases** page of this repository and download `WhichMap.apk` (tag `android-latest`).
2. Open the file and allow "install from this source" when Android asks.
3. Long-press the home screen → Widgets → **WhichMap** → pick Solo, Duos or Quads.

## Build
GitHub Actions builds the APK on every push (`.github/workflows/android.yml`).
Locally: copy `index.html`, `img/` and `icons/` into `android/app/src/main/assets/`, then run `gradle assembleDebug` in `android/`.

## Rotation
`Rotation.kt` and `index.html` use the same reference time (`REF`). If you adjust one, adjust the other.
