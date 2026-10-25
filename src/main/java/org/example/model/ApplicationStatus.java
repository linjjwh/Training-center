package org.example.model;

public enum ApplicationStatus {
    NEW("Новая"),
    UNDER_REVIEW("На рассмотрении"),
    APPROVED("Одобрена"),
    REJECTED("Отклонена"),
    CANCELLED("Отменена");

    private final String displayName;

    ApplicationStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
