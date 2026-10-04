package com.mysite.sbb.jwt;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter{

	private final JwtTokenProvider jwtTokenProvider;
	
	public static final String AUTHORIZATION_HEADER = "Authorization"; // 오타 수정
	public static final String BEARER_PREFIX = "Bearer "; // 🟢 [중요] 뒤에 스페이스바 공백 필수!
	
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) 
			throws ServletException, IOException{
		
		// 1. Request Header에서 토큰을 꺼냄
		String jwt = resolveToken(request);
		
		// 2. validateToken으로 토큰 유효성 검사
		if (StringUtils.hasText(jwt) && jwtTokenProvider.validateToken(jwt)) {
			Authentication authentication = jwtTokenProvider.getAuthentication(jwt);
			SecurityContextHolder.getContext().setAuthentication(authentication);
		}
		
		filterChain.doFilter(request, response);		
	}
	
	// Request Header에서 토큰 정보 추출
	private String resolveToken(HttpServletRequest request) {
		String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
		if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
			return bearerToken.substring(7); // "Bearer "의 길이는 7글자이므로 7번 인덱스부터 토큰 추출
		}
		return null;
	}
}