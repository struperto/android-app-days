# Area System Workflow

Use the templates in this order. Do not start area-specific app code before these steps are filled.

## Recommended order
1. Fill [Area Definition Template](./area-definition-template.md).
2. Fill [Wireframe Planning Template](./wireframe-planning-template.md).
3. Fill [Implementation Mapping Template](./implementation-mapping-template.md).
4. Review MVP scope, open questions, and non-goals.
5. Only then create wireframes, tasks, and implementation prompts.

## Why this order
- `Area Definition` decides what the area is.
- `Wireframe Planning` decides how the area is read and operated.
- `Implementation Mapping` decides what must actually be built in code, state, and persistence.

If these three truths are mixed too early, the result is usually:
- special-case UI
- unclear panel semantics
- persistence that does not match product behavior
- rework when new areas are added later

## Minimum gate before implementation
Every new area should at least have:
- identity and purpose
- `Lage` model
- `Richtung` model
- `Quellen` model
- `Flow` model
- tile behavior
- area home behavior
- persistence notes
- MVP scope

## Recommended pilot areas
Test the generic area system first with these five areas:
- `Vitalitaet`
  Reason: mixes status, habits, and possible local signals.
- `Fokus`
  Reason: makes `Richtung` and next-step clarity easy to judge.
- `Erholung`
  Reason: tests a softer area that should not pretend to be pure telemetry.
- `Lernen`
  Reason: tests progress, cadence, and source material logic.
- `Freundschaft`
  Reason: tests a social area that is less metric-heavy and more reflective.

Why these first:
- they cover signal, plan, and reflection patterns
- they stress all four panels in different ways
- they expose whether the generic system can handle both measurable and softer areas
