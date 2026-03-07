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

## Product docs

The current product truth lives here:
- `docs/single-v1.md`
- `docs/decisions.md`
- `docs/parking-lot.md`

## Current status

The repository now contains the first Single-first product structure:
- `single_home`
- `single_life_wheel`
- `single_working_set`
- `single_day_schedule`
- `single_plan`
- `single_capture`
- `single_create`
- `settings`

These screens are still lightweight, but the flow boundaries are now explicit and ready for iterative product work.
