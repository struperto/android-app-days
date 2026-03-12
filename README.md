# Android App Days

Android app repo for the `Days` product.

## Product direction

The repo is currently `Single`-first.

Current visible product:
- `Home`
- `Einstellungen`
- optional `Lebensrad`

Current product thesis:
- `Home` is the primary day surface
- the mirror dashboard shows fit vs drift
- block overlays are the real action layer
- `+` stays a small quick add entry
- passive context is local-first and currently comes from carryover, calendar, and notification pressure

Current exploration:
- `Start` as a life-area surface with a fixed tile language

Out of scope until the core loop is stable:
- `Multi` as a shipped mode
- `Assist` as a full mode
- operator or lab surfaces as primary UI
- cloud AI
- large passive-signal dashboards

## Tech baseline

- Kotlin
- Jetpack Compose
- Material 3
- Navigation Compose
- Room
- minSdk 26
- targetSdk 36
- JDK 17

## Local commands

```bash
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
./gradlew :app:compileDebugAndroidTestKotlin
./gradlew :app:installDebug
./scripts/capture_persona_dashboard_suite.sh
```

## Source Of Truth Docs

Keep product truth in a small set of docs:
- `docs/decisions.md`
- `docs/single-v1-contract.md`
- `docs/single-ux-concept.md`
- `docs/single-domain-foundation.md`
- `docs/start-v1.md`
- `docs/tile-system.md`
- `docs/mvp-user-test-scenarios.md`
- `docs/parking-lot.md`

## Repo hygiene

- Temporary screenshots, XML dumps, and exported DB files do not belong in the repo root.
- Put exploratory artifacts under ignored paths such as `tmp/` and remove them after review.
- If a doc is not part of the active source of truth, merge or delete it instead of letting parallel variants drift.
