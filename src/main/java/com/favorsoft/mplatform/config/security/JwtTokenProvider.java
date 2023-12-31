package com.favorsoft.mplatform.config.security;

import com.favorsoft.mplatform.cdn.domain.User;
import com.favorsoft.mplatform.cdn.service.UserService;
import io.jsonwebtoken.*;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.Date;

@Component
public class JwtTokenProvider {
    @Value("${jwt.secret}")
    private String secretKey;

    private final UserService userService;

    public JwtTokenProvider(UserService userService){
        this.userService = userService;
    }

    @PostConstruct
    protected void init() {
        secretKey = Base64.getEncoder().encodeToString(secretKey.getBytes());
    }

    // Jwt 토큰으로 인증 정보를 조회
    public Authentication getAuthentication(String token) {
        System.out.println((String)this.getUserByToken(token).getIssuer());
        User userDetails = userService.getObject((String)this.getUserByToken(token).getIssuer());
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    // Jwt 토큰에서 회원 구별 정보 추출
    public Claims getUserByToken(String token) {
        return Jwts.parser().setSigningKey(secretKey).build().parseClaimsJws(token).getBody();
    }

    // Request의 Header에서 token 파싱 : "X-AUTH-TOKEN: jwt토큰"
    public String resolveToken(HttpServletRequest req) {
        final String requestTokenHeader = req.getHeader("Authorization");
        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
            return req.getHeader("Authorization").substring(7);
        }
        return null;
    }

    // Jwt 토큰의 유효성 + 만료일자 확인
    public boolean validateToken(String jwtToken) {
        try {
            Jws<Claims> claims = Jwts.parser().setSigningKey(secretKey).build().parseClaimsJws(jwtToken);
            return !claims.getBody().getExpiration().before(new Date());
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
