# Single Domain Foundation

This file captures the engineering model behind `Single`. Keep it short and implementation-facing.

## Product core

`Single` is a local 24h operating system for `Soll`, `Ist`, and drift.

It needs to:
- define what a good day should look like
- see what actually happened
- explain the gap between both
- adapt locally over time

## Logical day

The canonical day starts at `06:00` and ends at `05:59` on the next calendar date.

Why:
- sleep and late evening belong to one regulation arc
- the night must stay visible inside the same day model
- planning and correction break if the day resets too early

## Core layers

### 1. Goal layer

Explicit goals define what the app is aiming for.

Each goal should know:
- domain
- cadence
- target type
- unit
- preferred window
- rationale

### 2. Observation layer

Observations are raw `Ist`.

They can come from:
- manual input
- health or sensor data
- calendar
- notifications
- imported local context

Observations stay separate from evaluations.

### 3. Evaluation layer

Evaluations transform observations into user-facing meaning.

They answer:
- on track or drifting
- strong or weak fit
- within the right window or not
- acceptable today or overloaded today

### 4. Projection layer

The dashboard is a projection of:
- goals
- observations
- evaluations
- local notes
- drift reasons

The visible blocks are not the source of truth. They are the clearest reading of the current day.

### 5. Learning layer

The first learning path is local and explainable.

Learn from:
- repeated corrections
- timing shifts
- carryover pressure
- calendar structure
- notification pressure
- explicit calibration

Do not introduce opaque behavior before the local rules are understandable.
