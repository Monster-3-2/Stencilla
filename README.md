# Stencilla — The Outfit Coach

AI wardrobe and styling app. Phase 1 (this build) covers: a fully **local-only** virtual closet
(photos and items never leave the device, except for one momentary AI tagging call), hybrid
auto-tagging (on-device rough label + Groq vision for the real tags), a styling profile (age,
lifestyle, body type, skin tone, style goal), AI outfit suggestions including Anchor Item Magic
(build a look around one chosen piece), and shopping suggestions for wardrobe gaps.

## Architecture

```
[Android app]
  ├─ Room database (closet items + tags)        <- lives entirely on-device
  ├─ Internal storage (closet photos, .jpg)      <- lives entirely on-device
  ├─ DataStore (auth token)                      <- lives entirely on-device
  └─ HTTPS ──► [FastAPI backend on Cloud Run]
                  ├─ Auth (register/login/profile) ──► Turso (libSQL/SQLite-compatible)
                  └─ AI calls ──► Groq API (Llama 4 Scout vision + Llama 3.3 70B text)
```

The backend is intentionally stateless for wardrobe/outfit data — there is no server-side
wardrobe table. Two endpoints carry the AI features:

- `POST /wardrobe/tag` — client uploads one photo, gets structured tags back. The image is
  base64-encoded in memory for the Groq call and discarded immediately after; it's never written
  to disk or any cloud storage server-side.
- `POST /outfits/suggest` — client sends its current local closet (just the tags, not images)
  plus the occasion/profile, gets back which item ids form an outfit, a reasoning sentence, and
  shopping suggestions. No outfit history is stored server-side — if you want history, the app
  would need to save past results into Room too (not built yet).

The only thing the backend persists is the `users` table (email, password hash, style profile) —
that's what lives in Turso.

## Repo layout

```
stencilla/
├── android/        Kotlin + Jetpack Compose app (Room for local closet storage)
├── backend/         FastAPI service (Python), stateless except for user accounts
└── .github/workflows/android-build.yml   CI that builds the debug APK
```

## How the AI is split (hybrid)

- **On-device**: ML Kit's bundled Image Labeling gives an instant, fully offline rough guess the
  moment a photo is taken or picked. No model file to source or train — it ships inside the
  `com.google.mlkit:image-labeling` library itself. This is purely a placeholder shown in the UI
  while the real tagging call is in flight.
- **Cloud**: right after a photo is saved locally, the backend's `/wardrobe/tag` endpoint asks
  Groq's vision model (Llama 4 Scout) for the authoritative category, subcategory, colors,
  pattern, formality, and season — returned as structured JSON, then written into Room alongside
  the local photo. Outfit generation (including Anchor Item Magic) uses Groq's Llama 3.3 70B text
  model, reasoning over your profile + your current local wardrobe (sent fresh with each request).

## Backend setup

