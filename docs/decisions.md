# Decisions

This file keeps only active product and architecture decisions.

## 2026-03-13 - Product chain is the top-level source of truth

Decision:
- Read the product through this chain:
  - `Quelle -> Signal -> Bereichslogik -> Bereichsfeed in Single -> Profillernen -> Multi`

Consequence:
- Think in signals, not in app clones.
- New features must map back to this chain.
- `Multi` must not become the dependency for first user value.

## 2026-03-13 - `Start` is the orchestrator and workbench

Decision:
- `Start` is not a passive home surface.
- It owns area creation, analysis, blocker handling, source selection, goal shaping, and output preview.

Consequence:
- The create flow becomes a real multi-screen pipeline.
- Analysis is visible and mandatory.
- `Start` must feel like the place where the engine starts, not like decorative overview copy.

## 2026-03-13 - Area creation requires analysis, blockers, and preview

Decision:
- The active target flow is:
  - `Freitext -> Analyse -> Blocker / Questions -> Output-Vorschau -> Bereich anlegen`
- Analysis may block hard.

Consequence:
- Blockers must be explicit and specific.
- When possible, blockers should offer a `Loesung finden` path.
- Different analysis outcomes may branch into different follow-up screens.

## 2026-03-13 - `Bereich` is an editable logic surface, not just a category

Decision:
- A `Bereich` is treated as a surface with its own intent, rules, goals, inputs, outputs, and evaluation.
- The exact minimal object contract is still under evaluation and must be validated by examples and runs.

Consequence:
- Area design must stay flexible enough for several goals, not only one.
- The proving examples are currently:
  - `News`
  - `Freunde/Nachrichten`
  - `App-Bau`

## 2026-03-13 - `Single` stays the mode name and becomes the calm `Bereichsfeed`

Decision:
- Keep the visible mode name `Single`.
- Product meaning shifts to one calm, prioritized `Bereichsfeed` across areas.
- Stop using `Bereichsinfo` as active product language.

Consequence:
- The main pattern is a feed of cards.
- Cards are read-first, not action-first.
- Each card should clearly represent one output from one area.

## 2026-03-13 - `Single` is the first profile-learning surface

Decision:
- `Profil` first learns only from what the user actually reads or ignores in `Single`.
- Profile editing is not a first-phase feature.

Consequence:
- `Single` must preserve readable provenance and stable feed structure.
- Profiling logic should not make the visible feed feel complicated.

## 2026-03-13 - Feed ordering in `Single` is prioritized by area logic

Decision:
- The `Single` feed should not stay purely chronological.
- Area logic should decide what appears and how it is weighted.

Consequence:
- Each area needs a clearer output contract.
- The feed should prefer full-width mobile cards over dashboard fragments.

## 2026-03-13 - `Multi` stays visible but secondary

Decision:
- `Multi` remains visible as a product direction.
- It is allowed to be framed as a future resonance layer.
- It stays secondary until `Start` and `Single` prove strong value.

Consequence:
- Keep `Multi` compile-safe and conceptually clear.
- Do not build fake depth or creepiness into the current implementation.

## 2026-03-13 - `Settings` stay slim and status-oriented

Decision:
- `Settings` should act as a calm system shell, not a second product universe.
- The preferred pattern is:
  - left `setting name`
  - right `current status`

Consequence:
- Move toward simple list or menu rows.
- Keep area logic primarily inside `Bereich`, not in a parallel settings labyrinth.

## 2026-03-13 - Local processing remains the default path

Decision:
- Go as far as possible with local collection, clustering, conservative condensation, and indexing before assuming cloud dependence.

Consequence:
- Source class, execution class, and storage class should stay separable in the architecture.
- Summaries must stay conservative and close to visible material.

## 2026-03-13 - The `News` proving run starts broad, not narrow

Decision:
- The first `News` run should not ask the user to narrow too early.
- Analysis should first inspect which possible sources and signals are available and relevant on the device.
- Ignoring or excluding sources may be prepared structurally, but should not dominate the first run.
- Browser and web-based paths should be prioritized first in the technical discovery.
- Screenshots should be listed as a valid candidate path.
- The analysis output should stay simple and operational: source name, link or path hint, status, optional repair action.

Consequence:
- `Start` should first discover and propose candidate `News` signal paths instead of forcing an early source choice.
- Emulator and device testing should include both positive and negative `News` examples.
- The initial card concept for `News` is a fixed-size, read-first card with text or image as the primary visible output.

## 2026-03-14 - `News` source setup stays general first and carries explicit V1 limits

Decision:
- `News` does not start with brand-wired sources.
- The first selectable source families are:
  - `Web`
  - `Feeds`
  - `Social Text`
  - `Social Bild`
  - `Video`
  - `Screenshots`
- Concrete brands like `FAZ`, `stol.it`, `X`, `Instagram`, and `YouTube` belong to the setup or later runtime layer, not the first selection layer.

Consequence:
- V1 should stay honest about limits:
  - `Web`: single public links and readable websites
  - `Feeds`: public RSS/Atom or feed-discoverable sites
  - `Social Text`: selected posts via share or link, not full timelines
  - `Social Bild`: selected posts via share, link, or screenshot, not full account import
  - `Video`: selected YouTube links or shares first, not full subscription sync
  - `Screenshots`: local image import first, OCR and extraction only conservatively
- When a source family is chosen, only that family gets a setup screen.
- Inside an existing area, `Aktueller Status` should list what is already added.
- `Aktueller Status` may expose a small `+` jump into `Hinzufuegen`.
- Source detail screens must stay additive, especially for multiple feeds in one `News` area.
