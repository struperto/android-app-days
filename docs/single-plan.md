# Single Plan

This file is the active source of truth for the `Single` mode.

## Product role

`Single` is the calm daily reading surface of `Days`.

It proves value.

It is not:
- a settings hub
- a dashboard full of widgets
- a control room

## Core shape

`Single` remains the mode name.

Its current internal product meaning is:
- one overall `Bereichsfeed`
- one feed across areas
- one card is one visible output from one area

## Feed rules

- the feed is prioritized, not only chronological
- each card should span the full available width on mobile
- cards should stay visually consistent in size and rhythm
- the user primarily reads, not acts

For now:
- reading first
- no action-heavy surfaces
- no mini-app behavior inside cards
- reading a feed item must not auto-archive it just because the user reaches the end

## Output contract

Each area should eventually produce one simple visible output for `Single`.

Examples:
- news items
- unread or unanswered communication
- writing reminders
- interesting selected posts
- creative or surprising daily impulses

The first implementation target is still simpler:
- straightforward information cards
- readable feed items
- clear provenance by area

## Initial card families

The first useful preview and feed families are:
- `Info-Kachel`
- `Leseliste-Kachel`
- `Offen/Unbeantwortet-Kachel`
- `Impuls/Ueberraschung-Kachel`

At the beginning these are still simple reading cards, not mini workflows.

## Personal learning

`Profil` learns first from `Single`.

Initial learning inputs:
- what the user reads
- what is skipped
- what gets revisited
- explicit lightweight feedback when added later

Do not make the profile manually editable yet.

## Prioritization

Area logic should eventually decide:
- what appears
- in what order
- with what degree of confidence or weight

The first version still needs a conservative prioritization path.

Current direction:
- priority comes from area logic first
- chronology is only the fallback
- the feed stays understandable even when ranking becomes smarter

## Design rules

- calm
- monolithic
- mostly gray and restrained
- consistent cards
- low visual noise
- scrolling feed over dashboard fragments

## Current implementation priorities

1. Align `Single` wording with `Bereichsfeed`.
2. Keep the surface read-first.
3. Move toward one full-width prioritized feed across areas.
4. Keep room for later profile learning without exposing that complexity yet.
5. Keep card families simple enough that preview and runtime can share the same language.
