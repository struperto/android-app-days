# Start Plan

This file is the active source of truth for the `Start` mode and the area creation flow.

## Product role

`Start` is the orchestrator and workbench of `Days`.

It must answer a simple user question:

`How do I create a useful area that turns signals into clear output?`

## Target flow

The target creation flow is:

`Freitext -> Analyse -> Quellen waehlen -> Setup je Quelle -> Blocker / Questions -> Output-Vorschau -> Bereich anlegen`

Rules:
- analysis is mandatory
- source choice is mandatory when the analysis exposes relevant source families
- analysis may block hard
- blockers must be visible and specific
- blockers should expose a `Loesung finden` path when possible
- every major creation phase gets its own screen

## Create flow contract

### 1. Freitext

The user starts with one free text field.

The prompt should focus on:
- what the area is for
- what the goal is
- which source or signal matters

### 2. Analyse

The system should visibly work through multiple steps.

Examples:
- reading the intent
- detecting potential signal classes
- checking device capability
- checking permissions or missing connectors
- forming draft outputs

The flow should not feel instant-fake.

### 3. Blockers and questions

Use dedicated screens for blockers and open questions.

Blocker types should eventually include:
- source missing
- permission missing
- goal unclear
- output not derivable
- device cannot support the intended flow

Questions should exist to reduce ambiguity, not to add ceremony.

### 3a. Quellen waehlen

When an area can work with multiple source families, the flow should not guess too much.

The user should first choose which source families matter.

Rules:
- keep names general
- state on the right should stay calm: `offen` or `gewaehlt`
- only selected sources spawn later setup screens
- source names should describe types, not brands, unless the user already named a concrete source

Example for plain `News`:
- `Web`
- `Feeds`
- `Social`
- `Screenshots`

Example for concrete `News`:
- `X`
- `Instagram`
- `FAZ`
- `stol.it`
- `Screenshots`

### 3b. Setup je Quelle

After source selection, each chosen source gets its own short setup screen.

These setup screens should stay minimal:
- one clear title
- one text field or one compact list
- no long intro prose

The setup must only ask what is needed for the chosen source.

### 4. Output preview

The create flow should show multiple example cards.

Initial default preview set:
- `Info-Kachel`
- `Leseliste-Kachel`
- `Offen/Unbeantwortet-Kachel`
- `Impuls/Ueberraschung-Kachel`

The user may select a preferred output shape.
That preference may still be changed later.

### 5. Bereich anlegen

Only after analysis, blockers, and preview does the area get created.

The new area should then open into the editable `Bereich` work surface.

## Area editing role

After creation:
- `Start` remains orchestration and overview
- `Bereich` becomes the edit surface

`Bereich` should own:
- source attachment
- rules
- goal tuning
- output shaping
- re-analysis when needed

## Design rules for Start

- one strong overview
- clear tiles
- minimal helper prose
- on mobile one clear heading is enough; stacked heading plus intro plus section heading is usually too much
- avoid explanation text when structure, status, and placement can do the job
- in `Aktueller Status`, `Hinzufuegen`, `Sortieren`, and `Im Feed`, prefer direct menu structure over intro cards
- in first-level and second-level menu lists, prefer `Name links / Wert rechts`; helper copy only when the state would otherwise stay ambiguous
- no settings-heavy first impression
- no fake dashboard complexity
- analysis and blocker states must feel deliberate and trustworthy
- in `Analyse`, avoid a second big inner heading; once the check is done, show one compact result card with title, status, and source rows
- preview and finalization screens should combine configuration and visible result tightly instead of stacking several intro blocks
- inside a created area, analysis runs quietly in the background and only stays reachable via the icon
- for `News / Medium`, `Hinzufuegen` should expose fast technical entry for links, feeds, social imports, video links, and screenshots

## Panel schema rule

`Aktueller Status`, `Hinzufuegen`, `Sortieren`, and `Im Feed` stay globally stable.

The rows inside them are not globally fixed.
They must come from a small typed schema per area family or template.

### Semantic contract for all families

The current weakness is not mostly styling, but mixed semantics.
Each first-level panel must own one clear kind of information.

- `Aktueller Status`
  read-only facts about the current state of the area
  this is not the place for setup or interpretation-heavy controls
  when useful, this screen may expose a small `+` to jump directly into `Hinzufuegen`
- `Hinzufuegen`
  real input paths, permissions, connectors, and source-specific setup
  this is usually the strongest operational panel
  source-specific add screens must stay additive: existing items remain visible and more than one item per source family must be possible
- `Sortieren`
  what the area prefers, ranks, or keeps in front
  this panel should describe selection and ordering, not low-level system knobs
- `Im Feed`
  rhythm, revisit behavior, sync timing, and optional extras
  this panel should answer when and how the area comes back

