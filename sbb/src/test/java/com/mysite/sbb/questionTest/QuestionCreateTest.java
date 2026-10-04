package com.mysite.sbb.questionTest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import com.mysite.sbb.BaseTest;

public class QuestionCreateTest extends BaseTest{
	
	@Test
	@DisplayName("로그인 후 질문 작성 성공 테스트")
	void createQuestionAfterLogin() throws Exception {
		Map<String, Object> userInfo = loginAndGetUserInfo("testtest", "1234");
		String accessToken = (String) userInfo.get("accessToken");
		
		String questionJson = "{\"subject\": \"JUnit 테스트 제목입니다.\", \"content\": \"JUnit 테스트 내용입니다.\"}";
		
		mockMvc.perform(post("/api/questions")
				.header("Authorization", "Bearer " + accessToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(questionJson))
		.andExpect(status().isCreated())
		.andDo(print());
	}
	
	@Test
	@DisplayName("질문 생성 헬퍼 메서드 테스트(ID 반환 확인)")
	void testCreateQuestionAndGetId() throws Exception {
		Integer questionId = createQuestionAndGetId("testtest", "1234", "헬퍼 테스트 제목", "헬퍼 테스트 내용");
		
		Assertions.assertThat(questionId).isNotNull();
		Assertions.assertThat(questionId).isGreaterThan(0);
		
		System.out.println("생성된 질문 ID:" + questionId);
		
	}

}
