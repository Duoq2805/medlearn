/* ─── Motion Design Tokens ─── */

export const MOTION = {
  /* ─── Duration ─── */
  duration: {
    ambient: 3,
    drift: 8,
    float: 4,
    section: 0.8,
    item: 0.6,
    stagger: 0.6,
  } as const,

  /* ─── Easing ─── */
  easing: {
    standard: [0.25, 0.1, 0.25, 1] as const,
    ambient: [0.42, 0, 0.58, 1] as const,
    flow: 'easeInOut' as const,
    enter: 'easeOut' as const,
  },

  /* ─── Spring Presets ─── */
  spring: {
    button: {
      type: 'spring' as const,
      stiffness: 300,
      damping: 15,
    },
    parallax: {
      stiffness: 80,
      damping: 16,
    },
  },

  /* ─── Viewport ─── */
  viewport: {
    default: { once: false, amount: 0.2 as const },
    loose: { once: false, amount: 0.1 as const },
  },

  /* ─── Offset values ─── */
  offset: {
    float: -12,
    slideUp: 24,
    stagger: 20,
  },

  /* ─── Scale values ─── */
  scale: {
    from: 0.95,
    hover: 1.02,
    tap: 0.97,
  },
} as const;

export type Easing = typeof MOTION.easing;
export type Spring = typeof MOTION.spring;
export type Duration = typeof MOTION.duration;
