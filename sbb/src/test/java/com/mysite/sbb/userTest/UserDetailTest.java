package com.mysite.sbb.userTest;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.mysite.sbb.BaseTest;
import com.mysite.sbb.answer.entity.Answer;
import com.mysite.sbb.answer.repository.AnswerRepository;
import com.mysite.sbb.question.entity.Question;
import com.mysite.sbb.question.repository.QuestionRepository;
import com.mysite.sbb.user.UserRole;
import com.mysite.sbb.user.entity.SiteUser;

public class UserDetailTest extends BaseTest {

	@Autowired
	QuestionRepository questionRepository;
	@Autowired
	AnswerRepository answerRepository;

	private SiteUser writer;
	private SiteUser other;
	private Question firestQuestion;
	private Question lastQuestion;

	@BeforeEach
	void setUpData() {
		writer = userRepository.findByUsername("writer").orElseThrow();
		other = userRepository.findByUsername("other").orElseThrow();

		Question last = null;
		for (int i = 1; i <= 7; i++) {
			Question q = questionRepository.save(new Question("질문" + i, "내용" + i, writer));
			if (i == 1)
				firestQuestion = q;
			last = q;
		}
		lastQuestion = last;

		for (int i = 1; i <= 6; i++) {
			answerRepository.save(new Answer("내 답변" + i, firestQuestion, writer));
		}
		answerRepository.save(new Answer("남의 답변1", lastQuestion, other));
		answerRepository.save(new Answer("남의 답변2", lastQuestion, other));

		flushAndClear();
	}

	@Test
	@DisplayName("기본 정보와 개수: 내 글 7, 내 댓글 6 (다른 사용자 것은 제외)")
	void summary() throws Exception {
		mockMvc.perform(get("/api/user/me").with(user("writer"))).andExpect(status().isOk())
				.andExpect(jsonPath("$.username").value("writer"))
				.andExpect(jsonPath("$.email").value("writer@test.com")).andExpect(jsonPath("$.questionCount").value(7))
				.andExpect(jsonPath("$.answerCount").value(6));
	}

	@Test
	@DisplayName("최근 글: 5개만, 최신순, 각 글의 답변 수 포함")
	void recentQuestions() throws Exception {
		mockMvc.perform(get("/api/user/me").with(user("writer"))).andExpect(jsonPath("$.recentQuestions", hasSize(5)))
				.andExpect(jsonPath("$.recentQuestions[0].id").value(lastQuestion.getId()))
				.andExpect(jsonPath("$.recentQuestions[0].subject").value("질문7"))
				.andExpect(jsonPath("$.recentQuestions[0].answerCount").value(2))
				.andExpect(jsonPath("$.recentQuestions[4].subject").value("질문3"));

	}

	@Test
	@DisplayName("최근 댓글: 5개만, 최신순, 달린 글의 id와 제목 포함")
	void recentAnswers() throws Exception {
		mockMvc.perform(get("/api/user/me").with(user("writer"))).andExpect(jsonPath("$.recentAnswers", hasSize(5)))
				.andExpect(jsonPath("$.recentAnswers[0].content").value("내 답변6"))
				.andExpect(jsonPath("$.recentAnswers[0].questionId").value(firestQuestion.getId()))
				.andExpect(jsonPath("$.recentAnswers[0].questionSubject").value("질문1"))
				.andExpect(jsonPath("$.recentAnswers[4].content").value("내 답변2"));
	}

	@Test
	@DisplayName("다른 사용자의 마이페이지에는 그 사용자의 데이터만")
	void otherUser() throws Exception {
		mockMvc.perform(get("/api/user/me").with(user("other"))).andExpect(status().isOk())
				.andExpect(jsonPath("$.questionCount").value(0)).andExpect(jsonPath("$.answerCount").value(2))
				.andExpect(jsonPath("$.recentQuestions", hasSize(0))).andExpect(jsonPath("$.recentAnswers", hasSize(2)))
				.andExpect(jsonPath("$.recentAnswers[0].content").value("남의 답변2"));
	}

	@Test
	@DisplayName("활동이 없는 사용자: 개수 0, 목록은 null이 아닌 빈 배열")
	void emptyUser() throws Exception {
		userRepository.save(new SiteUser("newbie", passwordEncoder.encode(PASSWORD), "newbie@test.com", UserRole.USER));

		mockMvc.perform(get("/api/user/me").with(user("newbie"))).andExpect(jsonPath("$.questionCount").value(0))
				.andExpect(jsonPath("$.answerCount").value(0)).andExpect(jsonPath("$.recentQuestions", hasSize(0)))
				.andExpect(jsonPath("$.recentAnswers", hasSize(0)));
	}

	@Test
	@DisplayName("인증: 비로그인 401, 실제 JWT 토큰으로 조회 성공, 없는 사용자는 404")
	void authentication() throws Exception {
		mockMvc.perform(get("/api/user/me")).andExpect(status().isUnauthorized());

		String token = loginAndGetAccessToken("writer");
		mockMvc.perform(get("/api/user/me").header("Authorization", "Bearer " + token)).andExpect(status().isOk())
				.andExpect(jsonPath("$.username").value("writer"));

		mockMvc.perform(get("/api/user/me").with(user("ghost"))).andExpect(status().isNotFound());
	}
}
