package com.softuni.finalexam.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class SessionCheckInterceptor implements HandlerInterceptor {

    public static final Set<String> UNAUTHENTICATED_ENDPOINTS = Set.of("/login", "/register", "/", "/profile", "/profile/add", "/profile-add", "/products", "/cart", "/cart/add");
    public static final Set<String> UNAUTHENTICATED_PATH_PREFIXES = Set.of("/products/", "/cart/");

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String servletPath = request.getServletPath();
        
        // потребителя иска да достъпи разрешена страница
        if (UNAUTHENTICATED_ENDPOINTS.contains(servletPath)){
            return true;
        }
        
        // Check path prefixes (e.g., /products/{id}, /cart/add)
        for (String prefix : UNAUTHENTICATED_PATH_PREFIXES) {
            if (servletPath.startsWith(prefix)) {
                return true;
            }
        }

        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendRedirect("/login");
            return false;
        }

        Object userId = session.getAttribute("userId");
        if (userId == null) {
            session.invalidate();
            response.sendRedirect("/login");
            return false;
        }

        return true;
    }
}
