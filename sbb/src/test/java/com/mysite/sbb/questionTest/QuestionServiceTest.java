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
import org.springframework.http.HttpStatusCode;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.mysite.sbb.question.dto.QuestionDetailDto;
import com.mysite.sbb.question.dto.QuestionResponseDto;
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

}
