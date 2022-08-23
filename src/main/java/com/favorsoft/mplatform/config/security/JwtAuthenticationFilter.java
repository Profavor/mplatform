package com.favorsoft.mplatform.config.security;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
