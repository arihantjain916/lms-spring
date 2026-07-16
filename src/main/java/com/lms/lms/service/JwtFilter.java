package com.lms.lms.service;

import com.lms.lms.GlobalValue.PublicRoutes;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.AccountStatusUserDetailsChecker;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsChecker;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Service;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Service
public class JwtFilter extends OncePerRequestFilter {


    @Autowired
    private JwtService jwtService;

    @Autowired
    ApplicationContext context;

    private static final AntPathMatcher pathMatcher = new AntPathMatcher();

    // throws LockedException / DisabledException, which the catch below turns into a 401
    private static final UserDetailsChecker accountStatusChecker = new AccountStatusUserDetailsChecker();

    @Autowired
    private PublicRoutes publicRoutes;


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String requestUri = request.getRequestURI();

        boolean matches = publicRoutes.PUBLIC.stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, requestUri));

        boolean openForGet = request.getMethod().equals("GET") && publicRoutes.OpenForGet.stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, requestUri));

        boolean isPublic = matches || openForGet;

        // resolve a bearer token from the Authorization header or the "token" cookie
        String authHeader = request.getHeader("Authorization");
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("token") && (authHeader == null || authHeader.isBlank())) {
                    authHeader = "Bearer " + cookie.getValue();
                }
            }
        }

        boolean hasToken = authHeader != null && authHeader.startsWith("Bearer ");

        // public route with no credentials: continue anonymously, nothing to authenticate
        if (isPublic && !hasToken) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            if (hasToken) {
                String token = authHeader.substring(7);
                String id = jwtService.extractUsername(token);
                String userAgent = jwtService.extractData(token, "userAgent");
                String ipAddress = jwtService.extractData(token, "ipAddress");

                // token is bound to the issuing device; reject if it no longer matches
                if (userAgent == null || ipAddress == null
                        || !userAgent.equals(request.getHeader("User-Agent"))
                        || !ipAddress.equals(request.getRemoteAddr())) {
                    throw new Exception("Invalid Token");
                }

                if (id != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserDetails userDetails = context.getBean(UserDetailsService.class).loadUserById(id);
                    // account state is re-checked on every request, not just at login:
                    // otherwise a token issued before a ban keeps working until it expires
                    accountStatusChecker.check(userDetails);
                    if (jwtService.validateToken(token, userDetails)) {
                        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                        usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
                    }
                }
            }
        } catch (Exception e) {
            SecurityContextHolder.clearContext();
            // on a protected route a bad/expired token is rejected; on a public route it just
            // means the request proceeds anonymously
            if (!isPublic) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
