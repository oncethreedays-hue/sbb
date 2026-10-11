package com.mysite.sbb.answerTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.mysite.sbb.BaseTest;
import com.mysite.sbb.answer.dto.AnswerResponseDto;
import com.mysite.sbb.answer.entity.Answer;
import com.mysite.sbb.answer.repository.AnswerRepository;
import com.mysite.sbb.answer.service.AnswerService;
import com.mysite.sbb.question.entity.Question;
import com.mysite.sbb.question.repository.QuestionRepository;
import com.mysite.sbb.user.entity.SiteUser;

public class AnswerServiceTest extends BaseTest {

	@Autowired
	AnswerService answerService;
	@Autowired
	AnswerRepository answerRepository;
	@Autowired
	QuestionRepository questionRepository;

	private Integer questionId;

	@BeforeEach
	void setUpQuestion() {
		SiteUser write = userRepository.findByUsername("writer").orElseThrow();
		questionId = questionRepository.save(new Question("제목", "내용", write)).getId();
		flushAndClear();
	}

	private void assertStatus(ThrowingCallable call, HttpStatus expected) {
		assertThatThrownBy(call).isInstanceOfSatisfying(ResponseStatusException.class,
				e -> assertThat(e.getStatusCode()).isEqualTo(expected));
	}

	private Integer newAnswer(String username, String content) {
		return answerService.create(questionId, content, username).getId();
	}

	// ---------- 등록 ----------
	@Test
	@DisplayName("등록: 응답 DTO에서 작성자, 질문 id, 추천 0이 담기고 DB에 저장")
	void create() {
		AnswerResponseDto dto = answerService.create(questionId, "답변 내용", "writer");
		flushAndClear();

		assertThat(dto.getContent()).isEqualTo("답변 내용");
		assertThat(dto.getAuthorUsername()).isEqualTo("writer");
		assertThat(dto.getQuestionId()).isEqualTo(questionId);
		assertThat(dto.getVoterCount()).isZero();

		Answer saved = answerRepository.findById(dto.getId()).orElseThrow();
		assertThat(saved.getContent()).isEqualTo("답변 내용");
		assertThat(saved.getCreateDate()).isNotNull();
	}

	@Test
	@DisplayName("등록 실패: 없는 질문은 404, 없는 사용자는 401")
	void createFail() {
		assertStatus(() -> answerService.create(99999, "내용", "writer"), HttpStatus.NOT_FOUND);
		assertStatus(() -> answerService.create(questionId, "내용", "writer3"), HttpStatus.UNAUTHORIZED);
	}
}
