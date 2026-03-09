# Tile System

## Purpose

This document defines the shared tile language for `Start`, `Single`, and later `Multi`.

The goal is not that every mode has the same layout.
The goal is that every mode speaks the same container language.

That means:
- same tile anatomy
- same shape rhythm
- same spacing rhythm
- same color roles
- same interaction rules
- adaptive layout changes without inventing a new visual system per mode

## Why this exists

The current `Start` surface already has the clearest visual direction in the app:
- large calm containers
- very little copy
- meaning mostly carried by shape, spacing, iconography, and color

The current `Single` screen is stronger in product meaning, but it is not yet formally part of the same tile system.

Today the project still has these gaps:
- `StartTile` exists only inside `StartScreen`
- corner radii vary too much (`16`, `18`, `22`, `24`, `30`, `32`, `999`)
- tile roles are not named
- spacing is locally coherent, but not systematized
- `Single` uses card containers, but not yet a clearly defined tile hierarchy

## External grounding

This definition follows the current official Material 3 and Android guidance:
- cards should represent one coherent piece of content, not become a generic screen shell
- shapes should come from a defined shape scale, not from many ad-hoc corner values
- adaptive layouts should change pane structure based on window size, instead of simply stretching one phone layout
- top-level navigation should adapt across compact and expanded windows

