package com.cakify.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutFilter;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .logout(logout -> logout.disable())
                .formLogin(login -> login.disable())
                .httpBasic(basic -> basic.disable());

        // Completely clear filter chain from interfering components
        http.addFilterBefore((req, res, chain) -> chain.doFilter(req, res), LogoutFilter.class);

        return http.build();
    }
}
