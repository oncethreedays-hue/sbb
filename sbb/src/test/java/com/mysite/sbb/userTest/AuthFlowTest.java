package com.mysite.sbb.userTest;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.Map;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.mysite.sbb.BaseTest;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.transaction.Transactional;

public class AuthFlowTest extends BaseTest {

	@Value("${jwt.secret}")
	String secret;

	// ---------- Helper ----------
	private ResultActions postQuestion(String bearerToken) throws Exception {
		var request = post("/api/questions").contentType(MediaType.APPLICATION_JSON)
				.content(toJson(Map.of("subject", "제목", "content", "내용")));
		if (bearerToken != null) {
			request.header("Authorization", "Bearer " + bearerToken);
		}
		return mockMvc.perform(request);
	}

	private ResultActions reissue(String refreshToken) throws Exception {
		return mockMvc.perform(post("/api/user/reissue").contentType(MediaType.APPLICATION_JSON)
				.content(toJson(Map.of("refreshToken", refreshToken))));
	}

	// 지정한 키/만료로 직겁 만든 토큰 (위조, 만료)
	private String buildToken(String signingSecret, String type, long expiresInMillis) {
		Key key = Keys.hmacShaKeyFor(signingSecret.getBytes(StandardCharsets.UTF_8));
		long now = System.currentTimeMillis();
		return Jwts.builder().setSubject("writer").claim("type", type).setIssuedAt(new Date(now - 120_000))
				.setExpiration(new Date(now + expiresInMillis)).signWith(key, SignatureAlgorithm.HS256).compact();
	}
	
	// ---------- Signup ----------
	@Test
	@DisplayName("가입 API: 201 후 그 계정으로 로그인 가능")
	void signupThenLogin() throws Exception {
		mockMvc.perform(post("/api/user/signup")
		.contentType(MediaType.APPLICATION_JSON)
		.content(toJson(Map.of("username", "newuser", "email", "new@test.com", "password1", "123", "password2", "123"))))
		.andExpect(status().isCreated());
		
		login("newuser", "123");
	}
}
