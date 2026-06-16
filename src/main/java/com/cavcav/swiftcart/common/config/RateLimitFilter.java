package com.cavcav.swiftcart.common.config;

import com.cavcav.swiftcart.common.exception.RateLimitException;
import com.cavcav.swiftcart.common.service.RateLimitService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitService rateLimitService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String uri = request.getRequestURI();
        String ip = getClientIp(request);

        try {

            if (uri.equals("/api/auth/register")) {
                rateLimitService.checkSignupLimit(ip);
            }

            filterChain.doFilter(request, response);

        } catch (RateLimitException ex) {

            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            String json = """
                    {
                      "status": 429,
                      "error": "Too Many Requests",
                      "message": "%s",
                      "retryAfter": %d
                    }
                    """.formatted(
                    ex.getMessage(),
                    ex.getRetryAfter()
            );

            response.getWriter().write(json);
        }
    }

    private String getClientIp(HttpServletRequest request) {

        String forwardedFor = request.getHeader("X-Forwarded-For");

        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }

        return request.getRemoteAddr();
    }
}