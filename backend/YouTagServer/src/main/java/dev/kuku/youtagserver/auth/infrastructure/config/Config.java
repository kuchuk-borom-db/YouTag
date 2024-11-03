package dev.kuku.youtagserver.auth.infrastructure.config;

import dev.kuku.youtagserver.auth.api.services.JwtService;
import dev.kuku.youtagserver.auth.infrastructure.JwtAuthenticationFilter;
import dev.kuku.youtagserver.auth.infrastructure.JwtAuthenticationProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

@EnableWebSecurity
@Configuration
class Config {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtService jwtService) throws Exception {
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
        http.addFilterBefore(new JwtAuthenticationFilter(new JwtAuthenticationProvider(jwtService)), BasicAuthenticationFilter.class);
        return http.build();
    }
}
