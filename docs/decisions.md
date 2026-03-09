# Decisions

## 2026-03-07 - Start with Single only

Decision:
- Build `Single` first until it becomes a real daily-use flow.

Why:
- The old project proved the direction, but spread across too many systems.
- `Single` defines the real product rhythm and value.

## 2026-03-07 - Home is the real product surface

Decision:
- Keep the current `Home` geometry and make it the real work surface.

Why:
- The user strongly likes the current Home shell.
- The missing piece is not layout, but meaning and usefulness.
- Replacing Home would throw away the strongest part of the current product.

## 2026-03-07 - Werkbank is not the primary concept

Decision:
- `Werkbank` is no longer the main visible product path.

Why:
- It bundled many capabilities, but felt like a container for tools instead of a useful daily surface.
- `Home` plus block overlays is a stronger mental model than a separate workspace hub.

Implementation consequence:
- `Workbench` may remain compile-safe, but Home must no longer depend on it.

## 2026-03-07 - The first value is fit vs drift

Decision:
- The first visible value of `Single` is seeing whether the day fits the personal `Soll`.

Why:
- The app should answer:
  - does the day fit?
  - where is it drifting?
  - what is the next useful correction?

Implementation consequence:
- The day track shows three blocks.
- Block tap opens lightweight detail.
- The graph stays visual and text-light.

## 2026-03-07 - Soll must be learned locally, not rated manually

Decision:
- `Soll` is no longer driven by repeated daily ratings.
- It is inferred locally from rhythm, plans, pressure, context, and recent behavior.

Why:
- Repeated manual scoring felt heavy and unhelpful.
- The core idea is that the phone should think with the user, not ask to be rated all the time.

Implementation consequence:
- no forced `0-5`
- no setup gate
- local rule-based `Soll Engine v0`

## 2026-03-07 - Soll v1 uses three local axes

Decision:
- `Soll` is shaped internally by three local axes:
  - `Fokusdruck`
  - `Stoeranfaelligkeit`
  - `Energiebedarf`

Why:
- The 1:1 user archetypes are useful for thinking, but too rigid as a product model.
- The app should adapt the day without forcing the user into a visible type label.
- These three axes are enough to make `Home` feel more personal without needing cloud AI or a long setup.

Implementation consequence:
- the engine derives a soft local profile from recent plans, open loops, and calibration
- each block gets stronger reasons, focus hints, and target weights
- `Home` can feel more focused, flexible, coordinating, creative, or gentle without exposing profile jargon

## 2026-03-07 - First passive context source is carryover from yesterday

Decision:
- The first passive context source for `Single` is yesterday's still-open plan carryover.

Why:
- It improves `Soll` immediately without requiring new Android permissions.
- A day should not look artificially clean when relevant pressure was left open yesterday.
- This is a better first step than jumping straight to notifications or broader sensors.

Implementation consequence:
- `Soll Engine` now folds yesterday's open plan items into morning target/pressure
- block reasons can explicitly mention yesterday carryover
- the local axes are nudged by yesterday pressure before any broader passive-signal work

## 2026-03-07 - Calendar is the first external passive source

Decision:
- Calendar is the first external passive source wired into `Single`.

Why:
- It adds real-world structure without pushing the product into a notification wall.
- Calendar explains why a block is already "occupied" even when the user did not explicitly plan it inside the app.
- It fits the Home day-track model better than a separate inbox-like source.

Implementation consequence:
- when permission is available, calendar events shape block target/pressure
- block overlay can show calendar context directly
- Home stays useful without permission and simply falls back to local-only signals

## 2026-03-07 - Notifications become the first passive pressure source

Decision:
- Notification listener signals are now wired as passive pressure for the current day.

Why:
- They add real-world interruption and coordination pressure that plans and calendar alone cannot explain.
- They fit the `fit vs drift` model better than a separate inbox surface.
- They stay optional behind a system listener toggle and do not block first use.

Implementation consequence:
- active notifications for today are stored locally
- `Soll Engine` can nudge current-block actual/pressure from notification load
- block overlay shows notification context directly
- `Settings` exposes calendar permission and notification-listener activation as the first passive-source controls

## 2026-03-07 - Home returns to the quieter mirror layout

Decision:
- Keep the newer passive-signal and `Soll` logic, but bring the visible Home design back to the earlier calmer mirror dashboard.

Why:
- The earlier bilateral mirror layout was clearer, more iconic, and felt more like the product.
- The newer Home experiments carried more logic, but visually became too busy.
- The right tradeoff is old visual calm plus new under-the-hood learning and passive context.

Implementation consequence:
- the extra overview/coach/timeline cards are no longer the main Home surface
- Home uses a single large mirror card with three block rows
- tapping a row still opens the richer block overlay, so functionality is not lost

## 2026-03-07 - Plus becomes secondary quick add

Decision:
- `+` stays in the shell, but only as a small quick-add entry.

Why:
- The primary product meaning should live in the day track, not in a launcher.
- Quick add is useful, but secondary.

## 2026-03-07 - Lebensrad calibrates, it does not block

Decision:
- `Lebensrad` stays optional and calibrates direction over time.

Why:
- The user should reach value immediately on Home.
- Calibration belongs in the background or as an explicit side path, not as a gate.

