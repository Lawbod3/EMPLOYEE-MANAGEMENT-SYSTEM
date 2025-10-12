package com.darum.employee.config;

import io.r2dbc.spi.ConnectionFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

import java.util.List;

@Configuration
@EnableR2dbcRepositories(basePackages = "com.darum.employee.repositories")
@RequiredArgsConstructor
public class R2dbcConfig extends AbstractR2dbcConfiguration {
    private final ConnectionFactory connectionFactory;

    @Override
    public ConnectionFactory connectionFactory() {
        return connectionFactory;
    }

    @Override
    protected List<Object> getCustomConverters() {
        return List.of(
                new ListConverters.StringToListConverter(),
                new ListConverters.ListToStringConverter()
        );
    }
}
