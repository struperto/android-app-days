# Start V1

`Start` is the life-area mode of `Days`.

This file replaces earlier parallel notes about the start area model and dashboard matrix.

## Purpose

`Start` is a calm local-first overview of meaningful life areas.

It should:
- make important areas visible at a glance
- stay quiet and visually legible
- open directly into one area-specific next action

It should not:
- become a mixed launcher
- imitate a KPI dashboard
- explain itself through long copy

## Current shell

`Start` uses one large overview surface with a fixed 4x4 area grid.

Rules:
- only large tiles
- no KPI strips outside tiles
- no button rows as the main pattern
- no helper prose on first read
- area titles sit below or clearly separated from the icon surface

## Current seeded areas

The current fixed area set is:
- `Vitalitaet`
- `Fokus`
- `Arbeit`
- `Partnerschaft`
- `Familie`
- `Freundschaft`
- `Netzwerk`
- `Zuhause`
- `Sicherheit`
- `Erholung`
- `Entwicklung`
- `Lernen`
- `Kreativitaet`
- `Freude`
- `Sinn`
- `Neues`

This set is a useful seed, not a permanent taxonomy.

## Area model

Each area should be representable with:
- stable `id`
- title
- icon
- short meaning or summary
- overview mode: `signal`, `plan`, or `reflection`
- current state semantic
- progress or directional cue
- next useful step

Areas must stay user-editable and later user-creatable without changing the shell.

## Detail contract

Opening an area keeps the same visual language.

The detail surface should have:
- one calm identity tile first
- one work tile below it
- three next levels:
  - `Status`
  - `Experiment`
  - `Automatik`

Complex editing or setup should move into focused screens instead of being compressed into oversized sheets.

## Data realism

Not every area is equally live or equally measurable.

Use three overview modes:
- `signal`: current condition, trend, drift
- `plan`: progress, next step, review rhythm
- `reflection`: last care, direction, next impulse

Do not pretend reflective areas are live telemetry when they are not.

## Non-goals

Do not block `Start` on:
- network sync
- BLE or Wi-Fi orchestration
- complex automation stacks
- deep multi-person coordination
