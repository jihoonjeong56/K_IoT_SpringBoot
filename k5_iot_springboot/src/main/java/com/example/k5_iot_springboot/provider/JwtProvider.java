package com.example.k5_iot_springboot.provider;


import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/*
    === JwtProvider ===
    : JWT(JSON Web Token)을 생성하고 검증하는 역할

    cf) JWT
        : 사용자 정보를 암호화된 토큰으로 저장
        - 서버에 요청할 때마다 전달 가능(사용자 정보 확인용)
        >> 주로 로그인 인증에 사용

        +) HS256 암호화 알고리즘 사용한 JWT 서명
            - 비밀키는 BASE64로 인코딩 지원
            - JWT 만료기간은 10시간으로 지정
                >> 환경변수 설정(jwt.secret / jwt.expiration)
 */
@Component
// cf) @Component (클래스 레벨 선언) - 스프링 런타임 시 컴포넌트 스캔을 통해 자동으로 빈을 찾고 등록(의존성 주임)
// @Bean (메서드 레벨 선언)- 반환되는 객체를 개발자가 수동으로 빈 등록
public class JwtProvider {
    public static final String BEARER_PREFIX = "Bearer "; // removeBearer 에서 사용

    // 환경변수에 지정한 비밀키와 만료 시간 저장 변수 선언
    private final SecretKey key;
    private final int jwtExpirationMs;
    private final int clockSkewSeconds;
    //성능/안전 : 파서를 생성자에서 1회 구성하여 재사용
    private final JwtParser parser;


    public JwtProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration}") int jwtExpirationMs,
            @Value("${jwt.clock-skew-seconds:0}") int clockSkewSeconds // 기본 0

    ) {
        // 생성자 : JWTProvider 객체 생성시 비밀키와 만료시간 초기화
        // 키 강도 검증(Base64 디코딩 후 256 비트 이상 권장)
        byte[] secretBytes = Decoders.BASE64.decode(secret);
        if (secretBytes.length < 32) {
            // 32 byte = 256 bit
            throw new IllegalArgumentException("jwt.secret 은 항상 256비트 이상을 권장합니다.");
        }
        //HMAC-SHA 알고리즘으로 암호화된 키 생성
        this.key = Keys.hmacShaKeyFor(secretBytes);
        this.jwtExpirationMs = jwtExpirationMs;
        this.clockSkewSeconds = Math.max(clockSkewSeconds, 0);

        this.parser = Jwts.parser()
                .verifyWith(this.key)
                .build();
    }
    /* ========
     * 토큰 생성
     * ========*/

    /**
     * 엑세스 토큰 생성 : subject=sub(username), roles는 커스텀 클레임
     */
    public String generateJwtToken(String username, Set<String> roles) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                // 표준 클레임 sub(Subject)에 사용자 아이디(또는 고유 식별자) 설정
                .subject(username)
                .claim("roles", roles) // 커스텀 클레임 키에 권한 목록 저장
                .issuedAt(new Date(now)) // 표준 클레임에 현재 시간 설정(발행시간)
                .expiration(new Date(now + jwtExpirationMs)) // 현재 시간에 만료시간을 던한 설정(만료시간)
                //.signWith(key, SignatureAlgorithm.HS256)
                .signWith(key) // 서명 키로 서명(자동 HS256 선택) - 비밀키 설정
                .compact(); // 빌더를 압축하여 최종 JWT 문자열 생성
    }
    /* ========
     * Bearer 처리
     * ========*/

    /**
     * HTTP Authorization 헤더에서 "Bearer " 제거
     */
    public String removeBearer(String bearerToken) {// 입력 : "Bearer <token>"
        if (bearerToken == null || !bearerToken.startsWith(BEARER_PREFIX)) {
            throw new IllegalArgumentException("Authorization 형식이 올바르지 앖습니다.");
        }
        // 인자가 한개인 경우: index 0 부터 인자값 "전"까지 잘라내기
        return bearerToken.substring(BEARER_PREFIX.length()).trim();  // 순수 토큰 반환
    }

    /* ========
     * 검증/파싱
     * ========*/

    /**
     * 내부파싱(검증 포함)/ 만료 시 clock-skew 허용 옵션
     */
    private Claims parseClaimsInternal(String token, boolean allowClockSkewOnExpiry) {
        //allowClockSkewOnExpiry : 만료 직후 허용 오차 적용 여부
        try {
            return parser.parseSignedClaims(token).getPayload();
            // 서명 및 기본 구조 검증 후 페이로드(claims)만 추출해 반환
        } catch (ExpiredJwtException ex) {
            // 토큰이 만료된 경우 발생한는 JJWT 전용 예외 처리
            if (allowClockSkewOnExpiry && clockSkewSeconds > 0 && ex.getClaims() != null) {
                Date exp = ex.getClaims().getExpiration();
                if (exp != null) {
                    long skewMs = clockSkewSeconds * 1000L; // 허용 오차 (초)를 밀리초로 변환\
                    long now = System.currentTimeMillis();
                    if (now - exp.getTime() <= skewMs) {
                        //현지 시간 - 만료시간 < = 허용 오차면 "막 완료" 간주
                        return ex.getClaims();
                    }
                }
            }
            throw ex; // 허용 오차 범위를 벗어나면 원래의 만료 예외를 다시 던짐
        }
    }

    /**
     * 토큰 유효성 검사(서명/만료포함)/ clock-skew 허용 적용
     */
    public boolean isValidToken(String tokenWithoutBearer) {
        try {
            parseClaimsInternal(tokenWithoutBearer, true);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Claims 추출(검증 포함)
     */
    public Claims getClaims(String tokenWithoutBearer) {
        return parseClaimsInternal(tokenWithoutBearer, true);

    }

    public String getUsernameFromJwt(String tokenWithoutBearer) {
        return getClaims(tokenWithoutBearer).getSubject();
    }

    /**
     * roles >> Set<String> 변환
     */
    @SuppressWarnings("unchecked") // 제네릭 캐스팅 경고 억제(런타임 타입 확인으로 보완)
    public Set<String> getRolesFromJwt(String tokenWithoutBearer) {
        Object raw = getClaims(tokenWithoutBearer).get("roles");
        if (raw == null) return Set.of(); // 권한 없음
        if(raw instanceof List<?> list){
            Set<String> result = new HashSet<>(); // 중복 제거 목적
            for (Object o: list) if(o!= null)result.add(o.toString());
            return result;
        }
        if(raw instanceof Set<?> set){
            Set<String> result = new HashSet<>();
            for (Object o: set) if(o!= null)result.add(o.toString());
            return result;
        }
        return Set.of(raw.toString());
    }
    /**남은 만료시간(ms)이 음수면 이미 만료*/
    public long getRemainingMillis(String tokenWithoutBearer){
        Claims c = parseClaimsInternal(tokenWithoutBearer, true);
        return c.getExpiration().getTime() - System.currentTimeMillis();
    }
}