Rules:
- no abstract filler rows that could fit every family equally badly
- no duplicate semantics across two panels
- avoid showing internal model words when the user expects product meaning
- a value like `3/5` alone is rarely enough; it should either be named clearly or replaced by a stronger word
- `Aktueller Status` should stay calm and mostly read-only
- `Hinzufuegen` may branch into richer setup screens
- `Sortieren` and `Im Feed` need the strongest family-specific wording

The first active schema families are:
- `News / Medium`
- `Person / Kontakt`
- `Projekt / App-Bau`
- `Ort / Wege`
- `Gesundheit / Ritual`
- `Inbox / Sammlung`

Everything else may fall back to a simpler generic schema until it earns its own variant.

### `News / Medium`

First-level rows are:
- `Aktueller Status`: `Status`, `Feeds`, `Web`, `Social Text`, `Social Bild`, `Video`, `Screenshots`, `Letzter Fund`
- `Hinzufuegen`: `Web`, `Feeds`, `Social Text`, `Social Bild`, `Video`, `Screenshots`
- `Sortieren`: `Zuerst`, `Mitlesen`, `Sortierung`, `Zeitraum`
- `Im Feed`: `Stil`, `Dichte`, `Nachladen`, `Wiederkehr`

Current proving slice:
- `X` and `Instagram` are manual-first via share, link, or screenshot
- `YouTube` is manual-first via share or link and only later a channel or playlist source
- `FAZ` and `stol.it` are web/feed-first running sources
- the screen should expose these concrete sources directly instead of generic `Browser` or `Feeds`

### `News / Medium` source matrix

- `Web`
  V1 input: direct `http(s)` article or website link
  formats and tech: public `http(s)` URLs, Android share target, app links
  current limit: no login-only pages, paywalled parsing may fail, one link import at a time
- `Feeds`
  V1 input: RSS/Atom URL or website that exposes a feed
  formats and tech: RSS/Atom URL first, public website URL as fallback source, multiple URLs per area
  current limit: public feeds only, no authenticated newsletters, no OPML import, no custom parser chains
- `Social Text`
  V1 input: share or direct link to selected posts
  formats and tech: public post URLs and Android share intents
  current limit: no full timeline sync, no background crawl of installed apps, no private account data
- `Social Bild`
  V1 input: share, direct link, or screenshot
  formats and tech: public post URLs, Android share, screenshot fallback
  current limit: no automatic account import, no private app data access
- `Video`
  V1 input: YouTube link or share, later channel or playlist URL
  formats and tech: YouTube watch URL, share URL, later channel or playlist URL
  current limit: no autoplay transcript pipeline, no full subscription sync, no private watch history
- `Screenshots`
  V1 input: local image import
  formats and tech: Android Photo Picker or direct image import
  current limit: image-first only, OCR and deeper extraction stay conservative
- `Benachrichtigungen`
  V1 input: notification listener later, not default for News
  formats and tech: Android notification listener service
  current limit: opt-in only, headline-level signals first
- `Dateien`
  V1 input: local document import later
  formats and tech: Storage Access Framework for text, html, pdf, export files
  current limit: not part of the default News setup yet

### `News / Medium` implementation direction

- `Web`
  implementation: Android share target plus direct link input
- `Feeds`
  implementation: RSS/Atom URL, plus feed discovery from websites where possible; multiple feed URLs per area must stay visible and removable in one place
- `Social Text`
  implementation: share target and direct post URL first
- `Social Bild`
  implementation: share target, post URL, or screenshot first
- `Video`
  implementation: YouTube share or URL first, later channel or playlist URL
- `Screenshots`
  implementation: photo picker import first

### `Person / Kontakt`

First-level rows are:
- `Stand`: `Status`, `Letzter Kontakt`, `Offen`
- `Eingang`: `Messenger`, `Notizen`, `Screenshots`, `Bestand`
- `Fokus`: `Personen`, `Auswahl`, `Antworten`, `Naehe`
- `Takt`: `Rhythmus`, `Rueckkehr`, `Signale`, `Extras`

### `Projekt / App-Bau`

First-level rows are:
- `Stand`: `Status`, `Naechster Zug`, `Offen`
- `Eingang`: `Verbindung`, `Material`, `Dateien`, `Screenshots`, `Bestand`
- `Fokus`: `Vorne`, `Auswahl`, `Ordnung`, `Horizont`
- `Takt`: `Rhythmus`, `Zugkraft`, `Wiedervorlage`, `Extras`

### `Ort / Wege`

First-level rows are:
- `Stand`: `Status`, `Letzter Ort`, `Aktiv`
- `Eingang`: `Standort`, `Wege`, `Orte`, `Screenshots`, `Bestand`
- `Fokus`: `Orte`, `Auswahl`, `Ausloeser`, `Zeitraum`
- `Takt`: `Rhythmus`, `Wiederkehr`, `Ortssignal`, `Extras`

### `Gesundheit / Ritual`

