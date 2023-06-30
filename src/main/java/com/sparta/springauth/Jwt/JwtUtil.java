package com.sparta.springauth.Jwt;

import com.sparta.springauth.Entity.UserRoleEnum;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.Key;
import java.util.Base64;
import java.util.Date;




@Component
public class JwtUtil {
    // JWT 데이터

    // Header KEY(cookie의 name) 값
    public static final String AUTHORIZATION_HEADER = "Authorization";

    // 사용자 권한 값의 KEY
    public static final String AUTHORIZATION_KEY = "auth";

    // Token 식별자. 토큰 앞에 붙일 용어(구분을 위해 한칸 띄워야 함)
    public static final String BEARER_PREFIX = "Bearer ";

    // 토큰 만료시간(임의로 작성)
    private final long TOKEN_TIME = 60 * 60 * 1000L; // 60분

    // import ... beans.factory.annotation.Value;
    // ${jwt.secret.key} = Application.properties 저장한 키값을 불러옴
    @Value("${jwt.secret.key}") // Base64 Encode 한 SecretKey
    private String secretKey;
    private Key key;
    private final SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

    // 로그 설정
    // 로깅: 애플리케이션 동작 시 프로젝트의 상태 및 동작을 시간순으로 기록
    // sl4j ... 로 쉽게 로깅 가능
    public static final Logger logger = LoggerFactory.getLogger("JWT 관련 로그");

    // 딱 한번만 받아오면 되는 값을 사용할 때 마다 새로운 요청을 방지하기 위해 사용
    // 생성자가 호출된 뒤에 호출이 됨
    @PostConstruct
    public void init() {
        // 값을 사용하기 위해 base64로 디코딩 하는 과정
        byte[] bytes = Base64.getDecoder().decode(secretKey);
        // 디코딩 된 값을 key로 반환
        key = Keys.hmacShaKeyFor(bytes);
    }
    // 토큰 생성
    public String createToken(String username, UserRoleEnum role) {
        Date date = new Date();

        return BEARER_PREFIX +
                Jwts.builder()
                        .setSubject(username) // 사용자 식별자값(ID)
                        .claim(AUTHORIZATION_KEY, role) // 사용자 권한
                        .setExpiration(new Date(date.getTime() + TOKEN_TIME)) // 만료 시간(현재시간 + 정의한 시간)
                        .setIssuedAt(date) // 발급일
                        .signWith(key, signatureAlgorithm) // ★★★암호화 알고리즘
                        .compact();
    }
    // JWT Cookie 에 저장(cookie저장 말고 header에 바로 넣어서 보낼 수도 있다)
    public void addJwtToCookie(String token, HttpServletResponse res) {
        try {
            token = URLEncoder.encode(token, "utf-8").replaceAll("\\+", "%20"); // Cookie Value 에는 공백이 불가능해서 encoding 진행

            Cookie cookie = new Cookie(AUTHORIZATION_HEADER, token); // Name-Value
            cookie.setPath("/");

            // Response 객체에 Cookie 추가
            res.addCookie(cookie);
        } catch (UnsupportedEncodingException e) {
            logger.error(e.getMessage());
        }
    }
    // JWT 토큰 substring(Bearer 의 공백 때문에 사용하지 않으면 에러발생)
    public String substringToken(String tokenValue) {
        // if 공백과 null이 아니라면 && token value가 BEARER로 시작한다면
        if (StringUtils.hasText(tokenValue) && tokenValue.startsWith(BEARER_PREFIX)) {
            // 1 2 3 4 5 6 7
            // B E A R E R " "
            // 공백까지 7글자
            return tokenValue.substring(7);
        }
        logger.error("Not Found Token");
        throw new NullPointerException("Not Found Token");
    }
    // 토큰 검증
    public boolean validateToken(String token) {
        try {
            // parserBuilder(): JWT 파서생성. 이후 JWT를 파싱할 수 있다.
            // setSigningKey(key): 암호화할때 사용할 키
            // build(): JWT파서를 빌드하여 완전한 JWT 파서 객체 생성
            // parseClaimsJws(token): JWT의 유효성을 검증하고, JWT에 포함된 클레임 (claims)을 추출
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (SecurityException | MalformedJwtException | SignatureException e) {
            logger.error("Invalid JWT signature, 유효하지 않는 JWT 서명 입니다.");
        } catch (ExpiredJwtException e) {
            logger.error("Expired JWT token, 만료된 JWT token 입니다.");
        } catch (UnsupportedJwtException e) {
            logger.error("Unsupported JWT token, 지원되지 않는 JWT 토큰 입니다.");
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims is empty, 잘못된 JWT 토큰 입니다.");
        }
        return false;
    }

    // 토큰에서 사용자 정보 가져오기
    public Claims getUserInfoFromToken(String token) {
        // 검증 후 body 부분에 존재하는 claims(데이터 집합)를 가져옴
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }
    // HttpServletRequest 에서 Cookie Value : JWT 가져오기
    public String getTokenFromRequest(HttpServletRequest req) {
        Cookie[] cookies = req.getCookies();
        if(cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(AUTHORIZATION_HEADER)) {
                    try {
                        return URLDecoder.decode(cookie.getValue(), "UTF-8"); // Encode 되어 넘어간 Value 다시 Decode
                    } catch (UnsupportedEncodingException e) {
                        return null;
                    }
                }
            }
        }
        return null;
    }
}





// Cookie에 들어있던 JWT 토큰을 substring

// JWT 검증

// JWT에서 사용자 정보 가져오기