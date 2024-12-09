package dev.kuku.youtagserver.auth.infrastructure;

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

import java.util.Arrays;
import java.util.List;

@EnableWebSecurity
@Configuration
class Config {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationProvider jwtAuthenticationProvider) throws Exception {
        http.authorizeHttpRequests(
                req -> req
                        .requestMatchers("api/public/**" ).permitAll()
                        .requestMatchers("api/authenticated/**" ).authenticated()
                        .anyRequest().denyAll()
        );
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()));
        http.csrf(AbstractHttpConfigurer::disable);
        http.formLogin(AbstractHttpConfigurer::disable);
        http.logout(AbstractHttpConfigurer::disable);
        http.httpBasic(AbstractHttpConfigurer::disable);
        http.addFilterBefore(new JwtAuthenticationFilter(jwtAuthenticationProvider), BasicAuthenticationFilter.class);
        return http.build();
    }

    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        //TODO Env variable
        // Allow multiple client origins
        configuration.setAllowedOrigins(List.of(
                "/**"
        ));

        // Allow all common HTTP methods
        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS", "HEAD"
        ));

        // Allow specific headers
        configuration.addAllowedHeader("*" );

        // Allow credentials (cookies, authorization headers, etc.)
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);


        return source;
    }


}
