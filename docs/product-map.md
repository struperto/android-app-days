# Product Map

This file is the active product source of truth for the overall `Days` model.

## Product sentence

`Days` translates sources into goals, goals into daily clarity, and later into profile learning and human resonance.

## Core chain

The product is organized around this chain:

`Quelle -> Signal -> Bereichslogik -> Bereichsfeed in Single -> Profillernen -> Multi`

Rules:
- Think in `signals`, not in app clones.
- Every visible feature must map back to the chain above.
- `Multi` must never become the dependency for first user value.
- `Start` and `Single` must already be useful without `Multi`.

## Mode meanings

### `Start`

`Start` is the orchestrator and workbench.

It owns:
- area creation
- area analysis
- blocker handling
- source selection
- goal shaping
- output preview

It does not exist to be a passive dashboard.

### `Bereich`

A `Bereich` is an editable logic surface with its own intent.

Each area may eventually contain:
- sources
- rules
- goals
- allowed outputs
- evaluation
- analysis state

The exact minimal contract is still under evaluation and must be tested against concrete runs.

### `Single`

`Single` remains the mode name.

Its current product meaning is:
- one calm daily reading surface
- one overall feed across areas
- cards as the primary pattern
- each card is one visible output from one area

Current working internal term:
- `Bereichsfeed`

Do not use `Bereichsinfo` anymore.

### `Profil`

`Profil` does not start as a manually edited profile.

It first learns from:
- what the user actually reads in `Single`
- what gets ignored
- explicit feedback when it exists

### `Multi`

`Multi` is the later resonance layer.

It may eventually involve:
- profile understanding
- recommendation
- matching
- resonance
- map or world surfaces

But it stays secondary until `Start` and `Single` feel real.

## Product principles

- `Start` is the motor.
- `Single` is the proof of value.
- Areas are translators, not categories.
- Signals are raw material, not product value by themselves.
- Every area must eventually expose a simple visible output for `Single`.
- Analysis is mandatory during area creation and may block hard.
- Analysis may re-trigger later when inputs, goals, or rules change.
- The UI should stay calm, text-light, and visually strict.

## MVP proving examples

The first proving areas are:
- `News`
- `Freunde/Nachrichten`
- `App-Bau`

These are the reference cases for architecture and flow decisions.

## Non-goals for the current implementation phase

- building `Multi` as a full product surface
- making `Profil` editable before it is readable
- supporting every possible signal source at once
- turning `Start` into a settings graveyard
- turning `Single` into a tool hub
