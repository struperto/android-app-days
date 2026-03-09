# Single Domain Foundation

## Purpose

`Days` is not only a task app.

It is a local system that helps a person:
- define what a good day should look like
- see what actually happened
- understand the gap between both
- learn how body, context, behavior, and intention interact

The app should learn the user.
The user should also learn themselves.

## Product Core

The product core is:

`A 24h personal operating system for Soll, Ist, and drift.`

This means:
- the app needs a logical day, not only calendar dates
- the app needs explicit goals, not only captured items
- the app needs observations, not only plans
- the app needs evaluations, not only raw metrics
- the app needs learning signals, not only static settings

## Logical Day

The canonical day in `Single` starts at `06:00` and ends at `05:59` on the next calendar date.

Why:
- sleep and late evening belong to one continuous regulation arc
- `00:00` to `05:00` must stay visible inside the same dashboard
- adaptive planning only works if the night belongs to the day that led into it

## Core Layers

### 1. Person Layer

Stable properties of the person:
- preferred day start
- energy pattern
- recovery need
- disruption sensitivity
- roles and commitments
- active sensors and data sources

This is partly explicit and partly learned over time.

### 2. Goal Layer

The app needs explicit goal definitions.

Each goal should know:
- domain
- cadence
- target type
- unit
- preferred time window
- adaptation mode
- rationale

Examples:
- `Sleep`: minimum `8h`
- `Movement`: minimum `10_000` steps
- `Nutrition`: range `120g-160g` protein
- `Focus`: window `08:00-11:00`
- `Hydration`: minimum `2.2L`

### 3. Observation Layer

Observations are the raw `Ist`.

They can come from:
- user input
- wearable
- phone sensors
- calendar
- imported health data
- system inference

Observations should remain separate from evaluations.

Examples:
- `7h 18m` sleep from watch
- `8_430` steps from phone or watch
- `3 coffees` from manual input
- `41 notifications` from phone
- `2h focus block` from plan or calendar

### 4. Evaluation Layer

The app must transform raw observations into meaningful `Soll/Ist` status.

Evaluation answers:
- on track or not
- below target or above target
- in the right time window or not
- good quantity but poor quality
- realistic today or should be adapted

Examples:
- sleep duration close to target, but poor recovery
- step goal missed, but still acceptable after bad sleep
- enough calories, but protein too low
- focus target missed because notification pressure was high

### 5. Timeline Layer

The dashboard needs hourly slots as the visible surface.

An hour slot is not the source of truth.
It is a projection of:
- goals
- observations
- derived evaluations
- local notes
- drift indicators

### 6. Learning Layer

The app should not jump to heavy AI first.

It should first learn from:
- explicit goals
- repeated corrections
- timing shifts
- completion patterns
- sensor context
- self-reported state

Important rule:
learning should explain itself in product language later.

Examples:
- `You sleep better when late caffeine is low.`
- `Your focus holds best between 08:00 and 10:00.`
- `10k steps is too aggressive on low-recovery days.`

## Human Domains Across 24h

The app should eventually model at least these domains.

| Domain | Example Soll | Example Ist | Important Drift |
| --- | --- | --- | --- |
| Sleep | 8h, asleep before 23:30, good recovery | 7h 10m, late sleep onset, weak HRV | too short, too late, poor quality |
| Recovery | calm evening, low stimulation | late screen, high pulse, no wind-down | wrong timing, wrong quality |
| Nutrition | protein target, meal rhythm, less sugar | enough calories, poor protein timing | quantity ok, composition poor |
| Hydration | 2.2L daily | 1.1L by afternoon | behind schedule |
| Movement | 10k steps, active minutes | 6.2k steps, one workout | total low, timing wrong |
| Focus | 2 protected hours | fragmented by notifications | high drift from interruptions |
| Admin | bounded coordination load | inbox overflow, late backlog | admin spills into evening |
| Social | meaningful contact, not overload | many pings, little nourishing contact | attention drain |
| Household | enough upkeep, not dominant | chores expand into recovery time | household steals regulation |
| Health | medication, symptoms, appointments | symptom spike, missed medication | safety and regulation issue |
| Emotional state | stable enough, no overload spiral | anxious, irritable, scattered | state blocks other goals |

## What `Soll` Can Be

`Soll` is not only one number.

It can be:
- minimum
- maximum
- range
- exact target
- time window
- binary done/not done
- quality score
- adaptive target based on context

## What `Ist` Can Be

`Ist` is not only one measurement.

It can be:
- count
- duration
- intensity
- quality
- timing
- subjective feeling
- inferred friction

## Minimal Data Model Needed

To make the system meaningful, we need at minimum:
- logical day
- domain goal
- goal window
- observation
- observation source
- evaluation
- hourly slot projection
- learning signal
- sensor capability map

## Architecture Recommendation

Recommended layers for the codebase:

1. `foundation domain`
   - pure data objects
   - no Android dependencies
   - no Room assumptions
2. `repository models`
   - storage and integration facing models
3. `projection engines`
   - convert goals plus observations into `Soll/Ist`
4. `view state`
   - dashboard-friendly shapes
5. `learning services`
   - pattern detection and adaptive target generation

## Near-Term Build Order

### Phase 0

Already underway:
- hourly dashboard
- three windows
- local note entry

### Phase 1

Define the foundation:
- domain model
- goal vocabulary
- observation vocabulary
- learning signal vocabulary

### Phase 2

Persist the hourly layer:
- slot entries
- hourly notes
- status changes
- event history

### Phase 3

Add explicit goals:
- sleep
- movement
- nutrition
- hydration
- focus

### Phase 4

Add observation pipelines:
- wearable sleep
- steps
- workouts
- notifications
- calendar
- manual nutrition

### Phase 5

Build simple evaluations:
- on track
- below target
- above target
- outside window
- recovery-adjusted target

### Phase 6

Build first learning:
- repeated successful hours
- repeated drift hours
- adaptive step goals
- adaptive focus windows
- sleep quality influences next-day recommendations

## Decision Rules

For now, these rules should stay stable:
- all primary reasoning is local-first
- the dashboard is only a projection, not the source of truth
- raw observations and derived evaluations stay separate
- adaptive targets must stay inspectable
- a user override is stronger than a learned guess

## Immediate Next Step

The next architectural step after this foundation should be:

`Goal definitions plus observation events`

That is the smallest real base from which sleep, nutrition, movement, and later adaptive `Soll` can emerge.
