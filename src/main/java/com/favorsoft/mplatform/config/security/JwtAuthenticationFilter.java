package com.favorsoft.mplatform.config.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import com.favorsoft.mplatform.support.JwtTokenUtil;

import java.io.IOException;

public class JwtAuthenticationFilter extends BasicAuthenticationFilter {
    private JwtTokenProvider jwtTokenProvider;
    private JwtTokenUtil jwtTokenUtil;

    public JwtAuthenticationFilter(AuthenticationManager authenticationManager, JwtTokenProvider jwtTokenProvider, JwtTokenUtil jwtTokenUtil) {
        super(authenticationManager);
        this.jwtTokenProvider = jwtTokenProvider;
        this.jwtTokenUtil = jwtTokenUtil;
    }
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = jwtTokenProvider.resolveToken((HttpServletRequest) request);
        if (token != null && jwtTokenProvider.validateToken(token)) {
            Authentication auth = jwtTokenProvider.getAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(auth);
            filterChain.doFilter(request, response);
        }else{
            filterChain.doFilter(request, response);
        }
    }


}
