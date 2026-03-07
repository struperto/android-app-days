# Android App Days

Clean-slate Android app repo for the final `Days` product.

## Product direction

This repository starts `Single-first`.

Initial V1 scope:
- Home
- Life Wheel onboarding and management
- Working Set edit flow
- Day schedule
- Plan
- Capture
- Create
- Small settings surface

Out of scope for the initial foundation:
- Multi mode
- Coworker mode
- Operator and experiment screens
- Large AI and background automation surface

## Tech baseline

- Kotlin
- Jetpack Compose
- Material 3
- Navigation Compose
- Android minSdk 26
- Android targetSdk 36
- JDK 17

## Local commands

```bash
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
```

## Collaboration defaults

- Work on feature branches.
- Open PRs into `main`.
- Keep `main` releasable.
- Prefer small, reviewable changes.

## Current status

The repository currently contains the initial buildable Android foundation only.
