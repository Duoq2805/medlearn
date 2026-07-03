import { motion, useSpring, useTransform, useScroll, useReducedMotion } from 'framer-motion';
import React, { useRef } from 'react';
import { MOTION } from '../../styles/motionTokens';

/* ─── Reduced Motion Helper ─── */
const useSafeMotion = () => {
  const reduced = useReducedMotion();
  return !reduced;
};

/* ─── Animation Variants ─── */
export const fadeInVariants = {
  hidden: { opacity: 0 },
  visible: { opacity: 1, transition: { duration: MOTION.duration.section, ease: MOTION.easing.standard } },
} as const;

export const slideUpVariants = {
  hidden: { opacity: 0, y: MOTION.offset.slideUp },
  visible: { opacity: 1, y: 0, transition: { duration: MOTION.duration.section, ease: MOTION.easing.standard } },
} as const;

export const scaleInVariants = {
  hidden: { opacity: 0, scale: MOTION.scale.from },
  visible: { opacity: 1, scale: 1, transition: { duration: MOTION.duration.item, ease: MOTION.easing.standard } },
} as const;

/* ─── FadeIn (handles both fade and slide-up) ─── */

interface FadeInProps {
  children: React.ReactNode;
  className?: string;
  once?: boolean;
  amount?: number | 'some' | 'all';
  direction?: 'up' | 'fade';
  [key: string]: unknown;
}

export const FadeIn = ({ children, className, once, amount, direction = 'fade', ...props }: FadeInProps) => {
  const shouldAnimate = useSafeMotion();
  return (
    <motion.div
      className={className}
      initial={shouldAnimate ? "hidden" : undefined}
      whileInView="visible"
      viewport={{ once: once ?? MOTION.viewport.default.once, amount: amount ?? MOTION.viewport.default.amount }}
      variants={{
        hidden: { opacity: 0, y: direction === 'up' ? MOTION.offset.slideUp : 0 },
        visible: { opacity: 1, y: 0, transition: { duration: MOTION.duration.section, ease: MOTION.easing.standard } },
      }}
      {...props}
    >
      {children}
    </motion.div>
  );
};

/* ─── Float (ambient floating motion) ─── */

interface FloatProps {
  children: React.ReactNode;
  className?: string;
  duration?: number;
  delay?: number;
  [key: string]: unknown;
}

export const Float = ({ children, className, duration = MOTION.duration.float, delay = 0, ...props }: FloatProps) => (
  <motion.div
    className={className}
    animate={{ y: [0, MOTION.offset.float, 0], rotate: [0, 3, 0] }}
    transition={{
      duration,
      repeat: Infinity,
      ease: MOTION.easing.flow,
      delay,
    }}
    {...props}
  >
    {children}
  </motion.div>
);

/* ─── Breathing (slow pulse / ambient life) ─── */

interface BreathingProps {
  children: React.ReactNode;
  className?: string;
  duration?: number;
  [key: string]: unknown;
}

export const Breathing = ({ children, className, duration = MOTION.duration.ambient, ...props }: BreathingProps) => (
  <motion.div
    className={className}
    animate={{ opacity: [0.6, 0.8, 0.6], scale: [1, 1.02, 1] }}
    transition={{
      duration,
      repeat: Infinity,
      ease: MOTION.easing.ambient,
    }}
    {...props}
  >
    {children}
  </motion.div>
);

/* ─── Drift (slow rotational drift) ─── */

interface DriftProps {
  children: React.ReactNode;
  className?: string;
  duration?: number;
  [key: string]: unknown;
}

export const Drift = ({ children, className, duration = MOTION.duration.drift, ...props }: DriftProps) => (
  <motion.div
    className={className}
    animate={{ rotate: [0, 3, 0] }}
    transition={{
      duration,
      repeat: Infinity,
      ease: MOTION.easing.ambient,
    }}
    {...props}
  >
    {children}
  </motion.div>
);

/* ─── StaggerContainer ─── */

interface StaggerContainerProps {
  children: React.ReactNode;
  className?: string;
  staggerChildren?: number;
  delayChildren?: number;
  [key: string]: unknown;
}

export const StaggerContainer = ({
  children,
  className,
  staggerChildren = 0.1,
  delayChildren = 0,
  ...props
}: StaggerContainerProps) => (
  <motion.div
    className={className}
    initial="hidden"
    whileInView="visible"
    viewport={MOTION.viewport.default}
    variants={{
      hidden: { opacity: 0 },
      visible: {
        opacity: 1,
        transition: { staggerChildren, delayChildren },
      },
    }}
    {...props}
  >
    {children}
  </motion.div>
);

/* ─── StaggerItem ─── */

interface StaggerItemProps {
  children: React.ReactNode;
  className?: string;
  [key: string]: unknown;
}

export const StaggerItem = ({ children, className, ...props }: StaggerItemProps) => (
  <motion.div
    className={className}
    variants={{
      hidden: { opacity: 0, y: MOTION.offset.stagger },
      visible: { opacity: 1, y: 0, transition: { duration: MOTION.duration.stagger, ease: MOTION.easing.enter } },
    }}
    {...props}
  >
    {children}
  </motion.div>
);

/* ─── ParallaxLayer (scroll-driven parallax) ─── */

interface ParallaxLayerProps {
  children: React.ReactNode;
  className?: string;
  speed?: number;
  offset?: number;
}

export const ParallaxLayer = ({ children, className, speed = 0.1, offset = 0 }: ParallaxLayerProps) => {
  const ref = useRef<HTMLDivElement>(null);
  const { scrollY } = useScroll();
  const y = useTransform(scrollY, (latest) => (latest - offset) * speed);
  const springY = useSpring(y, { stiffness: 50, damping: 20 });

  return (
    <motion.div ref={ref} className={className} style={{ y: springY }}>
      {children}
    </motion.div>
  );
};

/* ─── AnimatedSection ─── */

interface AnimatedSectionProps {
  children: React.ReactNode;
  className?: string;
  [key: string]: unknown;
}

export const AnimatedSection = ({ children, className, ...props }: AnimatedSectionProps) => {
  const shouldAnimate = useSafeMotion();
  return (
    <motion.section
      className={className}
      initial={shouldAnimate ? "hidden" : undefined}
      whileInView="visible"
      viewport={MOTION.viewport.loose}
      variants={{
        hidden: { opacity: 0, y: MOTION.offset.slideUp },
        visible: { opacity: 1, y: 0, transition: { duration: MOTION.duration.section, ease: MOTION.easing.standard } },
      }}
      {...props}
    >
      {children}
    </motion.section>
  );
};

/* ─── AnimatedButton ─── */

interface AnimatedButtonProps {
  children: React.ReactNode;
  className?: string;
  [key: string]: unknown;
}

export const AnimatedButton = ({ children, className, ...props }: AnimatedButtonProps) => (
  <motion.div
    className={className}
    whileHover={{ scale: MOTION.scale.hover }}
    whileTap={{ scale: MOTION.scale.tap }}
    transition={MOTION.spring.button}
    {...props}
  >
    {children}
  </motion.div>
);
