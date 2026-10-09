package com.mysite.sbb.questionTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.mysite.sbb.question.dto.QuestionDetailDto;
import com.mysite.sbb.question.dto.QuestionResponseDto;
import com.mysite.sbb.question.entity.Question;
import com.mysite.sbb.question.repository.QuestionRepository;
import com.mysite.sbb.question.service.QuestionService;
import com.mysite.sbb.user.UserRole;
import com.mysite.sbb.user.entity.SiteUser;
import com.mysite.sbb.user.repository.UserRepository;

import jakarta.persistence.EntityManager;

@SpringBootTest(properties = { "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1" })
@Transactional
public class QuestionServiceTest {

	@Autowired
	QuestionService questionService;
	@Autowired
	QuestionRepository questionRepository;
	@Autowired
	UserRepository userRepository;
	@Autowired
	EntityManager em;

	private SiteUser writer;
	private SiteUser other;

	@BeforeEach
	void setUp() {
		writer = userRepository.save(new SiteUser("writer", "pw", "writer@test.com", UserRole.USER));
		other = userRepository.save(new SiteUser("other", "pw", "other@test.com", UserRole.USER));
	}

	private void flushAndClear() {
		em.flush();
		em.clear();
	}

	@Test
	@DisplayName("질문 생성 후 상세조회")
	void createAndGetDetail() {
		Integer id = questionService.create("제목", "내용", "writer");
		flushAndClear();

		QuestionDetailDto dto = questionService.getQuestionDetail(id);

		assertThat(dto.getSubject()).isEqualTo("제목");
		assertThat(dto.getContent()).isEqualTo("내용");
		assertThat(dto.getAuthorUsername()).isEqualTo("writer");
		assertThat(dto.getVoterCount()).isZero();
		assertThat(dto.getAnswerList()).isEmpty();

	}

	@Test
	@DisplayName("없는 질문 조회 시 404")
	void getQuestionNotFound() {
		assertThatThrownBy(() -> questionService.getQuestionDetail(1000000)).isInstanceOfSatisfying(
				ResponseStatusException.class, e -> assertThat(e.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND));
	}

	@Test
	@DisplayName("MyBatis 목록 조회: 페이징, 최신순, 작성자명, 답변수")
	void getListPaging() {
		for (int i = 1; i <= 15; i++) {
			questionService.create("제목" + i, "내용" + i, "writer");
		}
		flushAndClear();

		Page<QuestionResponseDto> first = questionService.getList(0, "");
		Page<QuestionResponseDto> second = questionService.getList(1, "");

		assertThat(first.getTotalElements()).isEqualTo(15);
		assertThat(first.getTotalPages()).isEqualTo(2);
		assertThat(first.getContent()).hasSize(10);
		assertThat(second.getContent()).hasSize(5);

		// id DESC: 최신글이 맨 앞
		QuestionResponseDto latest = first.getContent().get(0);
		assertThat(latest.getSubject()).isEqualTo("제목15");
		assertThat(latest.getAuthorUsername()).isEqualTo("writer");
		assertThat(latest.getAnswerCount()).isZero();
		assertThat(latest.getCreateDate()).isNotNull();
	}

	@Test
	@DisplayName("목록 조회: 제목 또는 내용 키워드 검색")
	void getListSearch() {
		questionService.create("스프링 부트", "일반 내용", "writer");
		questionService.create("일반 제목", "마이바티스 설명", "writer");
		questionService.create("관계 없는 글", "관계 없는 내용", "writer");

		assertThat(questionService.getList(0, "스프링").getTotalElements()).isEqualTo(1);
		assertThat(questionService.getList(0, "마이바티스").getTotalElements()).isEqualTo(1);
		assertThat(questionService.getList(0, "없는키워드").getContent()).isEmpty();
		assertThat(questionService.getList(0, null).getTotalElements()).isEqualTo(3);

	}

	@Test
	@DisplayName("작성자가 질문 수정")
	void modifyByAuthor() {
		Integer id = questionService.create("원래 제목", "원래 내용", "writer");
		flushAndClear();

		questionService.modify(id, "수정 제목", "수정 내용", "writer");
		flushAndClear();

		Question found = questionRepository.findById(id).orElseThrow();
		assertThat(found.getSubject()).isEqualTo("수정 제목");
		assertThat(found.getContent()).isEqualTo("수정 내용");
		assertThat(found.getModifyDate()).isNotNull();
	}

	@Test
	@DisplayName("타인이 수정하면 403, 내용은 그대로")
	void modifyByOtherForbidden() {
		Integer id = questionService.create("원래 제목", "원래 내용", "writer");
		flushAndClear();

		assertThatThrownBy(() -> questionService.modify(id, "해킹", "해킹", "other")).isInstanceOfSatisfying(
				ResponseStatusException.class, e -> assertThat(e.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN));

		flushAndClear();
		assertThat(questionRepository.findById(id).orElseThrow().getSubject()).isEqualTo("원래 제목");
	}

	@Test
	@DisplayName("작성자가 삭제, 타인은 403")
	void delete() {
		Integer id = questionService.create("제목", "내용", "writer");
		flushAndClear();

		assertThatThrownBy(() -> questionService.delete(id, "other")).isInstanceOfSatisfying(
				ResponseStatusException.class, e -> assertThat(e.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN));

		questionService.delete(id, "writer");
		flushAndClear();
		assertThat(questionRepository.findById(id)).isEmpty();
	}

	@Test
	@DisplayName("추천, 중복 추천 무시, 추천 취소")
	void voteAndCancel() {
		Integer id = questionService.create("제목", "내용", "writer");
		flushAndClear();

		questionService.vote(id, "other");
		questionService.vote(id, "other");
		flushAndClear();
		assertThat(questionService.getQuestionDetail(id).getVoterCount()).isEqualTo(1);

		questionService.cancelVote(id, "other");
		questionService.cancelVote(id, "other");
		flushAndClear();
		assertThat(questionService.getQuestionDetail(id).getVoterCount()).isZero();

	}

	@Test
	@DisplayName("존재하지 않는 사용자는 401")
	void unknownUser() {
		assertThatThrownBy(() -> questionService.create("제목", "내용", "ghost")).isInstanceOfSatisfying(
				ResponseStatusException.class, e -> assertThat(e.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED));
	}

}
