package com.blike.filter;

import com.blike.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        System.out.println("=== JwtAuthenticationFilter ===");
        System.out.println("Request URI: " + request.getRequestURI());
        System.out.println("Authorization header: " + header);

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            System.out.println("Token extracted: " + token);
            if (JwtUtils.validateToken(token)) {
                Claims claims = JwtUtils.parseToken(token);
                String userId = claims.getSubject();
                String username = claims.get("username", String.class);
                System.out.println("Token valid: userId=" + userId + ", username=" + username);
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(userId, null,
                                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")));
                SecurityContextHolder.getContext().setAuthentication(auth);
                System.out.println("Authentication set in SecurityContext");
            } else {
                System.out.println("Token validation FAILED");
            }
        } else {
            System.out.println("No Bearer token found in Authorization header");
        }
        chain.doFilter(request, response);
    }
}