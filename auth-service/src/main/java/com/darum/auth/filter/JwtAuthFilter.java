//package com.darum.auth.filter;
//
//import com.darum.shared.security.JwtUtil;
//import com.darum.shared.security.SecurityConstants;
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
//import org.springframework.stereotype.Component;
//import org.springframework.web.filter.OncePerRequestFilter;
//import jakarta.servlet.ServletException;
//
//import java.io.IOException;
//
//@Slf4j
//@Component
//@RequiredArgsConstructor
//public class JwtAuthFilter extends OncePerRequestFilter {
//    @Value("${jwt.secret}")
//    private String jwtSecret;
//    private final UserDetailsService userDetailsService;
//
//    @Override
//    protected void doFilterInternal(HttpServletRequest request,
//                                    HttpServletResponse response,
//                                    FilterChain filterChain) throws ServletException, IOException {
//
//        try {
//            String authHeader = request.getHeader(SecurityConstants.TOKEN_HEADER);
//
//            if (authHeader == null || !authHeader.startsWith(SecurityConstants.TOKEN_PREFIX)) {
//                filterChain.doFilter(request, response);
//                return;
//            }
//
//            String token = authHeader.substring(7);
//            String userEmail = JwtUtil.extractUsername(token, jwtSecret);
//
//            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
//                UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);
//
//                if (JwtUtil.validateToken(token, jwtSecret)) {
//                    UsernamePasswordAuthenticationToken authToken =
//                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
//
//                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
//                    SecurityContextHolder.getContext().setAuthentication(authToken);
//
//                    log.debug("Authenticated user: {}", userEmail);
//                }
//            }
//        } catch (Exception e) {
//            log.error("Cannot set user authentication: {}", e.getMessage());
//        }
//
//        filterChain.doFilter(request, response);
//    }
//
//}
