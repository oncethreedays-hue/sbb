package com.mysite.sbb.questionTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mysite.sbb.question.dto.QuestionRequestDto;
import com.mysite.sbb.question.entity.Question;
import com.mysite.sbb.question.repository.QuestionRepository;
import com.mysite.sbb.question.service.QuestionService;
import com.mysite.sbb.user.UserRole;
import com.mysite.sbb.user.entity.SiteUser;
import com.mysite.sbb.user.repository.UserRepository;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

@SpringBootTest(properties = { "spring.datasource.url=jdbc:h2:mem:controllertestdb;DB_CLOSE_DELAY=-1" })
@AutoConfigureMockMvc
@Transactional
class QuestionRestControllerTest {

	@Autowired
	MockMvc mockMvc;
	@Autowired
	ObjectMapper objectMapper;
	@Autowired
	QuestionService questionService;
	@Autowired
	QuestionRepository questionRepository;
	@Autowired
	UserRepository userRepository;
	@Autowired
	EntityManager em;

	@BeforeEach
	void setUp() {
		userRepository.save(new SiteUser("writer", "pw", "writer@test.com", UserRole.USER));
		userRepository.save(new SiteUser("other", "pw", "other@test.com", UserRole.USER));

	}

	private void flushAndClear() {
		em.flush();
		em.clear();
	}

	private String Json(String subject, String content) throws Exception {
		return objectMapper.writeValueAsString(new QuestionRequestDto(subject, content));
	}

	// --------- 조회 ---------

	@Test
	@DisplayName("GET 목록: 페이징 + 작성자명 + 최신순")
	void list() throws Exception {
		for (int i = 1; i <= 12; i++) {
			questionService.create("제목" + i, "내용" + i, "writer");
		}
		flushAndClear();

		mockMvc.perform(get("/api/questions").param("page", "0")).andExpect(status().isOk())
				.andExpect(jsonPath("$.content", hasSize(10))).andExpect(jsonPath("$.content[0].subject").value("제목12"))
				.andExpect(jsonPath("$.content[0].authorUsername").value("writer"))
				.andExpect(jsonPath("$.content[0].answerCount").value(0))
				.andExpect(jsonPath("$.totalElements").value(12));

		mockMvc.perform(get("/api/questions").param("page", "1")).andExpect(status().isOk())
				.andExpect(jsonPath("$.content", hasSize(2)));

	}

	@Test
	@DisplayName("GET 목록: 키워드 검색")
	void listSearch() throws Exception {
		questionService.create("스프링 부트", "일반", "writer");
		questionService.create("일반", "마이바티스 설명", "writer");
		questionService.create("무관", "무관", "writer");
		flushAndClear();

		mockMvc.perform(get("/api/questions").param("kw", "스프링")).andExpect(status().isOk())
				.andExpect(jsonPath("$.content", hasSize(1)));
		mockMvc.perform(get("/api/questions").param("kw", "마이바티스")).andExpect(status().isOk())
				.andExpect(jsonPath("$.content", hasSize(1)));

	}

	@Test
	@DisplayName("GET 상세: 200 / 없는 id는 404")
	void detail() throws Exception {
		Integer id = questionService.create("제목", "내용", "writer");
		flushAndClear();

		mockMvc.perform(get("/api/questions/{id}", id)).andExpect(status().isOk())
				.andExpect(jsonPath("$.subject").value("제목")).andExpect(jsonPath("$.authorUsername").value("writer"))
				.andExpect(jsonPath("$.voterCount").value(0)).andExpect(jsonPath("$.answerList", hasSize(0)));

		mockMvc.perform(get("/api/question/{id}", 9999999)).andExpect(status().isNotFound());

	}

	// ---------- 등록 ----------

	@Test
	@DisplayName("POST 등록: 201 + Location 헤더, DB 저장 확인")
	void create() throws Exception {
		mockMvc.perform(post("/api/questions").with(user("writer")).with(csrf()).contentType(MediaType.APPLICATION_JSON)
				.content(Json("세 제목", "새 내용"))).andExpect(status().isCreated())
				.andExpect(header().string("Location", org.hamcrest.Matchers.startsWith("/api/questions/")));

		flushAndClear();
		assertThat(questionRepository.findAll()).extracting(Question::getSubject).contains("세 제목");

	}

