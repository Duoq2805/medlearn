package com.duoq.medlearn.flashcard.sm2;

import lombok.Value;

import java.time.OffsetDateTime;

/**
 * Pure SM-2 spaced repetition algorithm.
 * No dependencies on JPA, services, or Spring.
 */
public final class Sm2Algorithm {

    private Sm2Algorithm() {}

    @Value
    public static class Sm2Result {
        double easinessFactor;
        int interval;
        int repetitions;
        OffsetDateTime nextReviewAt;
    }

    /**
     * Calculate next review parameters using SM-2.
     *
     * @param quality     0-5 (0=complete blackout, 1=incorrect but saw answer,
     *                    2=incorrect but felt easy, 3=correct with difficulty,
     *                    4=correct after hesitation, 5=perfect recall)
     * @param ef          current easiness factor (minimum 1.3)
     * @param interval    current interval in days
     * @param repetitions consecutive correct reviews so far
     */
    public static Sm2Result calculate(int quality, double ef, int interval, int repetitions) {
        if (quality < 0 || quality > 5) {
            throw new IllegalArgumentException("Quality must be 0-5, got: " + quality);
        }
        if (ef < 1.3) ef = 1.3;
        if (repetitions < 0) repetitions = 0;
        if (interval < 0) interval = 0;

        if (quality < 3) {
            // Failed: reset
            repetitions = 0;
            interval = 1;
        } else {
            // Passed: increase interval
            if (repetitions == 0) {
                interval = 1;
            } else if (repetitions == 1) {
                interval = 6;
            } else {
                interval = Math.max(1, (int) Math.round(interval * ef));
            }
            repetitions++;
        }

        // Update easiness factor
        ef = ef + (0.1 - (5 - quality) * (0.08 + (5 - quality) * 0.02));
        if (ef < 1.3) ef = 1.3;

        return new Sm2Result(ef, interval, repetitions, OffsetDateTime.now().plusDays(interval));
    }

    /**
     * Quick helper: is a given quality considered "correct"?
     */
    public static boolean isCorrect(int quality) {
        return quality >= 3;
    }
}
