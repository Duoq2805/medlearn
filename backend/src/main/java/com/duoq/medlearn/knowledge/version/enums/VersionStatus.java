package com.duoq.medlearn.knowledge.version.enums;

public enum VersionStatus {
    DRAFT,
    PENDING_REVIEW,
    APPROVED,
    REJECTED,
    ARCHIVED;

    public boolean canTransitionTo(VersionStatus targetStatus) {
        if (targetStatus == null) {
            return false;
        }
        // Remove same-state transition allowance
        if (this == targetStatus) {
            return false;
        }

        return switch (this) {
            case DRAFT -> targetStatus == PENDING_REVIEW;
            case PENDING_REVIEW -> targetStatus == APPROVED || targetStatus == REJECTED;
            case REJECTED -> targetStatus == DRAFT;
            case APPROVED -> targetStatus == ARCHIVED;
            case ARCHIVED -> false;
        };
    }
}