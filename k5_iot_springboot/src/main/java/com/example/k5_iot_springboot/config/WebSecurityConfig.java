package com.example.k5_iot_springboot.config;

import com.example.k5_iot_springboot.filter.JwtAuthenticationFilter;

import com.example.k5_iot_springboot.handler.JsonAccessDeniedHandler;
import com.example.k5_iot_springboot.handler.JsonAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/*
    === WebSecurityConfig
    : 스프링 시큐리티 전체 규칙 설정
    : Spring Security를 통해 웹 애플리케이션의 보안을 구성(보안 환경설정)
    - (세션 대신) JWT 필터를 적용하여 인증 처리, CORS 및 CSRF 설정을 비활성화
    >> 서번간의 통신을 원활하게 처리
    - URL별 접근 권한, 필터 순서(JwtAuthenticationFilter를 UsernamePasswordAuthenticationFilter 앞에 배치) 등

    #Stateless(무상태성, 세션 미사용) + CSRF 비활성(JWT 조합)

 */
@Configuration // 설정 파일을 만들기 위한 어노테이션(Bean 등록), 해당 클래스가 Spring의 설정 클래스로 사용됨을 명시
@EnableWebSecurity // Spring Security의 웹 보안 활성화 - 스프링 시큐리티 기능을 웹 계층에 적용
@RequiredArgsConstructor

public class WebSecurityConfig {
    private final JwtAuthenticationFilter jwtAuthenticationFilter; // 사용자 정의 JWT 검증 필터
    private final JsonAuthenticationEntryPoint jsonAuthenticationEntryPoint;
    private final JsonAccessDeniedHandler jsonAccessDeniedHandler;

    // CORS 관련 속성을 properties 에서 주입받아 콤마(,) 로 분리하여 저장하는 데이터
    @Value("${cors.allowed-origins:*}") // https://app.example.com, https://admin.example.com
    private String allowedOrigins;

    @Value("${cors.allowed-headers:*}")
    private String allowedHeaders;

    @Value("${cors.allowed-method:GET, POST, PUT, PATCH, DELETE, OPTIONS}")
    private String allowedMethods;

    @Value("${cors.exposed-header:Authorization, Set-cookie}")
    private String exposedHeaders;

    @Value("${Security.h2-console:true}") //개발에 대한 편의성을 위해 로컬 개발 시 true값 설정
    // 개발용 H2 콘솔 접근 허용 여부
    private boolean h2ConsoleEnabled;

    /*======
     * PasswordEncoder / AuthManager
     * =====*/

    /**
     * 1)
     * 비밀번호 인코더 : 실무 기본 BCrypt (강도 기본값)
     */
    @Bean // 메서드 반환 객체를 스프링 빈으로 등록
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
        // >> 추후 회원가입/로그인 시 passwordEncoder.matches(raw, encoded); 비밀번호 비교
    }

    /**
     * 3)Spring이 구성한 것(AuthenticationManager)을 노출 - 스프링 기본 구성 재사용
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        // 표준 인증 흐름(UserDetailsService 등)을 사용할 수 있음
        return configuration.getAuthenticationManager();
    }

    /* ======
     * CORS
     * cf) CORS(Cross Origin Resource Sharing)
     * : 브라우저(EX. 4178) 에서 다른 도메인(Tomcat 서버: 8080)으로 부터 리소스를 요청할 때 발생하는 보안 정책
     * - REST API 사용시 다른 출처(도메인)에서 API 에 접근할 수 있도록 허용하는 정책
     *
     * CorsConfigurationSource 메서드
     * : 특정 출처에서 온 HTTP 요청을 허용하거나 거부할 수 있는 필터
     * =====*/
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration(); // 출처/헤더/메서드/쿠키어용 등을 담는 CORS 정책 객체

        List<String> origins = splitToList(allowedOrigins);

        // 1) 인증 정모(쿠키/Authorization) 허용
        config.setAllowCredentials(true);                       // 1) 인증 정모(쿠키/Authorization) 허용
        //config.setAllowedOriginPatterns(origins);              // 2) Origin 설정 - 도메인 매칭
        // >> 허용 origin을 * 로 둘 수 없음(반드시 구체적인 도메인이어야 함)
        config.setAllowedHeaders(splitToList(allowedHeaders)); // 3) 요청헤더 화이트 리스트 검증
        config.setAllowedMethods(splitToList(allowedMethods)); // 4) 허용 메서드
        config.setExposedHeaders(splitToList(exposedHeaders)); // 5) 응답에서 클라이언트가 읽을 수 있는 헤더

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config); // 모든 경로에 동일 CORS 정책 적용
        return source;

    }

    /*==========
     *Security Filter Chain
     *: 보안 필터 체인 정의, 특정 HTTP 요청에 대한 웹 기반 보안 구성
     *- CSRF 보호를 비활성화, CORS 정책을 활성화
     *=========== */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationEntryPoint authenticationEntryPoint, AccessDeniedHandler accessDeniedHandler) throws Exception {
        http
                // 1) CSRF 비활성(JWT + REST 조합에서 일반적)
                .csrf(AbstractHttpConfigurer::disable)
                // 2) 세션 미사용 (완전 무상태(Stateless) 모든 요청은 토큰만으로 인증/인가 진행)
                .sessionManagement(sm
                        -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 3) CORS 활성화(위의 Bean을 사용)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // 4) 예외 처리 지점 (필요 시 커스텀 핸들러 연결)
                // : 폼 로그인 /HTTP Basic 비활성 - JWT 만 사용
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .exceptionHandling(ex -> ex.authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                );

        // H2 DB 콘솔은 웹 브라우저에 iframe 태그를 사용하여 페이지를 띄움
        // : 로컬 개발 환경에서도 H2 콘솔을 보려면 해당 설정 필요
        if (h2ConsoleEnabled) {
            http.headers(headers
                    -> headers.frameOptions(frame -> frame.sameOrigin()));
            //-> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin);
        }

        // 5) URL 인가 규칙
        http
                .authorizeHttpRequests(auth -> {
                            // H2 콘솔 접근 권한 열기(개발 환경에서 DB를 직접 확인하기 때문에 -> 인증 절차 없이 접속할 수 있도록 예외) - 개발편의를 위해(실무에서 배포시에는 하면 안됨;)
                            if (h2ConsoleEnabled) auth.requestMatchers("/h2-console/**").permitAll();
                            auth
                                    // PreFlight 허용
                                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                                    // 인증 / 회원가입 등 공개 엔드 포인트 - 토큰이 필요없는 기능
                                    .requestMatchers("/api/v1/auth/**").permitAll()
                                    // 읽기 공개 예시 (게시글 목록, 조회 등)
                                    .requestMatchers(HttpMethod.GET, "/api/v1/boards/**").permitAll()
                                    .anyRequest().authenticated(); // 나머지는 인증 필요 - JWT 토큰이 있어야 접근 가능
                        }
                );
//        if (h2ConsoleEnabled) {
//            http.headers(headers
//                    -> headers.frameOptions(frame -> frame.sameOrigin()));
//            http.authorizeHttpRequests(auth
//                    -> auth.requestMatchers("/h2-console/**").permitAll());
//        }

        // JWT 인증 필터를 UsernamePasswordAuthenticationFilter 앞에 배치
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // 문자열 콤마구분을 리스트로 변환
    private static List<String> splitToList(String csv) {
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();
    }


}
