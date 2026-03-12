# Decisions

This file keeps only active product and architecture decisions. Move ideas, variants, and open exploration into `parking-lot.md`.

## 2026-03-07 - `Single` ships first

Decision:
- Build `Single` first until the core daily loop feels real.

Consequence:
- `Multi`, `Assist`, and lab surfaces stay secondary and must not distort the first shipped experience.

## 2026-03-07 - `Home` is the primary product surface

Decision:
- Keep `Home` as the visible anchor of the app.

Consequence:
- The strongest user-facing loop remains `Home -> Block Overlay -> Home`.
- `Workbench` and similar tool surfaces may stay compile-safe, but are not primary product language.

## 2026-03-07 - First visible value is fit vs drift

Decision:
- The app should first answer whether the current day fits the personal `Soll`, where it drifts, and what correction is useful next.

Consequence:
- The dashboard stays visual and text-light.
- The day is shown in three broad windows.
- Block detail stays lightweight and directly actionable.

## 2026-03-07 - `Soll` is inferred locally

Decision:
- `Soll` is learned from local rhythm, plans, pressure, context, and recent behavior, not from repeated manual scoring.

Consequence:
- No forced setup gate.
- No repeated visible `0-5` ratings.
- Local rule-based adaptation remains the default path.

## 2026-03-07 - Passive context grows in small local steps

Decision:
- Passive context enters `Single` in this order: yesterday carryover, calendar structure, notification pressure.

Consequence:
- The app stays useful without permissions.
- Each new source must improve the day model without turning the UI into an inbox or telemetry wall.

## 2026-03-07 - Calm geometry beats feature sprawl

Decision:
- Prefer the quieter mirror dashboard and a stable tile language over adding more visible panels, coach copy, or competing surfaces.

Consequence:
- New features must reinforce one calm visual system.
- If a change needs explanation text to work, the layout or interaction model is likely wrong.
