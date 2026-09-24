package com.mysite.sbb;

import com.mysite.sbb.jwt.JwtFilter;
import com.mysite.sbb.jwt.JwtTokenProvider;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.frameoptions.XFrameOptionsHeaderWriter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
	private final JwtTokenProvider jwtTokenProvider;

	SecurityConfig(JwtTokenProvider jwtTokenProvider) {
		this.jwtTokenProvider = jwtTokenProvider;
	}

	@Bean
	SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

		http
				// 1. CORS 설정 활성화
				.cors(Customizer.withDefaults())
				// 2. JWT는 세션을 사용하지 않으므로 STATELESS(무상태)로 설정
				.sessionManagement(
						(sessionManagement) -> sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				// 3. REST API / JWT 환경에서는 보통 CSRF 보안을 비활성화하거나 특정 경로만 예외 처리
				.csrf((csrf) -> csrf.disable()
				// 만약 완전한 API 서버라면 .disable() 처리하기도 합니다.
				)
				// 4. H2 콘솔 사용을 위한 프레임 옵션 설정 유지
				.headers((headers) -> headers.addHeaderWriter(
						new XFrameOptionsHeaderWriter(XFrameOptionsHeaderWriter.XFrameOptionsMode.SAMEORIGIN)))
				// 5. JWT 방식에서는 formLogin과 httpBasic 인증을 사용하지 않으므로 비활성화
				.formLogin((formLogin) -> formLogin.disable()).httpBasic((httpBasic) -> httpBasic.disable())

				// 6. 요청 경로별 권한 설정
				.authorizeHttpRequests((authorizeHttpRequests) -> authorizeHttpRequests
						.requestMatchers(new AntPathRequestMatcher("/**")).permitAll())
				// 7. UsernamePasswordAuthenticationFilter 전에 커스텀 JwtFilter를 먼저 실행하도록 등록
				.addFilterBefore(new JwtFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class);
		;

		return http.build();
	}

	// CORS 설정 빈
	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(List.of("http://localhost:3000")); // 리액트 주소
		configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
		configuration.setAllowedHeaders(List.of("*"));
		configuration.setAllowCredentials(true); // 자격증명(쿠키/토큰 헤더) 허용

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
			throws Exception {
		return authenticationConfiguration.getAuthenticationManager();
	}

}
