# Single V1

## Product principle

Days starts as a calm, private, single-user Android app.

Core idea:
- The phone thinks with me.
- The app stays useful without internet.
- AI supports the flow, but does not take over the product.

## V1 goal

Build one real, daily-usable `Single` flow before expanding to `Multi` or `Assist` as full modes.

V1 should be good at:
- capturing thoughts quickly
- turning captures into active items
- planning today
- reflecting that state back on Home

## Core flow

Primary V1 flow:
1. `Home`
2. `Capture`
3. `Vorhaben`
4. `Plan`
5. back to `Home`

`Create` can exist as a helper action, but should not become a separate product world in V1.

## Home contract

Home is the visual center of `Single`.

Layout contract:
- top left: mode picker pill
- top right: settings
- center: dashboard with low text density
- bottom left: `+` for core actions
- bottom right: `Assist`

Interaction contract:
- `+` opens the main action sheet
- `Assist` opens local AI help
- back closes open sheets first
- the dashboard remains the anchor of the screen

## Dock / action scope

The `+` sheet in V1 should stay small.

Keep:
- `Capture`
- `Vorhaben`
- `Plan`
- `Create`

Do not bring back the full old dock taxonomy in V1.

## AI in V1

AI starts as a small local layer, not as a full AI control center.

Allowed first-use cases:
- summarize text capture
- OCR text from image capture
- suggest next step from a capture

Not part of V1:
- notification decision system
- full Operator workflow
- WebView automation
- large AI lab surface
- multi-agent or coworker behavior

## Data scope

Keep the first data model minimal:
- `CaptureItem`
- `Vorhaben`
- `PlanItem`
- small `AssistSuggestion`

Anything beyond that must justify itself against the core flow.

## Design principles

- low text density
- icon-led actions
- dashboard stays central
- calm, clear layout
- minimal competing entry points

## Explicitly out of scope

For the first usable `Single` release, these stay out:
- `Multi`
- `Assist` as a full mode
- `Coworker`
- BLE
- notification listener workflows
- live device ingestion matrix
- podcast pipeline
- full voice system
- dock customization
- experimental labs

