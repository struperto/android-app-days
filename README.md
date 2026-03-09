# Android App Days

Clean-slate Android app repo for the final `Days` product.

## Product direction

This repository is `Single`-first.

Current visible shape:
- `Home`
- `Einstellungen`
- optional `Lebensrad` calibration

Current product idea:
- `Home` is the day itself
- the mirror dashboard shows fit vs drift
- block overlays are the real work surface
- `+` is only a small quick add
- passive context currently comes from carryover, calendar, and notification pressure

Out of scope for the current foundation:
- `Multi`
- `Assist` as a full mode
- Operator / experiment surfaces
- cloud AI
- large passive signal UI

## Tech baseline

- Kotlin
- Jetpack Compose
- Material 3
- Navigation Compose
- Room
- Android minSdk 26
- Android targetSdk 36
- JDK 17

## Local commands

```bash
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
./gradlew :app:compileDebugAndroidTestKotlin
```

## Product docs

The current product truth lives here:
- `docs/single-v1.md`
- `docs/single-v1-contract.md`
- `docs/single-ux-concept.md`
- `docs/decisions.md`
- `docs/parking-lot.md`

## Current status

The repository currently contains:
- `single_home`
- optional calibration and legacy slices kept compile-safe
- local data + projection for a `Soll` / `Ist` day track

The active visible loop is:
`Home -> Block Overlay -> Home`
