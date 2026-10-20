package com.mysite.sbb.userTest;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.Map;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.http.MediaType;

import org.springframework.test.web.servlet.ResultActions;

import com.mysite.sbb.BaseTest;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

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
		mockMvc.perform(post("/api/user/signup").contentType(MediaType.APPLICATION_JSON).content(
				toJson(Map.of("username", "newuser", "email", "new@test.com", "password1", "12345678", "password2", "12345678"))))
				.andExpect(status().isCreated());

		login("newuser", "12345678");
	}

	@Test
	@DisplayName("가입 API: 아이디 중복 409, 비밀번호 불일치 400, 빈 아이디 400")
	void signupFail() throws Exception {
		mockMvc.perform(post("/api/user/signup").contentType(MediaType.APPLICATION_JSON).content(
				toJson(Map.of("username", "writer", "email", "x@test.com", "password1", "12345678", "password2", "12345678"))))
				.andExpect(status().isConflict());

		mockMvc.perform(post("/api/user/signup").contentType(MediaType.APPLICATION_JSON).content(
				toJson(Map.of("username", "newuser", "email", "new@test.com", "password1", "12345678", "password2", "456"))))
				.andExpect(status().isBadRequest());

		mockMvc.perform(post("/api/user/signup").contentType(MediaType.APPLICATION_JSON).content(
				toJson(Map.of("username", "", "email", "new@test.com", "password1", "12345678", "password2", "12345678"))))
				.andExpect(status().isBadRequest());
	}

	// ---------- login ----------
	@Test
	@DisplayName("로그인 API:성공 시 토큰 반환, 실패 시 401")
	void loginApi() throws Exception {
		mockMvc.perform(post("/api/user/login").contentType(MediaType.APPLICATION_JSON)
				.content(toJson(Map.of("username", "writer", "password", PASSWORD)))).andExpect(status().isOk())
				.andExpect(jsonPath("$.accessToken", notNullValue()))
				.andExpect(jsonPath("$.refreshToken", notNullValue())).andExpect(jsonPath("$.username").value("writer"));

		mockMvc.perform(post("/api/user/login").contentType(MediaType.APPLICATION_JSON)
				.content(toJson(Map.of("username", "writer", "password", "wrong"))))
				.andExpect(status().isUnauthorized());
	}

	// ---------- 토큰으로 보호된 API호출 ----------
	@Test
	@DisplayName("Access Token이면 보호된 API 성공")
	void accessTokenWorks() throws Exception {
		String accessToken = loginAndGetAccessToken("writer");
		postQuestion(accessToken).andExpect(status().isCreated());
	}

	@Test
	@DisplayName("토큰 없이 보호된 API 호출하면 401, 공개 API(GET)는 200")
	void noToken() throws Exception {
		postQuestion(null).andExpect(status().isUnauthorized());
		mockMvc.perform(get("/api/questions")).andExpect(status().isOk());
	}

	@Test
	@DisplayName("Refresh Token을 Bearer에 넣으면 401 (용도 구분)")
	void refreshTokenCannotCallApi() throws Exception {
		String refreshToken = (String) login("writer", PASSWORD).get("refreshToken");
		postQuestion(refreshToken).andExpect(status().isUnauthorized());
	}

	@Test
	@DisplayName("깨진 토큰, 다른 키로 서명한 위조 토큰, 만료 토큰은 401")
	void invalidTokens() throws Exception {
		postQuestion("garbage.token.value").andExpect(status().isUnauthorized());

		String forged = buildToken("another-secret-key-another-secret-key-another-secret-key!!", "access", 60_000);
		postQuestion(forged).andExpect(status().isUnauthorized());

		String expired = buildToken(secret, "access", -60_000);
		postQuestion(expired).andExpect(status().isUnauthorized());
	}

	// ----------- 재발급 / 로그아웃 -----------
	@Test
	@DisplayName("재발급: 새 Access Token으로 API 호출가능")
	void reissueFlow() throws Exception {
		String refreshToken = (String) login("writer", PASSWORD).get("refreshToken");

		String body = reissue(refreshToken).andExpect(status().isOk())
				.andExpect(jsonPath("$.accessToken", notNullValue())).andReturn().getResponse().getContentAsString();
		String newAccessToken = (String) objectMapper.readValue(body, Map.class).get("accessToken");

		postQuestion(newAccessToken).andExpect(status().isCreated());
	}

	@Test
	@DisplayName("재발급 실패: Access Token 제출 401, 빈 값 400")
	void reissueFail() throws Exception {
		String accessToken = loginAndGetAccessToken("writer");
		reissue(accessToken).andExpect(status().isUnauthorized());

		mockMvc.perform(post("/api/user/reissue").contentType(MediaType.APPLICATION_JSON)
				.content(toJson(Map.of("refreshToken", "")))).andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("로그아웃 후 기존 refresh Token으로 재발급 불가")
	void logoutThenReissue() throws Exception {
		Map<String, Object> info = login("writer", PASSWORD);
		String accessToken = (String) info.get("accessToken");
		String refreshToken = (String) info.get("refreshToken");

		mockMvc.perform(post("/api/user/logout").header("Authorization", "Bearer " + accessToken))
				.andExpect(status().isNoContent());

		reissue(refreshToken).andExpect(status().isUnauthorized());
	}

	@Test
	@DisplayName("토큰 없이 로그아웃하면 401")
	void logoutWithoutToken() throws Exception {
		mockMvc.perform(post("/api/user/logout")).andExpect(status().isUnauthorized());
	}

	@Test
	@DisplayName("검증 실패 응답ㅣ 400 + 필드별 메시지, 존재하지 않는 경로 에러도 JSON")
	void errorResponseShape() throws Exception {
		mockMvc.perform(post("/api/user/signup").contentType(MediaType.APPLICATION_JSON).content(
				toJson(Map.of("username", "", "email", "x@test.com", "password1", "1234", "password2", "1234"))))
				.andDo(print()).andExpect(status().isBadRequest()).andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.fieldErrors.username").exists());
	}
}
