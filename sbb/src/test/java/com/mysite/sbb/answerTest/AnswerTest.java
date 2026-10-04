package com.mysite.sbb.answerTest;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import com.mysite.sbb.BaseTest;

public class AnswerTest extends BaseTest{
	
	@Test
	@DisplayName("로그인 > 질문작성 > 답변 작성 테스트")
	void createAsnwerSuccess() throws Exception {
		
		String testUsername = "testtest";
		String testPassword = "1234";
		String testSubject = "답변 테스트용 질문 제목";
		String testContent = "답변 테스트용 질문 내용";

		// 1. 로그인 & AccessToken
		Map<String, Object> userInfor = loginAndGetUserInfo(testUsername, testPassword);
		String accessToken = (String) userInfor.get("accessToken");
		
		// 2. 질문생성
		Integer questionId = createQuestionAndGetId(testUsername, testPassword, testSubject, testContent);
		
		// 3. 답변
		String answerJson = "{\"content\": \"이것은 테스트 답변 내용입니다.\"}";
		
		mockMvc.perform(post("/api/questions/{questionId}/answers", questionId)
				.header("Authorization", "Bearer " + accessToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(answerJson))
		.andExpect(status().isCreated())
		.andExpect(jsonPath("$.id").exists())
		.andExpect(jsonPath("$.content").value("이것은 테스트 답변 내용입니다."))
		.andExpect(jsonPath("$.authorUsername").value(testUsername))
		.andExpect(jsonPath("$.questionId").value(questionId))
		.andDo(print());
		
	}
	
}
