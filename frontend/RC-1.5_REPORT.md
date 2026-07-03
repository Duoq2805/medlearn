# RC-1.5 Report — Narrative Scroll Experience (Revised)

## What changed

| File | Δ |
|---|---|
| `src/components/motion/MotionWrappers.tsx` | **-30** (reverted personality presets) |
| `src/pages/home/HomePage.tsx` | **-25** (static feature cards, removed personality props) |

**Total: -55 lines, cleaner code.**

## What was done wrong in the first attempt

The first RC-1.5 pass added 6 section "personalities" to `AnimatedSection` — different opacity/translateY/scale values per section. That's not storytelling. That's different flavors of the same thing. The user called this out correctly.

## What actually changed

### Interaction model shift per section

| Section | Before (activity) | After (activity) | What changed |
|---|---|---|---|
| Hero | Hover floating cards | **Watch** — passive | Removed nothing (already passive) |
| Disease Cards | Hover cards + internal buttons | **Explore** — tactile inspection | Cards keep hover-lift (y:-6, scale:1.01) |
| Interactive Features | Click symptoms + cases | **Interact** — state changes matter | Symptom buttons change content on click |
| Features Grid | Hover 8 feature cards with lift + icon bounce | **Understand** — static reference | Removed all hover effects → pure reading |
| CTA | Click one of two buttons | **Decide** — weighted choice | Two distinct paths (register vs explore) |

### Key change: Features Grid is now static

The 8 feature cards in the "Understand" section **no longer hover or animate**. No `whileHover`, no spring, no lift, no icon bounce. Pure `<div>` elements with content. The user's only activity is reading.

This creates a genuine rhythm:
1. **Watch** (no interaction needed)
2. **Explore** (tactile — cards lift when inspected)
3. **Interact** (clicking changes content directly)
4. **Understand** (pure reading — no interaction expected)
5. **Decide** (choose between two paths)

## What was reverted

- `AnimatedSection` is back to its simple single-variant form
- No `personality` prop
- No personality preset map
- All `AnimatedSection` usages in HomePage use clean `<AnimatedSection>` no extra props

## Remaining weaknesses

1. **Disease Cards and Interactive Features** both use card-hover patterns. The "Explore" vs "Interact" distinction is real (Explore: read + click sub-actions; Interact: click changes state) but might not read immediately — the visual language for "inspect" vs "interact" is the same (gray card, pointer cursor).

2. **Hero "Watch"** doesn't actively ask the user to watch — it's just the default state. The floating card animation exists but is subtle. A true "watch" chapter would have an auto-playing demo or animated illustration.

3. **Features Grid "Understand"** being static is the clearest storytelling win — the user's cursor stops turning into a pointer, which subconsciously signals "read this, don't click it." But the `card-neumorphic-sm` class still has the visual appearance of an interactive card, which is slightly misleading.

4. **The gap between sections** is still scroll-based entrance animation. The storytelling comes from content + interaction, not from how sections appear. If the content already tells a story (Clinical → Interactive → Features → Decision), the motion just needs to stay out of the way.
