package com.mysite.sbb.userTest;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.transaction.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional // 테스트 끝나면 반드시 롤백
public class UserRestControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	@DisplayName("회원가입 성공 테스트")
	void signupSucess() throws Exception {

		String jsonContent = "{" + "\"username\": \"junituser\"," + "\"password1\": \"123456\","
				+ "\"password2\": \"123456\"," + "\"email\": \"junit@test.com\"" + "}";

		// when & then: POST 요청을 보내고 결과 검증
		mockMvc.perform(post("/api/user/signup").contentType(APPLICATION_JSON).content(jsonContent))
				.andExpect(status().isCreated()).andExpect(content().string("회원 가입이 완료 되었습니다.")).andDo(print());

	}

	@Test
	@DisplayName("회원가입 실패 테스트")
	void signupFailPasswordMismatch() throws Exception {
		String jsonContent = "{" + "\"username\": \"junituser2\"," + "\"password1\": \"123456\","
				+ "\"password2\": \"999999\"," + "\"email\": \"junit2@test.com\"" + "}";

		mockMvc.perform(post("/api/user/signup").contentType(MediaType.APPLICATION_JSON).content(jsonContent))
				.andExpect(status().isBadRequest()).andExpect(jsonPath("$.password2").exists()).andDo(print());
	}

	@Test
	@DisplayName("회원가입 실패 - 이미 존재하는 사용자(아이디 또는 이메일 중복")
	void signupFailByDuplicateUser() throws Exception {

		String firstUserJson = "{" + "\"username\": \"duptest\"," + "\"password1\": \"123456\","
				+ "\"password2\": \"123456\"," + "\"email\": \"dup@test.com\"" + "}";

		mockMvc.perform(post("/api/user/signup").contentType(MediaType.APPLICATION_JSON).content(firstUserJson))
				.andExpect(status().isCreated());

		String duplicateUserJson = "{" + "\"username\": \"duptest\"," + // 👈 동일한 아이디
				"\"password1\": \"654321\"," + "\"password2\": \"654321\"," + "\"email\": \"dup@test.com\"" + // 👈 동일한
																												// 이메일
				"}";

		mockMvc.perform(post("/api/user/signup").contentType(MediaType.APPLICATION_JSON).content(duplicateUserJson))
				.andExpect(status().isConflict()) // 409 Conflict 기대
				.andExpect(content().string("이미 등록된 사용자입니다.")) // 우리가 지정한 메시지 확인
				.andDo(print());

	}

	@Test
	@DisplayName("로그인 성공 시 JWT 엑세스 토큰 발급 테스트")
	void loginSuccess() throws Exception {

		// given
		String jsonContent = "{\"username\": \"testtest\", \"password\": \"1234\"}";

		// when, then
		mockMvc.perform(post("/api/user/login").contentType(MediaType.APPLICATION_JSON).content(jsonContent))
				.andExpect(status().isOk()).andExpect(jsonPath("$.accessToken").exists()).andDo(print());

	}
}
