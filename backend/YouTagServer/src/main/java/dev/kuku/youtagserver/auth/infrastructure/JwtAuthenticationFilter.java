package dev.kuku.youtagserver.auth.infrastructure;

import dev.kuku.youtagserver.auth.domain.models.JwtAuthenticationToken;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
class JwtAuthenticationFilter extends OncePerRequestFilter {
    final JwtAuthenticationProvider provider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        String token = authHeader.replace("Bearer ", "");
        log.info("JWT Token found : {}", token);
        SecurityContextHolder.getContext().setAuthentication(provider.authenticate(new JwtAuthenticationToken(token)));
        filterChain.doFilter(request, response);
    }
}
