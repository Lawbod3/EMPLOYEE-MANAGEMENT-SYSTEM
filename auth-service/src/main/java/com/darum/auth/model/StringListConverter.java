package com.darum.auth.model;

import com.darum.shared.dto.Roles;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Converter(autoApply = true)
public class StringListConverter implements AttributeConverter<List<String>, String> {
    private static final String DELIMITER = ",";

    @Override
    public String convertToDatabaseColumn(List<String> list) {
        if (list == null || list.isEmpty()) {
            return Roles.USER; // Default role if empty
        }
        // Filter out null/empty values and trim each role
        return list.stream()
                .filter(role -> role != null && !role.trim().isEmpty())
                .map(String::trim)
                .collect(Collectors.joining(DELIMITER));
    }

    @Override
    public List<String> convertToEntityAttribute(String string) {
        if (string == null || string.trim().isEmpty()) {
            return new ArrayList<>(List.of(Roles.USER)); // Default role
        }

        // Split, trim, filter empty values, and return mutable list
        return Arrays.stream(string.split(DELIMITER))
                .map(String::trim)
                .filter(role -> !role.isEmpty())
                .collect(Collectors.toCollection(ArrayList::new));
    }
}
