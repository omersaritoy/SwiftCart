package com.cavcav.swiftcart.common.config;

import com.cavcav.swiftcart.common.exception.RateLimitException;
//import com.cavcav.swiftcart.common.service.RateLimitService;
import com.cavcav.swiftcart.common.service.RateLimitService2;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitService2 rateLimitService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String uri = request.getRequestURI();
        String method = request.getMethod();
        String ip = getClientIp(request);

        try {
            if (uri.equals("/api/v1/auth") && method.equals("POST")) {
                rateLimitService.checkSignupLimit(ip);
            } else if (uri.equals("/api/v1/auth/verify")) {
                String token = request.getParameter("token");
                if (token != null) {
                    rateLimitService.checkVerifyLimit(token);
                }
            } else if (!isExcluded(uri)) {
                rateLimitService.checkGeneralLimit(ip);
            }

            filterChain.doFilter(request, response);

        } catch (RateLimitException ex) {
            sendRateLimitError(response, ex);
        }
    }

    private boolean isExcluded(String uri) {
        return uri.startsWith("/api/v1/auth") ||
                uri.startsWith("/swagger-ui") ||
                uri.startsWith("/api-docs") ||
                uri.equals("/favicon.ico");
    }

    private void sendRateLimitError(HttpServletResponse response, RateLimitException ex) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Retry-After", String.valueOf(ex.getRetryAfter()));

        String json = """
                {
                  "status": 429,
                  "error": "Too Many Requests",
                  "message": "%s",
                  "retryAfter": %d
                }
                """.formatted(ex.getMessage(), ex.getRetryAfter());

        response.getWriter().write(json);
    }

    private String getClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}