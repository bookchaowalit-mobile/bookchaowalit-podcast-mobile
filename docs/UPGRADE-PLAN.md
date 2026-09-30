# Upgrade plan

## Current state

Score: 5/10 (was 4/10) — tested pure-Kotlin domain core and honest CI; the
Compose UI is still a placeholder and the Android build is unverified locally.

## Backlog

- P0: Confirm the `android` CI job is green (AGP 8.5 + Kotlin 2.0.21 Compose
  plugin + material dependency were fixed statically, without an Android SDK).
- P0: Replace the placeholder Home/Explore screens with UI backed by `core`
  (ViewModel + state holder calling the `core` API).
- P1: RSS parsing of show feeds in `core` with fixture tests; subscription list.
- P1: Media3/ExoPlayer playback service driven by `PlayQueue` + `ProgressTracker`; persist progress.
- P2: Compose UI tests (`androidx.compose.ui:ui-test-junit4`) for the main flow.
- P2: Pin `distributionSha256Sum` in `gradle-wrapper.properties`.

## Done in this pass (pass 1)

- Added `core/` (included Gradle build) with the app's domain logic and 6 unit tests.
- Committed the Gradle wrapper (8.10.2).
- Fixed Kotlin 2.0 Compose setup: `org.jetbrains.kotlin.plugin.compose`
  replaces the removed `composeOptions.kotlinCompilerExtensionVersion`; Java/Kotlin
  target 17; added `com.google.android.material` for the manifest theme.
- CI: removed `|| true`; separate `core` and `android` jobs.
- README now states what is verified and what is not.

## Done in this pass (pass 2)

- Fixed `PlayQueue.playNext`: moving an episode queued *before* the current one, or the current one itself, silently switched the currently playing episode. The current episode now never changes; play-next on it is a no-op.
- 8 more edge-case tests (14 total): play-next in all positions, empty queue, duplicate adds, malformed `itunes:duration` values, format boundaries, progress clamping/zero-length episodes. Verified with `./gradlew -p core test --offline`.
- Still not verified locally: the Compose `app/` (no Android SDK; dl.google.com is blocked here).