	@Test
	@DisplayName("POST 등록: 제목이 비어 있으면 400")
	void createValidationFail() throws Exception {
		mockMvc.perform(post("/api/questions").with(user("writer")).with(csrf()).contentType(MediaType.APPLICATION_JSON)
				.content(Json("", "내용"))).andExpect(status().isBadRequest());

	}

	@Test
	@DisplayName("POST 등록: 비로그인은 401 또는 403")
	void createUnauthenticated() throws Exception {
		mockMvc.perform(
				post("/api/questions").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(Json("제목", "내용")))
				.andExpect(status().is4xxClientError());
	}

	// ---------- 수정 ----------

	@Test
	@DisplayName("PUT 수정: 작성 204, DB반영")
	void modify() throws Exception {
		Integer id = questionService.create("원래 제목", "원래 내용", "writer");

		mockMvc.perform(put("/api/questions/{id}", id).with(user("writer")).with(csrf())
				.contentType(MediaType.APPLICATION_JSON).content(Json("수정 제목", "수정 내용")))
				.andExpect(status().isNoContent());

		flushAndClear();
		Question found = questionRepository.findById(id).orElseThrow();
		assertThat(found.getSubject()).isEqualTo("수정 제목");
		assertThat(found.getModifyDate()).isNotNull();

	}

	@Test
	@DisplayName("PUT 수정: 타인은 403, 내용은 그래도")
	void modifyForbidden() throws Exception {
		Integer id = questionService.create("원래 제목", "원래 내용", "writer");
		flushAndClear();

		mockMvc.perform(put("/api/questions/{id}", id).with(user("other")).with(csrf())
				.contentType(MediaType.APPLICATION_JSON).content(Json("해킹", "해킹"))).andExpect(status().isForbidden());
	}

	@Test
	@DisplayName("PUT 수정: 없는 id는 404")
	void modifyNotFound() throws Exception {
		mockMvc.perform(put("/api/questions/{id}", 999999).with(user("writer")).with(csrf())
				.contentType(MediaType.APPLICATION_JSON).content(Json("제목", "내용"))).andExpect(status().isNotFound());
	}

	// ---------- 삭제 ----------

	@Test
	@DisplayName("DELETE 삭제: 타인 403 / 작성자 204, DB에서 제거")
	void deleteQuestion() throws Exception {
		Integer id = questionService.create("제목", "내용", "writer");
		flushAndClear();

		mockMvc.perform(delete("/api/questions/{id}", id).with(user("other")).with(csrf()))
				.andExpect(status().isForbidden());

		mockMvc.perform(delete("/api/questions/{id}", id).with(user("writer")).with(csrf()))
				.andExpect(status().isNoContent());

		flushAndClear();
		assertThat(questionRepository.findById(id)).isEmpty();

	}

	// ---------- 추천 ----------

	@Test
	@DisplayName("추천 / 중복 추천 / 추천 취소")
	void voteAndCancel() throws Exception {
		Integer id = questionService.create("제목", "내용", "writer");
		flushAndClear();

		mockMvc.perform(post("/api/questions/{id}/vote", id).with(user("writer")).with(csrf()))
				.andExpect(status().isNoContent());

		mockMvc.perform(post("/api/questions/{id}/vote", id).with(user("writer")).with(csrf()))
				.andExpect(status().isNoContent());
		flushAndClear();

		mockMvc.perform(get("/api/questions/{id}", id)).andExpect(jsonPath("$.voterCount").value(1));

		mockMvc.perform(delete("/api/questions/{id}/vote", id).with(user("writer")).with(csrf()))
				.andExpect(status().isNoContent());

		flushAndClear();
		mockMvc.perform(get("/api/questions/{id}", id)).andExpect(jsonPath("$.voterCount").value(0));

	}

}
