package com.cavcav.swiftcart.common.config;

import com.cavcav.swiftcart.auth.service.JwtService;
import com.cavcav.swiftcart.user.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final RedisTemplate<String, String> redisTemplate;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        // Header yoksa veya Bearer ile başlamıyorsa geç
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.replace("Bearer", "").trim();

        // Blacklist kontrolü
        if (isBlacklisted(token)) {
            log.warn("Blacklisted token used: path={}", request.getRequestURI());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token has been invalidated");
            return;
        }

        // Token geçerli mi?
        if (!jwtService.isAccessTokenValid(token)) {
            log.warn("Invalid access token: path={}", request.getRequestURI());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired token");
            return;
        }

        // Refresh token ile access token endpoint'ine istek atmaya çalışıyor mu?
        String tokenType = jwtService.extractTokenType(token);
        if (!"ACCESS".equals(tokenType)) {
            log.warn("Refresh token used as access token: path={}", request.getRequestURI());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token type");
            return;
        }

        String email = jwtService.extractEmailFromAccessToken(token);
        String role = jwtService.extractRoleFromAccessToken(token);

        // Kullanıcı hâlâ aktif mi?
        userRepository.findByEmail(email).ifPresentOrElse(user -> {
            if (!user.getIsActive() || !user.getIsEmailVerified()) {
                log.warn("Inactive or unverified user tried to access: email={}", email);
                try {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Account is not active");
                } catch (IOException e) {
                    log.error("Error sending response: {}", e.getMessage());
                }
                return;
            }

            // SecurityContext'e set et
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            user,
                            null,
                            List.of(new SimpleGrantedAuthority("ROLE_" + role))
                    );
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            log.debug("JWT authentication successful: email={}, path={}",
                    email, request.getRequestURI());

        }, () -> log.warn("User not found from token: email={}", email));

        filterChain.doFilter(request, response);
    }

    private boolean isBlacklisted(String token) {
        return redisTemplate.hasKey("blacklist:" + token);
    }

    // Auth endpoint'lerini filtreden geç
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.startsWith("/api/v1/auth/") ||
                path.startsWith("/swagger-ui") ||
                path.startsWith("/api-docs");
    }
}