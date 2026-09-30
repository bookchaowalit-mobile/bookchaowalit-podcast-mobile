# Podcast — Mobile

Jetpack Compose Android app for **Podcast** — subscribe to shows and listen with a play queue.

Part of [Chaowalit Greepoke](https://bookchaowalit.com)'s 101 Portfolio Projects.

## Status

- `core/` — pure-Kotlin domain logic with unit tests. Builds and tests
  without the Android SDK. **This is the verified part of the repo.**
- `app/` — Compose UI shell (Home / Explore / Profile tabs). It depends on
  `core`, but the screens do not use it yet, and the Android build has so far
  only been checked in CI, not locally. See [docs/UPGRADE-PLAN.md](docs/UPGRADE-PLAN.md).

## Core features (`core/`, package `com.bookchaowalit.podcast`)

- `itunes:duration` parsing (`SS`, `MM:SS`, `HH:MM:SS`; malformed values return null) and display formatting
- Listening progress per episode with a 95% "played" threshold and resume points
- Clamped skip-back / skip-forward
- Play queue: add to end (no duplicates), play next, remove, next/previous, total remaining time

## Tech Stack

- **UI:** Jetpack Compose + Material 3 (Compose BOM 2024.06)
- **Language:** Kotlin 2.0 (Compose compiler Gradle plugin)
- **Android:** AGP 8.5, min SDK 26, target SDK 34
- **Build:** Gradle 8.10 wrapper (committed); `core` is an included build

## Getting Started

Requires JDK 17+.

```bash
./gradlew -p core build          # domain logic + unit tests, no Android SDK needed
./gradlew :app:assembleDebug     # needs the Android SDK (ANDROID_HOME or local.properties)
```

CI (`.github/workflows/build.yml`) runs both; either failing fails the workflow.

## Related

- **Frontend:** [bookchaowalit-website/podcast-frontend](https://github.com/bookchaowalit-website/bookchaowalit-podcast-frontend)
- **Portfolio:** [bookchaowalit.com](https://bookchaowalit.com)

## License

MIT
