package com.mysite.sbb.questionTest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.mysite.sbb.BaseTest;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class QuestionRestControllerTest extends BaseTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private EntityManager em;

	@Test
	@DisplayName("로그인 후 질문 작성 성공 테스트")
	void createQuestionAfterLogin() throws Exception {
		// 1. 헬퍼 메서드를 호출해 로그인 후 토큰과 사용자 정보가 담긴 Map 획득
		Map<String, Object> userInfo = loginAndGetUserInfo("testtest", "1234");
		String accessToken = (String) userInfo.get("accessToken");

		// 2. [Given] 작성할 질문 데이터 JSON
		String questionJson = "{\"subject\": \"JUnit 테스트 제목입니다.\", \"content\": \"JUnit 테스트 내용입니다.\"}";

		// 3. [When & Then] 발급받은 accessToken을 헤더에 담아 질문 작성 API 호출
		mockMvc.perform(post("/api/questions").header("Authorization", "Bearer " + accessToken)
				.contentType(MediaType.APPLICATION_JSON).content(questionJson)).andExpect(status().isCreated())
				.andDo(print());

	}

	@Test
	@DisplayName("질문 생성 헬퍼 메서드 테스트 (ID 반환 확인)")
	void testCreateQuestionAndGetId() throws Exception {

		Integer questionId = createQuestionAndGetId("testtest", "1234", "헬퍼 테스트 제목", "헬퍼 테스트 내용");

		org.assertj.core.api.Assertions.assertThat(questionId).isNotNull();
		org.assertj.core.api.Assertions.assertThat(questionId).isGreaterThan(0);

		// 콘솔에 잘 찍히는지 확인
		System.out.println("받아온 생성된 질문 ID: " + questionId);
	}

	@Test
	@DisplayName("로그인 후 질문 수정 성공 테스트")
	void updateQuestionAfterLogin() throws Exception {
		// 테스트용 질문 생성
		Integer questionId = createQuestionAndGetId("testtest", "1234", "수정 전 제목", "수정 전 내용");

		Map<String, Object> userInfo = loginAndGetUserInfo("testtest", "1234");
		String accessToken = (String) userInfo.get("accessToken");

		// 수정할 데이터
		String updateJson = "{\"subject\": \"수정된 제목입니다.\", \"content\": \"수정된 내용입니다.\"}";

		mockMvc.perform(put("/api/questions/{id}", questionId).header("Authorization", "Bearer " + accessToken)
				.contentType(MediaType.APPLICATION_JSON).content(updateJson)).andExpect(status().isOk()).andDo(print());
	}

	@Test
	@DisplayName("로그인 후 질문 삭제 성공 테스트")
	void deleteQuestionAfterLogin() throws Exception {

		Integer questionId = createQuestionAndGetId("testtest", "1234", "삭제할 제목", "삭제할 내용");

		Map<String, Object> userInfo = loginAndGetUserInfo("testtest", "1234");
		String accessToken = (String) userInfo.get("accessToken");

		mockMvc.perform(delete("/api/questions/{id}", questionId).header("Authorization", "Bearer " + accessToken)
				.contentType(MediaType.APPLICATION_JSON)).andExpect(status().isNoContent()).andDo(print());

	}

	@Test
	@DisplayName("질문 1개 생성 후 답변 100개 등록 및 조회 테스트")
	void createAnswerAndGetTest() throws Exception {
		String testUsername = "testtest";
		String testPassword = "1234";
		String testSubject = "답변 100개 질문";
		String testContent = "질문 내용";

		// 1. 질문 1개 생성
		Integer questionId = createQuestionAndGetId(testUsername, testPassword, testSubject, testContent);

		// 2. 질문 100개 생성
		for (int i = 1; i <= 10; i++) {
			createAnswerAndGetId(testUsername, testPassword, questionId, "테스트 답변 내용" + i);

		}
		em.flush();
		em.clear();

		// 3. 질문, 답변 100개 조회
		mockMvc.perform(get("/api/questions/{id}", questionId)).andExpect(status().isOk())
				.andExpect(jsonPath("$.answerList.length()").value(10)).andDo(print());

	}
}
