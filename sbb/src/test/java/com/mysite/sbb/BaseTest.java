package com.mysite.sbb;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.http.MediaType;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.transaction.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class BaseTest {

	@Autowired
	protected MockMvc mockMvc;

	@Autowired
	protected ObjectMapper objectMapper;

	// 로그인 후 토큰과 사용자 정보를 Map으로 반환
	protected Map<String, Object> loginAndGetUserInfo(String username, String password) throws Exception {
		String loginJson = String.format("{\"username\": \"%s\", \"password\": \"%s\"}", username, password);

		String responseBody = mockMvc
				.perform(post("/api/user/login").contentType(MediaType.APPLICATION_JSON).content(loginJson))
				.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

		return objectMapper.readValue(responseBody, new TypeReference<Map<String, Object>>() {
		});

	}

	// 로그인 및 질문 생성하고 ID를 반환하는 공통 헬퍼
	protected Integer createQuestionAndGetId(String username, String password, String subject, String content)
			throws Exception {
		Map<String, Object> userInfo = loginAndGetUserInfo(username, password);
		String accessToken = (String) userInfo.get("accessToken");

		String questionJson = String.format("{\"subject\": \"%s\", \"content\": \"%s\"}", subject, content);

		String responseBody = mockMvc
				.perform(post("/api/questions").header("Authorization", "Bearer " + accessToken)
						.contentType(MediaType.APPLICATION_JSON).content(questionJson))
				.andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();

		Map<String, Object> responseMap = objectMapper.readValue(responseBody,
				new TypeReference<Map<String, Object>>() {
				});
		return ((Number) responseMap.get("id")).intValue();
	}

	// 로그인 후 질문을 생성하고, 생성된 질문의 ID를 반환
	protected Integer createAnswerAndGetId(String username, String password, Integer questionId, String content)
			throws Exception {
		// 1. 로그인 + accessToken
		Map<String, Object> userInfo = loginAndGetUserInfo(username, password);
		String accessToken = (String) userInfo.get("accessToken");

		// 2. 답변 생성
		String answerJson = String.format("{\"content\": \"%s\"}", content);

		String responseContent = mockMvc
				.perform(post("/api/questions/{questionId}/answers", questionId)
						.header("Authorization", "Bearer " + accessToken).contentType(MediaType.APPLICATION_JSON)
						.content(answerJson))
				.andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();

		// 3. 답변 id 추출
		Map<String, Object> responseMap = objectMapper.readValue(responseContent,
				new TypeReference<Map<String, Object>>() {
				});

		return ((Number) responseMap.get("id")).intValue();
	}
}
