package com.darum.employee.model;

import java.util.Arrays;
import java.util.Optional;

public enum Department {
    IT,
    HR,
    FINANCE,
    SALES,
    MARKETING,
    OPERATIONS,
    SUPPORT;



    public static Optional<Department> fromString(String value) {
        return Arrays.stream(values())
                .filter(dept -> dept.name().equalsIgnoreCase(value))
                .findFirst();
    }

    public static Boolean isDepartment(String value) {
        return fromString(value).isPresent();
    }


}
