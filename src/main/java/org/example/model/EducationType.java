package org.example.model;

public enum EducationType {
    FULL_TIME("Очная"),
    PART_TIME("Заочная"),
    ONLINE("Онлайн");

    private final String displayName;

    EducationType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
