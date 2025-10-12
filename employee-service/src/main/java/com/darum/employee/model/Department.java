package com.darum.employee.model;

public enum Department {
    IT,
    HR,
    FINANCE,
    SALES,
    MARKETING,
    OPERATIONS,
    SUPPORT;

    // Optional helper for cleaner use
    public static boolean isValid(String value) {
        for (Department department : Department.values()) {
            if (department.name().equalsIgnoreCase(value)) {
                return true;
            }
        }
        return false;
    }
}
