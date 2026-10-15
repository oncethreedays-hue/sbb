package com.mysite.sbb.answerTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
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

	// ---------- 수정 / 삭제 ----------
	@Test
	@DisplayName("수정: 작성자는 성공, 타인은 403이고 내용 그대로")
	void modify() {
		Integer id = newAnswer("writer", "원래 내용");
		flushAndClear();

		assertStatus(() -> answerService.modify(id, "해킹", "other"), HttpStatus.FORBIDDEN);
		flushAndClear();
		assertThat(answerRepository.findById(id).orElseThrow().getContent()).isEqualTo("원래 내용");

		answerService.modify(id, "수정 내용", "writer");
		flushAndClear();

		Answer found = answerRepository.findById(id).orElseThrow();
		assertThat(found.getContent()).isEqualTo("수정 내용");
		assertThat(found.getModifyDate()).isNotNull();
	}

	@Test
	@DisplayName("추천: 중복은 한 번만, 취소하면 0")
	void voteAndCancel() {
		Integer id = newAnswer("writer", "내용");
		flushAndClear();

		answerService.vote(id, "other");
		answerService.vote(id, "other");

		flushAndClear();

		assertThat(answerRepository.findById(id).orElseThrow().getVoter()).hasSize(1);

		answerService.cancelVote(id, "other");
		answerService.cancelVote(id, "other");

		flushAndClear();

		assertThat(answerRepository.findById(id).orElseThrow().getVoter()).isEmpty();
	}

	// --------- 목록 ----------
	@Test
	@DisplayName("목록: 페이징, 최신순, 전체 개수")
	void getListPaging() {
		for (int i = 1; i <= 12; i++) {
			newAnswer("writer", "답변" + i);
		}
		flushAndClear();

		Page<AnswerResponseDto> first = answerService.getList(questionId, 0, "latest");
		Page<AnswerResponseDto> second = answerService.getList(questionId, 1, "latest");

		assertThat(first.getTotalElements()).isEqualTo(12);
		assertThat(first.getContent()).hasSize(10);
		assertThat(second.getContent()).hasSize(2);
		assertThat(first.getContent().get(0).getContent()).isEqualTo("답변12");
		assertThat(first.getContent().get(0).getAuthorUsername()).isEqualTo("writer");
		assertThat(first.getContent().get(0).getQuestionId()).isEqualTo(questionId);

	}

	@Test
	@DisplayName("목록: 추천순 정렬 (추천 수가 많은 순)")
	void getListSortByVotest() {
		Integer a1 = newAnswer("writer", "답변1");
		Integer a2 = newAnswer("writer", "답변2");
		Integer a3 = newAnswer("writer", "답변3");
		answerService.vote(a1, "writer");
		answerService.vote(a1, "other");
		answerService.vote(a2, "other");
		flushAndClear();

		List<AnswerResponseDto> list = answerService.getList(questionId, 0, "votes").getContent();
		assertThat(list).extracting(AnswerResponseDto::getId).containsExactly(a1, a2, a3);
		assertThat(list.get(0).getVoterCount()).isEqualTo(2);
		assertThat(list.get(2).getVoterCount()).isZero();

	}

	@Test
	@DisplayName("목록: 다른 질문의 답변이 섞이지 않고, 없는 질무은 404")
	void getListToIsolation() {
		SiteUser writer = userRepository.findByUsername("writer").orElseThrow();
		Integer otherQuestionId = questionRepository.save(new Question("다른 질문", "내용", writer)).getId();
		newAnswer("writer", "이 질문의 답변");
		answerService.create(otherQuestionId, "다른 질문의 답변", "writer");

		flushAndClear();

		Page<AnswerResponseDto> list = answerService.getList(questionId, 0, "latest");

		assertThat(list.getTotalElements()).isEqualTo(1);
		assertThat(list.getContent().get(0).getContent()).isEqualTo("이 질문의 답변");
		assertStatus(() -> answerService.getList(999999, 0, "latest"), HttpStatus.NOT_FOUND);
	}
}