Sources:
- [Card | Jetpack Compose | Android Developers](https://developer.android.com/develop/ui/compose/components/card)
- [Material Design 3 in Compose | Android Developers](https://developer.android.com/develop/ui/compose/designsystems/material3)
- [Build adaptive apps | Android Developers](https://developer.android.com/develop/ui/compose/build-adaptive-apps)
- [Build adaptive navigation | Android Developers](https://developer.android.com/develop/ui/compose/layouts/adaptive/build-adaptive-navigation)

## Core rule

`Days` uses one shared tile family across all modes.

Important consequence:
- `Start`, `Single`, and `Multi` must not invent separate surface grammars
- they may use different tile arrangements and density
- they must still feel like the same product
- phone edit flows should prefer one orienting tile plus one work tile instead of long stacks of sibling form cards
- inside a work tile, prefer section rhythm and dividers over obvious nested mini-cards
- a lower work tile should still read as one object even when it contains summary, signals, and form controls

## What a tile is

A tile is the primary visual container of the app.

A tile is:
- one coherent unit of meaning
- directly scannable
- visually calm
- usually actionable
- never a random wrapper around unrelated sub-sections

A tile is not:
- a generic white rectangle
- a feed card with noisy metadata
- a container for multiple unrelated interaction patterns
- a workaround for missing navigation structure

## Tile anatomy

Every tile should use the same internal grammar.

Ordered from top to bottom:
- `eyebrow`: short system label like `Pulse`, `Tag`, `24h Dashboard`
- `headline`: the main read of the tile
- `payload`: the visual or structural content
- `support`: optional compact context, state, or next step
- `affordance`: optional directional hint like arrow, switch affordance, or status mark

Rules:
- eyebrow is short and low-emphasis
- headline is the first semantic read
- payload carries most of the value
- support text stays short
- if a tile needs paragraphs to explain itself, the tile is underspecified
- if a chip or label does not change a decision, it should be removed
- generic counters like `1 Ziele` or weak state words like `aktiv` are not valid support copy on their own
- internal implementation terms like `Core` stay out of user-facing tiles unless they are truly product language

## Tile roles

We standardize four tile roles.

### 1. Hero tile

Used for the primary work surface of a mode.

Examples:
- `Single` daily dashboard
- later `Multi` coordination overview

Characteristics:
- visually dominant
- highest information density
- may contain structured sub-tiles or rows inside
- should still represent one main job
- on phone, edit-heavy flows should usually resolve into one hero work tile, not several equal-weight form cards

### 2. Summary tile

Used for scanning a compact topic or area.

Examples:
- `Pulse`
- later `Multi` person/team/day summaries

Characteristics:
- medium density
- optimized for fast scanning
- usually one tap target

### 3. Action tile

Used when the tile primarily moves the user somewhere or starts a flow.

Examples:
- create/capture entry points
- mode-specific next-step surfaces

Characteristics:
- strong affordance
- minimal copy
- one dominant action

### 4. Row tile

Used inside a hero tile or in stacked lists when a compact structured subunit is needed.

Examples:
- hour rows in `Single`
- future `Multi` person rows or block rows

Characteristics:
- dense, but still clearly separated
- one small task or state unit
- never visually heavier than the hero tile that contains it

## Shape scale

We stop choosing radii ad hoc.

The tile system should use this shape scale:
- `tileSmall = 16.dp`
- `tileMedium = 22.dp`
- `tileLarge = 28.dp`
- `tileHero = 32.dp`
- `pill = 999.dp`

Mapping:
- row tile: `tileMedium`
- summary tile: `tileLarge`
- action tile: `tileLarge`
- hero tile: `tileHero`

Rule:
- avoid new intermediate radii unless there is a strong reason

## Spacing scale

Tiles need a predictable spacing rhythm.

Use:
- `space8`
- `space12`
- `space16`
- `space20`
- `space24`

Default mapping:
- row tile padding: `16.dp`
- summary tile padding: `20.dp`
- hero tile padding: `24.dp`
- tile-to-tile gap on phone: `16.dp`
- tile-to-tile gap on larger widths: `20.dp`

Rule:
- spacing should express hierarchy more than text does

## Color roles

Tiles should use color as state and emphasis, not as decoration.

Default roles:
- page background: warm neutral `surface`
- primary tile container: `surfaceStrong`
- secondary inset area: `surface`
- border: `outlineSoft` at low alpha
- accent: reserved for active state, key emphasis, progress, or primary affordance
- status colors: only for domain/state semantics

Rules:
- do not mix several bright accents inside one tile unless data requires it
- most tiles should read as neutral first, semantic second
- preserve contrast using matching `on-*` color roles
- state color belongs in indicators, chips, progress, or small highlighted regions, not the entire tile background

## Elevation and borders

The system should feel soft and grounded, not shadow-heavy.

Rules:
- prefer filled or softly outlined surfaces over visible shadow stacks
- one subtle border is usually enough
- use stronger outlines only for active or selected state
- avoid mixing elevation, thick border, bright fill, and strong icon tint on the same tile

## Interaction rules

Rules:
- a tappable tile should use the component's clickable form, not only an outer modifier when avoidable
- one tile should have one dominant action
- nested tap zones should be rare and visually obvious
- pressed, selected, and disabled states must be visible without adding extra explanatory copy

## Typography rules

Rules:
- eyebrow uses the small label style
- headline uses one stable title style per tile role
- payload typography depends on content, not on arbitrary emphasis
- support text stays one level below the headline
- avoid building hierarchy through many font sizes; use spacing and grouping first

## Adaptive layout rules

Material guidance is clear: adaptive layouts should change structure, not just stretch.

This is the implication for `Days`:

### Compact width

- one primary column
- hero tile stacked with summary/action tiles
- row tiles may live inside the hero tile

### Medium width

- still mostly one reading flow
- summary tiles may form a two-column grid
- hero tile remains the visual anchor

### Expanded width

- use panes, not stretched phone cards
- one hero pane plus one supporting pane is the default direction
- top-level navigation should be able to switch from bottom-style behavior to rail-style behavior

## Mode application

### Start

`Start` remains the purest tile surface.

Rules:
- only summary and action tiles
- no stray ribbons, strips, or explainer sections outside tiles
- each new Start function must enter as a tile
- the primary `Pulse` tile is now a fixed 4x4 area map
- area labels sit below the icon surface, not inside the gray container
- day-status copy like `Heute`, `offen`, or similar summary counters should not return on `Start`

### Single

`Single` should adopt the same tile language, but not collapse into a generic grid.

Rules:
- `Home` remains the anchor
- `Home` is a hero tile, not a separate visual system
- window switcher, domain strip, and hour rows must be treated as structured content inside the hero tile
- supporting functions around `Single` should use summary or action tiles, not unrelated screens with new container styles

Important consequence:
- `Single` should share the tile grammar of `Start`
- `Single` does not need to copy the exact Start layout

### Multi

`Multi` should be built from the same tile family from day one.

Recommended direction:
- compact: summary tile stack
- expanded: supporting-pane or list-detail structure
- each person, team, or day cluster appears as a summary or row tile
- coordination overview becomes the hero tile

## Current code implications

Near-term implementation work should do the following:

1. Extract a shared tile primitive from `StartTile`
2. Add explicit tile tokens to the theme for shape, spacing, and role
3. Migrate `Single` containers onto named tile roles instead of raw `Card` usage
4. Reduce radius drift across the whole app
5. Keep `Start` as the reference surface for calm, low-copy tile behavior

## Non-goals

This tile system does not mean:
- every screen becomes a dashboard
- every card becomes identical
- every mode uses the same density
- `Single` stops being the primary product surface

## Decision test

Any future screen or refactor should pass this test:

1. Is each tile one coherent unit of meaning?
2. Does the screen still look like `Days`, not like a new mini-app?
3. Is the hierarchy carried mostly by shape, spacing, and structure?
4. On larger windows, does the layout change by panes rather than stretch?
5. Could the same tile role exist in `Start`, `Single`, and `Multi` without redesigning the whole system?
