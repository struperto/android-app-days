# Codex setup for Days

## Config split

- Global config in `~/.codex/config.toml` stays the safe default.
- Repo config in `.codex/config.toml` is the Android override for this repo.
- Use the repo config for Gradle, Android SDK, emulator, screenshots, and `adb` work.

## Working model

- Keep one lead chat in the main worktree for integration and final review.
- Give each writing chat its own worktree.
- Keep file ownership clear. Do not let two writing chats edit the same area at once.

## Suggested worktrees

- `android-app-days`: Lead/Integrator
- `android-app-days-analysis`: Analyse or Doku
- `android-app-days-ui`: UI/Compose
- `android-app-days-data`: Architektur/Data
- `android-app-days-test`: Tests/Build

## Agent roles

### Lead/Integrator

- Owns planning, merge order, final verification, and cross-cutting decisions.
- Writes small integration changes only.

### Analyse

- Reads `docs/`, `navigation/`, `domain/`, and current feature files.
- Produces findings, task slices, and risk notes.

### Architektur/Data

- Owns `data/`, `domain/`, `AppContainer.kt`, and repository or DAO changes.
- Avoids UI changes except small contract fixes.

### UI/Compose

- Owns `feature/`, `navigation/`, and `ui/theme/` when implementing screens.
- Uses emulator screenshots for larger UI iterations.

### Tests/Build

- Owns Gradle verification, device install, emulator refresh, and regression checks.
- Handles `adb`, screenshots, test runs, and build failures.

### Doku/Decisions

- Owns `docs/*.md`.
- Keeps decisions and product contract in sync with shipped behavior.

## Parallel rules

- One writing agent per worktree.
- One owner per file area.
- Use read-only analysis chats freely, but keep writing chats isolated.
- Merge back through the lead chat after tests pass.
