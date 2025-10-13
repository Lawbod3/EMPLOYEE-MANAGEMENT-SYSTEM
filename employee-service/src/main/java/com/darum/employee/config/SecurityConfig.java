package com.darum.employee.config;

import com.darum.employee.security.JwtAuthWebFilter;
//import com.darum.employee.security.JwtAuthenticationConverter;
//import com.darum.employee.security.JwtAuthenticationManager;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;



@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtAuthWebFilter jwtAuthWebFilter;



    @Bean
    public SecurityWebFilterChain securityFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchange -> exchange
                        .pathMatchers("/employees/admin/**").hasAnyRole("ADMIN", "SUPERADMIN")
                        .pathMatchers("/employees/**").authenticated()
                        .anyExchange().permitAll()
                )
                // ✅ Use your custom reactive JWT filter
                .addFilterBefore(jwtAuthWebFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }



    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }
}
