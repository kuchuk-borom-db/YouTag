package dev.kuku.youtagserver.auth.infrastructure;

import dev.kuku.youtagserver.auth.application.JwtService;
import dev.kuku.youtagserver.user.api.services.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@EnableWebSecurity
@Configuration
class Config {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtService jwtService, UserService userService) throws Exception {
        http.authorizeHttpRequests(
                req -> req
                        .requestMatchers("api/public/**").permitAll()
                        .requestMatchers("api/authenticated/**").authenticated()
                        .anyRequest().denyAll()
        );
        http.csrf(AbstractHttpConfigurer::disable);
        http.formLogin(AbstractHttpConfigurer::disable);
        http.logout(AbstractHttpConfigurer::disable);
        http.httpBasic(AbstractHttpConfigurer::disable);
        http.addFilterBefore(new JwtAuthenticationFilter(new JwtAuthenticationProvider(jwtService, userService)), BasicAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Allow your client origin specifically
        configuration.addAllowedOrigin("http://localhost:8081");

        // Allow all common HTTP methods including OPTIONS (important for preflight)
        configuration.addAllowedMethod("GET");
        configuration.addAllowedMethod("POST");
        configuration.addAllowedMethod("PUT");
        configuration.addAllowedMethod("DELETE");
        configuration.addAllowedMethod("OPTIONS");

        // Allow all headers
        configuration.addAllowedHeader("*");

        // Allow credentials if needed (cookies, authorization headers, etc.)
        configuration.setAllowCredentials(true);

        // Optional: expose headers if needed
        configuration.addExposedHeader("Authorization");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Apply to all paths
        source.registerCorsConfiguration("/**", configuration);
        // Specifically apply to your OAuth endpoint
        source.registerCorsConfiguration("/api/public/auth/**", configuration);

        return source;
    }
}
