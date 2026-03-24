package com.example.demo.config;

import com.example.demo.security.CustomOAuth2UserService;
import com.example.demo.security.CustomUserDetailsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);
    private final CustomUserDetailsService customUserDetailsService;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final String googleClientId;
    private final String googleClientSecret;

    public SecurityConfig(
        CustomUserDetailsService customUserDetailsService,
        CustomOAuth2UserService customOAuth2UserService,
        @Value("${spring.security.oauth2.client.registration.google.client-id:}") String googleClientId,
        @Value("${spring.security.oauth2.client.registration.google.client-secret:}") String googleClientSecret
    ) {
        this.customUserDetailsService = customUserDetailsService;
        this.customOAuth2UserService = customOAuth2UserService;
        this.googleClientId = googleClientId;
        this.googleClientSecret = googleClientSecret;
        
        logger.info("🔐 [Security Config] Google Client ID Present: {}", isConfigured(googleClientId));
        logger.info("🔐 [Security Config] Google Client Secret Present: {}", isConfigured(googleClientSecret));
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(customUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, DaoAuthenticationProvider daoAuthenticationProvider) throws Exception {
        logger.info("🔐 [Security Config] Configuring Security Filter Chain...");
        
        http
            .authenticationProvider(daoAuthenticationProvider)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/home", "/courses/**", "/auth/**", "/css/**", "/images/**", "/error").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/enroll/**").hasRole("PATIENT")
                .anyRequest().authenticated())
            .formLogin(form -> form
                .loginPage("/auth/login")
                .loginProcessingUrl("/auth/login")
                .defaultSuccessUrl("/home", true)
                .permitAll())
            .logout(logout -> logout
                .logoutSuccessUrl("/home")
                .permitAll())
            .rememberMe(Customizer.withDefaults());

        if (isConfigured(googleClientId) && isConfigured(googleClientSecret)) {
            logger.info("✅ [Security Config] OAuth2 Google credentials configured - enabling oauth2Login()");
            http.oauth2Login(oauth -> oauth
                .loginPage("/auth/login")
                .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                .defaultSuccessUrl("/home", true));
        } else {
            logger.warn("⚠️  [Security Config] OAuth2 Google credentials NOT configured - skipping oauth2Login()");
        }

        return http.build();
    }

    private boolean isConfigured(String value) {
        return value != null && !value.isBlank() && !value.contains("YOUR_GOOGLE");
    }
}
