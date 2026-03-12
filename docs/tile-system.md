# Tile System

This document defines the shared tile language for `Start`, `Single`, and later `Multi`.

## Core rule

`Days` uses one tile family across modes.

That means:
- shared shape rhythm
- shared spacing rhythm
- shared color roles
- shared interaction language

Modes may differ in density and arrangement, but they must still feel like the same product.

## What a tile is

A tile is:
- one coherent unit of meaning
- directly scannable
- visually calm
- usually actionable

A tile is not:
- a generic wrapper
- a feed card with noisy metadata
- a stack of unrelated interaction patterns

## Tile anatomy

Each tile should have this internal grammar:
- `eyebrow`: optional short label
- `headline`: main read
- `payload`: visual or structural value
- `support`: optional compact context
- `affordance`: optional directional hint or state mark

Rules:
- payload carries most of the meaning
- support copy stays short
- if a tile needs paragraphs to explain itself, the tile is wrong
- chips that do not change a decision should be removed
- implementation terms do not belong in user-facing tiles

## Tile roles

### Hero tile

Use for the primary work surface of a mode.

Characteristics:
- visually dominant
- highest information density
- still one main job

### Summary tile

Use for compact scanning of one topic or area.

Characteristics:
- medium density
- fast to scan
- usually one tap target

### Action tile

Use when the tile mainly starts a flow or moves the user elsewhere.

Characteristics:
- strong affordance
- minimal copy

## Layout rules

- Prefer one orienting tile plus one work tile over long stacks of sibling cards.
- Inside a work tile, use section rhythm and dividers before adding nested cards.
- Keep corner radii and spacing on a small token scale instead of ad-hoc values.
- Meaning should come first from layout, iconography, state, and rhythm, not from explanatory text.
