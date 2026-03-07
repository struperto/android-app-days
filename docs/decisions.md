# Decisions

## 2026-03-07 - Start with Single only

Decision:
- Build `Single` first until it becomes a real daily-use flow.

Why:
- The old project proved the product direction, but spread across too many systems.
- `Single` defines the real data model, UI language, and product rhythm.
- `Multi` and `Assist` should grow from a stable `Single`, not in parallel.

## 2026-03-07 - UI labels are Single, Multi, Assist

Decision:
- Use `Single`, `Multi`, and `Assist` as UI labels.

Why:
- They are clearer and less game-like than `Singleplayer` and `Multiplayer`.
- `Assist` fits the local AI/support role better than `Coworker` for the current product tone.

## 2026-03-07 - Keep AI small in V1

Decision:
- Bring only a small local AI gateway into `Single` V1.

Why:
- The old repo already contains strong AI infrastructure, but it is too large for a first focused product.
- The right V1 role for AI is support inside the core flow:
  - summarize
  - OCR
  - next-step suggestion

Explicitly not in first V1:
- AI lab
- model manager in the main user flow
- notification AI pipeline
- Operator and WebView automation

## 2026-03-07 - Old repo is reference, not migration target

Decision:
- Treat the old `android_app` project as a reference archive.

Why:
- It contains many valuable ideas, but too much coupled surface area.
- A clean repo keeps decisions visible and scope under control.

Allowed from old repo:
- design ideas
- wording ideas
- selected logic patterns
- selected technical slices

Not allowed by default:
- broad copy-over of whole subsystems

## 2026-03-07 - Product truth lives in docs, not in chat

Decision:
- Keep product direction in repo docs.

Why:
- Chat is useful for discovery, but poor as a long-term source of truth.
- The team needs one stable place for scope, decisions, and deferred ideas.

The current control files are:
- `docs/single-v1.md`
- `docs/decisions.md`
- `docs/parking-lot.md`