1. `cd backend && cp .env.example .env` and fill in:
   - `GROQ_API_KEY` — free, no credit card, from
     [console.groq.com/keys](https://console.groq.com/keys)
   - `TURSO_DATABASE_URL` / `TURSO_AUTH_TOKEN` — free, no credit card, from
     [turso.tech](https://turso.tech). Install the Turso CLI, run `turso db create stencilla`,
     then `turso db show stencilla` for the URL and `turso db tokens create stencilla` for the
     token. Leave both blank for local development — the backend automatically falls back to a
     local SQLite file (`stencilla.db`) when they're empty.
   - `JWT_SECRET` — any long random string.
2. Local run: `pip install -r requirements.txt && uvicorn app.main:app --reload`
3. **Deploy to Google Cloud Run** (free tier — note: enabling Cloud Run requires a credit card on
   file even though the free tier itself won't charge you):
   ```bash
   gcloud auth login
   gcloud config set project YOUR_PROJECT_ID
   cd backend
   gcloud run deploy stencilla-api \
     --source . \
     --region us-central1 \
     --allow-unauthenticated \
     --set-env-vars JWT_SECRET=your-secret,GROQ_API_KEY=your-key,TURSO_DATABASE_URL=your-db-url,TURSO_AUTH_TOKEN=your-token
   ```
   `gcloud` builds the Dockerfile and deploys it for you — no separate Docker build step needed.
   Cloud Run prints the live HTTPS URL (something like
   `https://stencilla-api-xxxxx-uc.a.run.app`) when it finishes — copy it for the Android app.
4. Cloud Run's free tier scales to zero between requests (no idle cost) and wakes on the next
   request with roughly 1-3 seconds of cold-start latency — noticeable but not disruptive for a
   personal app.

The backend was smoke-tested end-to-end during development (register/login/profile flows, the
stateless `/wardrobe/tag` endpoint, and `/outfits/suggest` including its anchor-item and
empty-wardrobe error paths) all verified against a live test server with the AI calls mocked.
What's *not* verified is the real Groq calls or a real Turso connection, since those need your
own credentials.

## Android setup

1. Open `android/` in Android Studio (Iguana or newer).
2. In `android/app/build.gradle.kts`, replace the `BASE_URL` placeholder with your deployed Cloud
   Run URL (must end in `/`).
3. Let Gradle sync. **Note:** this repo does not commit the Gradle wrapper jar (a binary file I
   couldn't generate in my build environment). Android Studio will offer to regenerate it on first
   sync — accept that, or run `gradle wrapper --gradle-version 8.7` once from a terminal if you have
   Gradle installed locally.
4. Run on a device/emulator (minSdk 26 / Android 8+).

### Getting an APK without Android Studio

A native Android app is **compiled source code**, not a website — it cannot be produced by a
"website-to-APK" converter tool (those wrap an `index.html` in a WebView and will fail with
exactly the error you saw, since there's no `index.html` here — this is a real Kotlin project).
The only correct ways to get an installable APK from this repo are:

- **GitHub Actions (recommended, no Android Studio needed):** push this repo to GitHub. The
  included `.github/workflows/android-build.yml` runs automatically, installs the real Android
  SDK and Gradle in the cloud, and compiles the app properly. Open your repo's **Actions** tab,
  click the latest run, wait for the green check, then open **Artifacts** at the bottom and
  download `stencilla-debug-apk`. Unzip that download to get `app-debug.apk`, transfer it to your
  phone, and tap it to install (Android will prompt you to allow installs from this source once).
- **Android Studio:** open the `android/` folder as a project (not as a file/folder browse — use
  **File → Open**), let it sync, then **Build → Build App Bundle(s)/APK(s) → Build APK(s)**.

## Local-first design notes

- Closet photos live in the app's private internal storage (`filesDir/closet_photos/`) —
  invisible to other apps, not in the user's gallery, deleted automatically on uninstall, and
  included in Android's automatic app backup (so a closet survives a phone backup/restore).
- Closet metadata (tags, ids, timestamps) lives in a local Room/SQLite database on-device.
- The backend never sees a photo more than once (the single tagging call) and never stores one.

## Honest limitations of this Phase 1 build

- No outfit history — since the backend is stateless, a generated outfit only exists until you
  navigate away. Worth adding as a local Room table if you want to revisit past suggestions.
- No cross-device sync — because the closet is local-only by design, it doesn't follow you to a
  second device. A future "backup to cloud" feature would need to be opt-in, given the privacy
  trade-off of uploading photos of someone's actual wardrobe.
- Skin tone is self-selected from a swatch list rather than detected from a selfie scan, and
  there's no body-measurement scanning yet — both are real computer-vision features that need a
  deliberate build (and a privacy-design pass, since they involve photos of people's bodies), not
  something to bolt on casually.
- I could not compile this Android project myself (no Android SDK in my build sandbox) — I
  cross-checked every non-obvious API (Room, ML Kit, Navigation Compose, icon names, library
  package paths) against current documentation and tested the libSQL/Turso SQLAlchemy dialect
  locally, but the first real compile will be in Android Studio or CI.

## Roadmap (not yet built)

Calendar/weather-aware suggestions, the AI Outfit Verifier (selfie upload → fit/style feedback),
skin-tone and body scanning, an ongoing learning algorithm that refines suggestions from your
feedback, local-only wardrobe usage analytics, gamified styling goals, and exact-size shopping
integration. Tell me which of these you want next.
