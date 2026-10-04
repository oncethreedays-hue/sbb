package com.mysite.sbb.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.ArrayList;
import java.util.Date;

@Component
public class JwtTokenProvider {
	
	
	private final Key key;
	
	 // accessToken 유효시간: 1시간
	private final long ACCESS_TOKEN_VALID_TIME = 1000L * 60 * 60;
	// refreshToken 유효시간: 2주
	private final long REFRESH_TOKEN_VALID_TIME = 1000L * 60 * 60 * 24 * 14;
	
	public JwtTokenProvider(@Value("${jwt.secret}") String secretKeyString) {
		byte[] bytes = secretKeyString.getBytes(StandardCharsets.UTF_8);
		this.key = Keys.hmacShaKeyFor(bytes);
	}
			
	// 1-1 Access Token 생성
	public String createAccessToken(String username) {
		return createToken(username, ACCESS_TOKEN_VALID_TIME);
	}
	// 1-2 Refresh Token 생성
	public String createRefreshToken(String username) {
		return createToken(username, REFRESH_TOKEN_VALID_TIME);
	}
	
	// JWT 토큰 생성
	public String createToken(String username, long validityTime) {
		Claims claims = Jwts.claims().setSubject(username);
		Date now = new Date();
		Date validity = new Date(now.getTime() + validityTime);

		return Jwts.builder()
				.setClaims(claims)
				.setIssuedAt(now)
				.setExpiration(validity)
				.signWith(key, SignatureAlgorithm.HS256)
				.compact();
	}

	// JWT 토큰에서 인증 정보(Authentication) 조회
	public Authentication getAuthentication(String token) {
		String username = getUsernameFromToken(token);
		UserDetails principal = new User(username, "", new ArrayList<>());
		return new UsernamePasswordAuthenticationToken(principal, "", principal.getAuthorities());
	}

	// 토큰에서 회원에서 이름(Username) 추출
	public String getUsernameFromToken(String token) {
		return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody().getSubject();

	}

	// 토큰 유효성 검증
	public boolean validateToken(String token) {
		try {
			Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
			return true;
		} catch (JwtException | IllegalArgumentException e) {
			return false;
		}

	}
}
