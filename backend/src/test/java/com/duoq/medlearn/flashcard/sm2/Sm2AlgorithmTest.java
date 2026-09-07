package com.duoq.medlearn.flashcard.sm2;

import com.duoq.medlearn.flashcard.sm2.Sm2Algorithm;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Sm2AlgorithmTest {

    @Test
    void perfectQuality_ShouldIncreaseInterval() {
        var result = Sm2Algorithm.calculate(5, 2.5, 0, 0);
        assertEquals(2.6, result.getEasinessFactor(), 0.001);
        assertEquals(1, result.getInterval());
        assertEquals(1, result.getRepetitions());
        assertNotNull(result.getNextReviewAt());
    }

    @Test
    void secondPerfectReview_ShouldSetIntervalToSix() {
        var result = Sm2Algorithm.calculate(5, 2.6, 1, 1);
        assertEquals(2.7, result.getEasinessFactor(), 0.001);
        assertEquals(6, result.getInterval());
        assertEquals(2, result.getRepetitions());
    }

    @Test
    void thirdPerfectReview_ShouldMultiplyIntervalByEF() {
        // Uses OLD ef (2.6) for interval calc: round(6 * 2.6) = 16
        var result = Sm2Algorithm.calculate(5, 2.6, 6, 2);
        assertEquals(2.7, result.getEasinessFactor(), 0.001);
        assertEquals(16, result.getInterval());
        assertEquals(3, result.getRepetitions());
    }

    @Test
    void intervalUsesOldEF_NotNewEF() {
        // Old ef=2.5 → round(6*2.5) = 15, new ef = 2.6 after calc
        var result = Sm2Algorithm.calculate(5, 2.5, 6, 2);
        assertEquals(2.6, result.getEasinessFactor(), 0.001);
        assertEquals(15, result.getInterval());
    }

    @Test
    void failedReview_ShouldResetRepetitions() {
        var result = Sm2Algorithm.calculate(0, 2.5, 10, 5);
        assertEquals(1.7, result.getEasinessFactor(), 0.001);
        assertEquals(1, result.getInterval());
        assertEquals(0, result.getRepetitions());
    }

    @Test
    void failedReviewWithLowEF_ShouldClampToMinimum() {
        var result = Sm2Algorithm.calculate(0, 1.0, 5, 3);
        assertEquals(1.3, result.getEasinessFactor(), 0.001);
        assertEquals(1, result.getInterval());
        assertEquals(0, result.getRepetitions());
    }

    @Test
    void borderlineFail_Quality2_ShouldReset() {
        var result = Sm2Algorithm.calculate(2, 2.5, 6, 3);
        assertTrue(result.getEasinessFactor() < 2.5);
        assertEquals(1, result.getInterval());
        assertEquals(0, result.getRepetitions());
    }

    @Test
    void hardCorrect_Quality3_ShouldGraduallyIncrease() {
        var result = Sm2Algorithm.calculate(3, 2.5, 0, 0);
        assertEquals(2.36, result.getEasinessFactor(), 0.001);
        assertEquals(1, result.getInterval());
        assertEquals(1, result.getRepetitions());
    }

    // minimumEF test moved to failedReviewWithLowEF_ShouldClampToMinimum

    @Test
    void negativeRepetitions_ShouldNormalize() {
        var result = Sm2Algorithm.calculate(4, 2.5, 0, -1);
        assertEquals(1, result.getInterval());
        assertEquals(1, result.getRepetitions());
    }

    @Test
    void negativeInterval_ShouldNormalize() {
        var result = Sm2Algorithm.calculate(5, 2.5, -5, 0);
        assertEquals(1, result.getInterval());
    }

    @Test
    void boundaryQuality_ShouldThrow() {
        assertThrows(IllegalArgumentException.class, () -> Sm2Algorithm.calculate(-1, 2.5, 0, 0));
        assertThrows(IllegalArgumentException.class, () -> Sm2Algorithm.calculate(6, 2.5, 0, 0));
    }

    @Test
    void isCorrect_ShouldReturnTrueForQuality3OrAbove() {
        assertFalse(Sm2Algorithm.isCorrect(0));
        assertFalse(Sm2Algorithm.isCorrect(1));
        assertFalse(Sm2Algorithm.isCorrect(2));
        assertTrue(Sm2Algorithm.isCorrect(3));
        assertTrue(Sm2Algorithm.isCorrect(4));
        assertTrue(Sm2Algorithm.isCorrect(5));
    }

    @Test
    void masterLevelReview_ShouldReachLongInterval() {
        // Simulate 5 perfect reviews
        var result = Sm2Algorithm.calculate(5, 2.5, 0, 0);
        result = Sm2Algorithm.calculate(5, result.getEasinessFactor(), result.getInterval(), result.getRepetitions());
        result = Sm2Algorithm.calculate(5, result.getEasinessFactor(), result.getInterval(), result.getRepetitions());
        result = Sm2Algorithm.calculate(5, result.getEasinessFactor(), result.getInterval(), result.getRepetitions());
        result = Sm2Algorithm.calculate(5, result.getEasinessFactor(), result.getInterval(), result.getRepetitions());

        assertTrue(result.getEasinessFactor() > 2.5);
        assertTrue(result.getInterval() >= 16);
        assertEquals(5, result.getRepetitions());
    }
}
