package com.softuni.finalexam.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CsrfTokenRepository csrfTokenRepository() {
        return new HttpSessionCsrfTokenRepository();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // CSRF protection is enabled by default in Spring Security 6.x
            // Configure CSRF to use session-based token repository
            .csrf(csrf -> csrf
                .csrfTokenRepository(csrfTokenRepository())
            )
            // Permit all requests since we're using custom session-based authentication
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/**")
                    .permitAll()
            )
            // Configure session management - ALWAYS create sessions to ensure CSRF tokens can be saved
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.ALWAYS)
            )
            // Disable default form login since we use custom authentication
            .formLogin(form -> form.disable())
            // Disable default logout since we use custom logout
            .logout(logout -> logout.disable());
        
        return http.build();
    }
}