## 2026-03-08 - Start is fixed to a tile-only layout

Decision:
- `Start` uses only large tiles in one shared visual system.
- All previous mixed elements on `Start` are removed, not parked.

Why:
- The old Start drifted toward a mixed landing page with too many UI languages at once.
- A fixed tile system makes the surface calmer and easier to scan.
- It creates a stable design baseline that can still be improved iteratively without reopening the whole structure.

Implementation consequence:
- `Start` currently contains exactly one primary tile: `Pulse`
- new Start information must be introduced as another tile, not as a ribbon, CTA row, stats strip, or explainer block
- iteration should optimize the fixed tile language, spacing, density, and meaning, but not reintroduce heterogeneous sections

Copy consequence:
- `Start` should avoid vague system phrasing and interpretive coaching language
- phrases like status summaries or motivational focus lines should not carry the UI
- the surface should read through form and flow first, text second

## 2026-03-08 - One shared tile family across all modes

Decision:
- `Days` uses one shared tile family across `Start`, `Single`, and later `Multi`.
- The modes may differ in layout, but not in surface language.

Why:
- `Start` already shows the clearest visual system in the project.
- `Single` has stronger product meaning, but still lacks a formal container system.
- Material 3 guidance supports using a coherent card language with adaptive pane changes instead of inventing a separate screen grammar per mode.

Implementation consequence:
- extract tile primitives and tile tokens from the current `Start` implementation
- standardize shape and spacing scales
- treat `Single` Home as a hero tile inside the same design system, not as a separate UI species
- build `Multi` from the same tile family from the start

Reference:
- see `docs/tile-system.md`

## 2026-03-08 - Domain detail uses one info tile and one work tile

Decision:
- A phone-sized domain detail screen uses exactly two primary containers: one info tile on top and one work tile below.
- Goals, today's values, sources, and patterns belong inside the one work tile instead of being split into several sibling cards.
- Generic UI copy such as `aktiv`, `1 Ziele`, `1 Heute`, or internal taxonomy like `Core` must not appear unless it directly changes a user decision.

Why:
- The user already chose the domain. Repeating weak labels and internal categories adds noise instead of orientation.
- Stacked edit cards make one job feel like multiple screens.
- The edit surface should explain itself through structure, fields, iconography, spacing, and one clear work area.

Implementation consequence:
- Keep the top tile orienting and calm.
- Merge domain editing into one structured work tile without inner stat cards or mini-cards.
- Keep `Soll`, `Ist`, `Signale`, and editing as one continuous reading flow inside that tile.
- Remove chips, labels, and counters that do not change what the user does next.

Working consequence:
- UX review is not complete until vague copy, duplicate containers, and internal wording are challenged.
- Design docs should record these copy cuts explicitly so the same weak labels do not return in later iterations.

## 2026-03-08 - Release pulse is six domains, not the full catalog

Decision:
- The first release pulse is exactly six active domains:
  - `Schlaf`
  - `Bewegung`
  - `Hydration`
  - `Ernaehrung`
  - `Fokus`
  - `Stress`
- Placeholder domains stay in the catalog, but not in the primary release pulse until they have real metrics, sources, and edit value.

Why:
- A release domain must be more than a label. It needs a goal, an `Ist`, a source story, and a believable correction path.
- `Stress` is the only additional domain already backed by a live passive signal in the current app through notification load.
- Promoting broader placeholders now would inflate the surface with areas that still collapse into generic manual fields or dead detail screens.

Implementation consequence:
- `Start` pulse shows six release-ready domains.
- Existing installs must receive missing release goals and refreshed catalog metadata during seed upgrade.
- Release review should reject any domain that lacks a real metric-to-surface chain.

## 2026-03-08 - Start stays general, pulse gets specific only after tap

Decision:
- The first screen stays general and reads as a calm overview of today.
- `Start` should not read like a six-item domain catalog even when six release domains exist internally.

Why:
- The first screen is orientation, not taxonomy.
- Leading with named health domains too early makes the product feel narrower and more technical than it is.
- Better release products use progressive disclosure: summary first, specific area second.

Implementation consequence:
- `Start` shows one general `Pulse` tile with a short day summary and quiet signals.
- Domain names are not the first visible read on `Start`; they remain available through tap targets and semantics.
- Concrete `Soll`/`Ist` editing stays inside the domain detail screen, not on the first surface.

## 2026-03-08 - Start is now a fixed 16-area map, not a day-status summary

Decision:
- `Start` no longer uses `Heute`, `stabil`, `brauchen Blick`, or `offen` as its first read.
- `Start` shows one fixed `Pulse` tile with 16 always-visible areas in a 4x4 grid.
- The `+` dock remains, but the old production tools are removed from it. It now opens setup/model actions: `Fingerprint`, `Domaenen`, `Quellen`, `Research`.

Why:
- The first screen should show the full product landscape, not a temporary daily verdict.
- Area titles must be stable, always visible, and readable below the icon surface.
- The old dock actions came from an earlier tool-hub phase and no longer match the new role of `Start`.

Implementation consequence:
- `Start` is driven by the life-area/fingerprint model instead of the six daily domains.
- The default area catalog expands to 16 areas and seed upgrades must backfill missing areas on existing installs.
- Area taps currently route into fingerprint calibration until deeper area-specific content is defined.
