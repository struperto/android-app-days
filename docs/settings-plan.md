# Settings Plan

This file is the active source of truth for `Settings`.

## Product role

`Settings` should become a calm system shell, not a second product universe.

It should help the user inspect configuration state without creating more conceptual layers than necessary.

## Structure direction

`Settings` should stay slim and monolithic.

Desired feeling:
- gray
- restrained
- simple
- list-like
- no ornamental feature sprawl

## Menu pattern

Use a simple two-part row pattern:
- left: setting name
- right: current status

This same principle should also be usable inside area-related settings.

The row itself should open the relevant next screen directly.

Reference examples:
- `Benachrichtigungen` -> `Erlaubt`
- `Kalender` -> `Nicht verbunden`
- `Signalquellen` -> `5 aktiv`
- `Profil` -> `Nur Lesemodus`

## Responsibility split

- global system and access state may live in `Settings`
- area-specific logic should primarily live inside `Bereich`

`Settings` should not become the place where real area work happens.

## Design rules

- full-screen focused lists over nested dashboards
- clear state at a glance
- keep descriptions short
- avoid deep special-case trees
- prefer one direct next step per row

## Current implementation priorities

1. Simplify the visual language.
2. Move toward list rows with label and current status.
3. Reduce the feeling of a monolithic settings blob.
4. Keep `Settings` consistent with the stricter area design language.
5. Prefer menu-like rows and focused follow-up screens over decorative cards.
