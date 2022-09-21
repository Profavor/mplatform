package com.favorsoft.mplatform.support;

import java.util.Base64;
import java.util.Calendar;
import java.util.Date;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import org.springframework.beans.factory.annotation.Value;


/**
 * 테스트용 토큰발급 Class
 * 보안상 토큰발급 서버는 별도로 둔다.
 */
@Component
public class JwtTokenUtil {
    @Value("${jwt.secret}")
    private String secretKey;

    @PostConstruct
    protected void init() {
        secretKey = Base64.getEncoder().encodeToString(secretKey.getBytes());
    }

    public String createToken(){
        long curTime = System.currentTimeMillis();
		Calendar cal = Calendar.getInstance();
		cal.set(2099, 11, 30);

        return  Jwts.builder()
				
				.setSubject("Test JWT")
				
				//[2]
				//setHeaderParam 메소드를 통해 JWT 헤더가 지닐 정보들을 담는다.
				//alg 의 경우는 default 값이 SHA256이므로 따로 설정할 필요는 없다.
				//typ 를 셋팅 안해주면 오류 발생한다.
                 .setId("profavor")
                 .setIssuer("profavor")
				
				 .setHeaderParam("typ", "JWT")
				 
				 //[3] 만료 시간
				 //.setExpiration(new Date(curTime + 360000000))
				 .setExpiration(new Date(cal.getTimeInMillis()))
				 
				 //[4] 발급 시간 
				 .setIssuedAt(new Date(curTime))
				 
				 
				 //[5] Payload 에 Private Claims 를 담기 위해 claim 메소드를 이용한다.
				 // private claim으로 VO객체를 추가할 수 있음
				 //.claim(DATA_KEY, user)
				 
				 
				 //[6] 복호화 할때 사용하는 시그니처 설정.
				 // header의 인코딩값 + payload의 인코딩값 + 비밀키 = 시그니처
				 // signWith api는 해싱알고리즘과 비밀키가 필요하다.
				 .signWith(SignatureAlgorithm.HS256, secretKey)
				 
				 //생성
				 .compact();
    }
}
