# Single V1

## Product principle

Days starts as a calm, private, single-user Android app.

Core idea:
- the phone thinks with me
- the app stays useful without internet
- the first visible value is the shape of today

Core product sentence:
- `Days verwandelt Gedanken, Signale und Ziele in einen klaren Tag.`

## V1 goal

Build one real, daily-usable `Single` flow before expanding to `Multi` or `Assist`.

V1 should be good at:
- showing whether the current day matches the personal `Soll`
- showing drift per time block
- letting the user correct the day from `Home`
- turning small local inputs into visible change on the mirror dashboard

## Core flow

Primary V1 flow:
1. `Home`
2. tap a block
3. inspect `Soll`, `Ist`, reasons, and items
4. act directly in the block
5. back to `Home`
6. see the day track change

The old slices `Erfassen`, `Vorhaben`, `Plan`, and `Werkbank` still exist in code where needed, but are no longer the primary visible product path.

## Release domain set

The first useful release carries exactly these active pulse domains:
- `Schlaf`
- `Bewegung`
- `Hydration`
- `Ernaehrung`
- `Fokus`
- `Stress`

Reason:
- each of these already has a goal path, an `Ist` path, and at least one believable source path
- `Stress` qualifies because notification load is already wired as a passive signal
- the remaining catalog domains stay parked until they are more than placeholders

## Home contract

Home is the visual and functional center of `Single`.

Layout contract:
- top left: mode picker pill
- top right: settings
- center: a calm `Heute · Datum` mirror dashboard
- bottom left: a small `+` quick add
- the main day rhythm uses three blocks:
  - `Vormittag`
  - `Nachmittag`
  - `Abend`

Interaction contract:
- the mirror dashboard is text-light and visual
- each row compares left `Soll` against right `Ist`
- block tap opens a light overlay
- the overlay shows:
  - focus
  - `Soll`
  - `Ist`
  - drift reasons
  - block items
  - direct actions
- the dashboard header exposes:
  - `Tag anpassen`
- `Lebensrad` is optional calibration, not a start gate

## Soll / Ist in V1

`Soll` comes from:
- basis rhythm
- today plans
- open pressure
- optional life area calibration
- short local learning from recent plan history

Internally, `Soll v1` is now shaped by three soft local axes:
- `Fokusdruck`
- `Stoeranfaelligkeit`
- `Energiebedarf`

These axes are not a visible quiz or profile picker.
They are inferred locally and only change how the three day blocks behave:
- how much protection a block wants
- how much interruption a block can absorb
- how light the evening or overloaded parts of the day should stay

`Ist` comes from:
- done items
- open day items
- new captures and imports
- carryover pressure

The first passive context source already active in `Single` is:
- yesterday's still-open plan carryover

The first external passive context source active in `Single` is:
- calendar events, when Android calendar access is available

The next passive source now active in `Single` is:
- notification listener signals, when Android notification listener access is enabled

This means Home can show a heavier morning or a softer evening even before broader passive signals exist, simply because the previous day was not really closed.

The dashboard itself stays light:
- no visible `Soll` / `Ist` labels in the graph
- no repeated `0-5` self-ratings
- no setup gate before first use
- the strong visual language comes from the bilateral mirror rows, not from long text cards

## Tool scope

Visible product tools stay minimal:
- `Home`
- `+` quick add
- block overlays
- `Settings`

Secondary only:
- `Werkbank`
- separate `Erfassen`
- separate `Vorhaben`
- separate `Plan`

Planned later:
- voice
- broader passive signals
- deeper local assist
- finer graph modes and granularity

## AI in V1

AI stays local and secondary.

Allowed first-use cases:
- later support for better block suggestions
- later support for signal interpretation
- later support for local summarization

Not part of this Home-first V1:
- cloud AI
- external services
- visible AI mode
- operator or web automation

## Explicitly out of scope

For the first useful `Single` release, these stay out:
- `Multi`
- `Assist` as a full mode
- `Coworker`
- BLE
- a broad passive-signal wall
- synthetic users and large simulation runs