First-level rows are:
- `Stand`: `Status`, `Letztes Signal`, `Trend`
- `Eingang`: `Health`, `Notizen`, `Screenshots`, `Bestand`
- `Fokus`: `Vorne`, `Auswahl`, `Deutung`, `Verlauf`
- `Takt`: `Rhythmus`, `Dichte`, `Messung`, `Extras`

### `Inbox / Sammlung`

First-level rows are:
- `Stand`: `Status`, `Letzter Fang`, `Offen`
- `Eingang`: `Links`, `Notizen`, `Dateien`, `Screenshots`, `Bestand`
- `Fokus`: `Vorne`, `Auswahl`, `Ordnung`, `Dauer`
- `Takt`: `Rhythmus`, `Rueckholen`, `Wiedervorlage`, `Extras`

## Signal-source direction

`Start` must think in signals, not in full app integrations.

Reference signal sets will be defined for:
- `News`
- `Freunde/Nachrichten`
- `App-Bau`

Each proving area should eventually get five candidate sources.

## Reference signal sets

These sets are not final product promises.
They are the current proving slices for architecture and emulator-driven implementation.

### `News`

Candidate sources:
- shared article links
- RSS or Atom feeds
- browser share intents
- screenshots of articles or timelines
- notification headlines from selected apps

### `Freunde/Nachrichten`

Candidate sources:
- notifications from messengers
- shared conversation screenshots
- contact-triggered manual notes
- pending reply captures from share or text input
- calendar-linked communication reminders

### `App-Bau`

Candidate sources:
- GitHub or issue links shared into the app
- screenshots from emulator or device
- notes or text snippets from development work
- calendar blocks related to build or release work
- notifications from coding or task apps

## Blocker model

The first active blocker taxonomy is:
- `Quelle fehlt`
- `Rechte fehlen`
- `Ziel unklar`
- `Output nicht ableitbar`
- `Geraet kann den Flow nicht tragen`

Each blocker screen should try to provide:
- a clear reason
- the affected source or intent
- one recommended repair path
- one fallback path when repair is not possible

## Question model

Questions are not generic surveys.
They should reduce one concrete ambiguity in the flow.

Typical question classes:
- Which source matters most?
- What should the area ignore?
- Which output shape is preferred?
- Is the goal reading, tracking, reminding, or filtering?

## Create-flow phases

### Phase 1

- freitext entry
- visible staged analysis
- source-family selection when relevant
- one setup step per selected source
- blocker or question handoff
- preview with four sample cards
- area creation only after a valid path exists

### Phase 2

- stronger source checks per reference area
- better blocker-specific `Loesung finden` flows
- initial learning from chosen preview shape

### Phase 3

- re-analysis when key area inputs change
- deeper device-aware capability guidance
- reusable question engine across areas

## Current implementation priorities

1. Rebuild the creation flow into a clean multi-screen pipeline.
2. Make analysis states, blockers, and questions explicit.
3. Add output preview cards and selection.
4. Tighten the `Bereich` edit surface around area logic.
5. Ensure the flow stays useful even before every connector exists.

## Active test and interview loop

The create flow must not be evaluated with empty states only.

Use this loop for every serious iteration:

1. enter one concrete example sentence
2. run the flow screen by screen
3. inspect the result as user and as system
4. note confusion, blockers, and missing inferences
5. adjust UI or logic
6. rebuild, reinstall, and test again on emulator or device

The current proving method is:
- roleplay the user intent
- roleplay the system analysis
- inspect whether the screen helps or gets in the way
- convert each friction into code or wording changes

## Current reference runs

These runs are the active first slice for product and flow testing.

### `News`

Seed input:
- `Ich will News ruhig lesen und nur das Wichtige im Feed sehen.`

Current findings:
- do not narrow too early
- first inspect all relevant available sources and signals
- Chrome and similar browser behavior should be considered if locally discoverable
- web and browser-based signal paths are currently the highest-priority technical slice
- positive and negative examples must be prepared on emulator and device
- the first visible output should stay fixed-size and read-first, using text or image
- screenshots should be treated as a technical candidate source as well
- the analysis result should stay simple: source name, path/link hint, status, and when needed one repair or enable action

Interview questions:
- What counts as news here?
- What should be ignored?
- Is the goal reading, tracking, or acting?
- Which sources should be allowed first?
- What should one good feed card look like?

### `Freunde/Nachrichten`

Seed input:
- `Ich will offene Nachrichten von Freunden sehen und nichts Wichtiges uebersehen.`

Interview questions:
- Which people matter first?
- What counts as unanswered?
- Should the area only show open loops or also warm signals?
- When is a reminder useful and when is it annoying?
- What should one good feed card show first?

### `App-Bau`

Seed input:
- `Ich will App-Bau-Material sammeln und den naechsten sinnvollen Zug sehen.`

Interview questions:
- Which materials count first?
- What should become visible in the feed?
- What is noise in this area?
- Is the goal progress, overview, or prioritization?
- What should one good feed card contain?
