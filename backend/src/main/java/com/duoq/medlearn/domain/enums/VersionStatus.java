package com.duoq.medlearn.domain.enums;

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
        if (this == targetStatus) {
            return true;
        }

        return switch (this) {
            case DRAFT -> targetStatus == PENDING_REVIEW || targetStatus == ARCHIVED;
            case PENDING_REVIEW -> targetStatus == APPROVED || targetStatus == REJECTED;
            case REJECTED -> targetStatus == DRAFT || targetStatus == ARCHIVED;
            case APPROVED -> targetStatus == ARCHIVED;
            case ARCHIVED -> false;
        };
    }
}