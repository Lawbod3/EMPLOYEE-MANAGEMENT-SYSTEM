package com.darum.employee.config;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;


@Component
public class ListConverters {
    private static final ObjectMapper mapper = new ObjectMapper();

    @ReadingConverter
    public static class StringToListConverter implements Converter<String, List<String>> {
        @Override
        public List<String> convert(String source) {
            try {
                return Arrays.asList(mapper.readValue(source, String[].class));
            } catch (JsonProcessingException e) {
                return List.of(); // fallback empty list
            }
        }
        }

        public static class ListToStringConverter implements Converter<List<String>, String> {
            @Override
            public String convert(List<String> source) {
                try{
                    return mapper.writeValueAsString(source);
                } catch (JsonProcessingException e) {
                    return "[]";
                }
            }
        }
    }




