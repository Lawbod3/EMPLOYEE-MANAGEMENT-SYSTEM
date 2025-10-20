package com.darum.auth.config;





//import com.darum.auth.security.JwtAuthenticationFilter;
import com.darum.auth.security.GatewayHeaderAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    //private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final GatewayHeaderAuthenticationFilter gatewayHeaderAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
       http.csrf(csrf -> csrf.disable())
               .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
               .authorizeHttpRequests(authz -> authz
                       .requestMatchers("/auth/**").permitAll()
                       .requestMatchers(
                               "/v3/api-docs/**",
                               "/swagger-ui/**",
                               "/swagger-ui.html"
                       ).permitAll()
                       .requestMatchers("/auth/admin/**").hasAnyRole("ADMIN","SUPERADMIN")
                       .anyRequest().authenticated()
               )
               .addFilterBefore(gatewayHeaderAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);


        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }

}
