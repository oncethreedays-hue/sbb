package com.mysite.sbb.answerTest;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.*;

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
				.andExpect(jsonPath("$.content").value("새 답변")).andExpect(jsonPath(".authorUsername").value("writer"))
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

		mockMvc.perform(post("/api/questions/{qid}/answers", questionId).header("Authorization", "Bearer " + token)
				.contentType(MediaType.APPLICATION_JSON).content(content("토큰으로 등록"))).andExpect(status().isCreated())
				.andExpect(jsonPath("$.authorUsername").value("writer"));
	}

	// ---------- 목록 ----------
	@Test
	@DisplayName("GET 목록: 비로그인도 조회 가능, 페이징과 최신순")
	void listAnswer() throws Exception {
		for (int i = 1; i <= 12; i++) {
			newAnswer("writer", "답변" + i);
		}
		flushAndClear();

		mockMvc.perform(get("/api/questions/{qid}/answers", questionId)).andExpect(status().isOk())
				.andExpect(jsonPath("$.content", hasSize(10))).andExpect(jsonPath("$.content[0].content").value("답변12"))
				.andExpect(jsonPath("$.totalElements").value(12));

		mockMvc.perform(get("/api/questions/{qid}/answers", questionId).param("page", "1")).andExpect(status().isOk())
				.andExpect(jsonPath("$.content", hasSize(2)));
	}

	@Test
	@DisplayName("GET 목록: 추천순 정렬, 없느 질문은 404")
	void listAnswerSortAndNotFound() throws Exception {
		Integer a1 = newAnswer("writer", "답변1");
		newAnswer("writer", "답변2");
		answerService.vote(a1, "other");
		flushAndClear();

		mockMvc.perform(get("/api/questions/{qid}/answers", questionId).param("sort", "votes"))
				.andExpect(status().isOk()).andExpect(jsonPath("$.content[0].id").value(a1))
				.andExpect(jsonPath("$.content[0].voterCount").value(1));

		mockMvc.perform(get("/api/questions/{qid}/answers", 99999)).andExpect(status().isNotFound());
	}

	@Test
	@DisplayName("질문 상세 응답에 답변  목록이 포함된다 (AnswerResponseDto.from 연결확인)")
	void questionDetailIncludesAnswers() throws Exception {
		newAnswer("writer", "상세에 포함된 답변");
		flushAndClear();

		mockMvc.perform(get("/api/questions/{id}", questionId)).andExpect(status().isOk())
				.andExpect(jsonPath("$.answerList", hasSize(1)))
				.andExpect(jsonPath("$.answerList[0].content").value("상세에 포함된 답변"));
	}

	// ---------- 수정 / 삭제 ----------
	@Test
	@DisplayName("PUT 수정: 작성자 204, 타인 403, 없는 답변 404")
	void modifyAnswer() throws Exception {
		Integer id = newAnswer("writer", "원래 내용");
		flushAndClear();

		mockMvc.perform(put("/api/answers/{id}", id).with(user("other")).contentType(MediaType.APPLICATION_JSON)
				.content(content("해킹"))).andExpect(status().isForbidden());

		mockMvc.perform(put("/api/answers/{id}", id).with(user("writer")).contentType(MediaType.APPLICATION_JSON)
				.content(content("수정 내용"))).andExpect(status().isNoContent());

		mockMvc.perform(put("/api/answers/{id}", 99999).with(user("writer")).contentType(MediaType.APPLICATION_JSON)
				.content(content("내용"))).andExpect(status().isNotFound());

		flushAndClear();
		mockMvc.perform(get("/api/questions/{qid}/answers", questionId))
				.andExpect(jsonPath("$.content[0].content").value("수정 내용"));
	}

	@Test
	@DisplayName("DELETE 삭제: 타인 403, 작성자 204, 목록에서 사라짐")
	void deleteAnswer() throws Exception {
		Integer id = newAnswer("writer", "내용");
		flushAndClear();

		mockMvc.perform(delete("/api/answers/{id}", id).with(user("other"))).andExpect(status().isForbidden());
		mockMvc.perform(delete("/api/answers/{id}", id).with(user("writer"))).andExpect(status().isNoContent());

		flushAndClear();

		mockMvc.perform(get("/api/questions/{qid}/answers", questionId)).andExpect(jsonPath("$.content", hasSize(0)));

	}

	// ---------- 추천 ----------

	@Test
	@DisplayName("추천 / 중복 추천 / 추천 취소 (POST, DELETE)")
	void voteCancel() throws Exception {
		Integer id = newAnswer("writer", "내용");
		flushAndClear();

		mockMvc.perform(post("/api/answers/{id}/vote", id).with(user("other"))).andExpect(status().isNoContent());
		mockMvc.perform(post("/api/answers/{id}/vote", id).with(user("other"))) // 중복
				.andExpect(status().isNoContent());
		flushAndClear();
		mockMvc.perform(get("/api/questions/{qid}/answers", questionId))
				.andExpect(jsonPath("$.content[0].voterCount").value(1));

		mockMvc.perform(delete("/api/answers/{id}/vote", id).with(user("other"))).andExpect(status().isNoContent());
		flushAndClear();
		mockMvc.perform(get("/api/questions/{qid}/answers", questionId))
				.andExpect(jsonPath("$.content[0].voterCount").value(0));
	}

//	@Test
//	@Dis

}
