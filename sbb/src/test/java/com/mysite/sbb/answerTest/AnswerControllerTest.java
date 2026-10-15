package com.mysite.sbb.answerTest;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.http.MediaType;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.mysite.sbb.BaseTest;
import com.mysite.sbb.answer.service.AnswerService;
import com.mysite.sbb.question.entity.Question;
import com.mysite.sbb.question.repository.QuestionRepository;
import com.mysite.sbb.user.entity.SiteUser;

public class AnswerControllerTest extends BaseTest {

	@Autowired
	AnswerService answerService;
	@Autowired
	QuestionRepository questionRepository;

	private Integer questionId;

	@BeforeEach
	void setUpQuestion() {
		SiteUser writer = userRepository.findByUsername("writer").orElseThrow();
		questionId = questionRepository.save(new Question("질문 제목", "질문 내용", writer)).getId();
		flushAndClear();

	}

	private String content(String text) throws Exception {
		return toJson(Map.of("content", text));
	}

	private Integer newAnswer(String username, String text) {
		return answerService.create(questionId, text, username).getId();
	}
	// ---------- 등록 ----------

	@Test
	@DisplayName("POST 등록: 201과 생성된 답변 본문")
	void createAnswer() throws Exception {
		mockMvc.perform(post("/api/questions/{qid}/answers", questionId).with(user("writer"))
				.contentType(MediaType.APPLICATION_JSON).content(content("새 답변"))).andExpect(status().isCreated())
				.andExpect(jsonPath("$.content").value("새 답변")).andExpect(jsonPath(".authoerUsername").value("writer"))
				.andExpect(jsonPath("$.questionId").value(questionId)).andExpect(jsonPath("$.voterCount").value(0));
	}

	@Test
	@DisplayName("POST 등록: 빈 애용 400, 없는 질문 404, 비로그인 401")
	void createAnswerFail() throws Exception {
		mockMvc.perform(post("/api/questions/{qid}/answers", questionId).with(user("writer"))
				.contentType(MediaType.APPLICATION_JSON).content(content(""))).andExpect(status().isBadRequest());

		mockMvc.perform(post("/api/questions/{qid}/answers", 999999).with(user("writer"))
				.contentType(MediaType.APPLICATION_JSON).content(content("내용"))).andExpect(status().isNotFound());

		mockMvc.perform(post("/api/questions/{qid}/answers", questionId).contentType(MediaType.APPLICATION_JSON)
				.content(content("내용"))).andExpect(status().isUnauthorized());

	}
	
	@Test
	@DisplayName("JWT 토큰으로 실제 로그인한 사용자가 답변 등록")
	void createAnswerWithToken() throws Exception {
		String token = loginAndGetAccessToken("writer");
		
		mockMvc.perform(post("/api/questions/{qid}/answers", questionId).header("Authorization", ))
	}
}
