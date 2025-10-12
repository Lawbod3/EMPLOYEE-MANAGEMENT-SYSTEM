package com.darum.employee.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Converter(autoApply = true)
public class StringListConverter implements AttributeConverter<List<String>, String> {
    private static final String DELIMITER = ",";

    @Override
    public String convertToDatabaseColumn(List<String> list) {
        return list != null ? String.join(DELIMITER, list) : "";
    }

    @Override
    public List<String> convertToEntityAttribute(String joined) {
        return joined != null && !joined.isEmpty()
                ? Arrays.asList(joined.split(DELIMITER))
                : new ArrayList<>();
    }
}
