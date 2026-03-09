# Single V1 Contract

## Visible product

`Single V1` shows exactly:
- `Home`
- `Einstellungen`
- optional `Lebensrad` as calibration

Not primary visible product surfaces:
- `Werkbank`
- separate `Erfassen`
- separate `Vorhaben`
- separate `Plan`

These may stay compile-safe in code, but are not the main product language anymore.

## Product promise

`Days verwandelt Gedanken, Signale und Ziele in einen klaren Tag.`

The first visible value is:
- see whether the current day fits the personal `Soll`
- see which block drifts
- make one focused correction directly from `Home`

## Core loop

1. Open `Home`
2. Read the day track
3. Tap `Vormittag`, `Nachmittag`, or `Abend`
4. Inspect `Soll`, `Ist`, reasons, and block items
5. Act:
   - `Jetzt rein`
   - `Später`
   - `Fertig`
   - `verschieben`
6. Return to the day track and see it change

## Home contract

Home stays stable and recognizable.

It contains:
- `Heute · Datum`
- one central day track
- three broad time windows:
  - `Vormittag`
  - `Nachmittag`
  - `Abend`
- one secondary `+` entry for quick add

Home does not:
- explain itself with dense text
- ask for repeated ratings
- require setup before use
- force the user into a tool hub before the day is understandable

## Release pulse contract

The release pulse contains exactly these active domains:
- `Schlaf`
- `Bewegung`
- `Hydration`
- `Ernaehrung`
- `Fokus`
- `Stress`

All other domains may stay visible in settings as future catalog entries, but they are not part of the first release promise.

## Interaction contract

- tap on a time block opens a light overlay directly above `Home`
- back closes the overlay before navigation
- `+` is only a small quick-add entry, not the main value carrier
- share/import returns to `Home`, not to `Werkbank`
- `Lebensrad` calibrates long-term direction, but is not a start gate

## Explicit V1 non-goals

Do not block V1 on:
- voice-first default input
- passive signals as a full UI surface
- visible AI mode
- multi-mode behavior
- synthetic user simulation

Those come after the Home-centered loop feels useful.
