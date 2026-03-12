# Start V1

`Start` is the life-area mode of `Days`.

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

`Start` uses an adaptive grid overview with user-created areas.
Areas are fully user-creatable via a guided Create-Flow.

Rules:
- only large tiles
- no KPI strips outside tiles
- no button rows as the main pattern
- no helper prose on first read
- area titles sit below or clearly separated from the icon surface

## Area model

Each area is representable with:
- stable `areaId`
- title and summary
- icon (`iconKey`) and template (`templateId`)
- `behaviorClass` (TRACKING, PROGRESS, RELATIONSHIP, MAINTENANCE, PROTECTION, REFLECTION)
- `tileDisplayMode` (AMPEL, SCORE, SHORT_TEXT, LATEST_NAME, COUNT, TREND)
- `familyKey` (Radar, Pflicht, Routine, Kontakt, Gesundheit, Ort, Sammlung)
- bound skills (`Set<AreaSkillKind>`)
- current snapshot (manualScore, manualStateKey, manualNote)
- authoring config (lageMode, directionMode, sourcesMode, flowProfile, etc.)

Areas are fully user-editable and user-creatable.

## Skill system

Each area can bind any number of **Skills** from a fixed registry of 12:

| Skill | Key | Permission | Data source |
|-------|-----|-----------|-------------|
| Gesundheit | `health_tracking` | Health Connect | HealthConnectRepository |
| Kalender | `calendar_watch` | READ_CALENDAR | CalendarSignalRepository |
| Benachrichtigungen | `notification_filter` | Notification Listener | NotificationSignalRepository |
| Manuell | `manual_log` | none | local |
| Screenshots | `screenshot_reader` | READ_MEDIA_IMAGES | ScreenshotRepository |
| Fotos | `photo_stream` | READ_MEDIA_IMAGES | PhotoStreamRepository |
| Kontakte | `contact_watch` | READ_CONTACTS | ContactWatchRepository |
| App-Nutzung | `app_usage` | PACKAGE_USAGE_STATS | AppUsageRepository |
| Website | `website_reader` | none | WebsiteReaderRepository |
| Podcast | `podcast_follow` | none | PodcastFollowRepository |
| Standort | `location_context` | ACCESS_FINE_LOCATION | LocationContextRepository |
| Checkliste | `checklist` | none | ChecklistRepository |

Skills that map to a legacy `DataSourceKind` (health, calendar, notifications, manual) also auto-sync the legacy `area_source_bindings` table via a bridge in `RoomAreaSkillBindingRepository`.

Skills requiring config (Website, Podcast, Location) store their configuration in `configJson` on the `area_skill_bindings` table.

## Create-Flow

The Create-Flow has three surfaces:

1. **CreateCapture** — Input kind (Text, Link, Screenshot, App, Kontakt, Ort) + free text + auto-generated suggestions
2. **CreateConfirm** — Title, meaning, behavior class, skill selection (FlowRow chips for all 12 skills), identity link
3. **CreateOptions** — Template and icon selection

`inferPrimaryIntent()` maps German keywords to skill sets:
- "kalender"/"termin" -> CALENDAR_WATCH
- "screenshot"/"bild" -> SCREENSHOT_READER
- "podcast"/"feed" -> PODCAST_FOLLOW
- "nachricht"/"kontakt" -> NOTIFICATION_FILTER + CONTACT_WATCH
- "gesund"/"schlaf" -> HEALTH_TRACKING
- "ort"/"zuhause" -> LOCATION_CONTEXT
- Link input -> WEBSITE_READER

After area creation, `StartViewModel.createArea()` binds all selected skills via `areaSkillBindingRepository.bind()`.

## Tile rendering

Tiles show family-colored gradients with:
- icon + family label (top row)
- title + short status (middle)
- action label + arrow (bottom)

`tileDisplayMode` controls the status display (currently resolved via `startAreaTileStatusLine()`; per-mode rendering is scaffolded but not yet visually differentiated).

`familyKey` is resolved from DB first, with keyword-heuristic fallback for older areas.

## Detail contract (AreaStudio)

Opening an area shows the AreaStudio with four panels:
- **Blick/Stand** (Snapshot) — today's state, manual score, manual note
- **Fokus/Naechster Zug** (Path) — direction, focus track
- **Quellen/Signale** (Sources) — bound skills and source setup
- **Takt/Rhythmus** (Options) — cadence, flow profile, reminders

Panel labels adapt per family.

## Database

- `area_instances` — core area data (v15: +tileDisplayMode, +familyKey columns)
- `area_skill_bindings` — skill-to-area bindings (new in v15)
- `checklist_items` — per-area checklists (new in v15)
- `area_source_bindings` — legacy source bindings (kept in sync via bridge)

Migration v14->v15 creates new tables and migrates existing source bindings to skill bindings.

## Non-goals

Do not block `Start` on:
- network sync
- BLE or Wi-Fi orchestration
- complex automation stacks
- deep multi-person coordination
