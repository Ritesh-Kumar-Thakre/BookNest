package com.booknest.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.csrf(csrf -> csrf.disable())
				.cors(cors -> cors.disable())
				.authorizeHttpRequests(auth -> auth
						.requestMatchers("/auth/register", "/auth/login", "/auth/refresh").permitAll()
						.requestMatchers("/auth/internal/**").permitAll()
						.requestMatchers("/actuator/**").permitAll()
						.anyRequest().permitAll()
				);
		return http.build();
	}
}