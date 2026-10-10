package com.mysite.sbb;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mysite.sbb.user.UserRole;
import com.mysite.sbb.user.entity.SiteUser;
import com.mysite.sbb.user.repository.UserRepository;

import jakarta.persistence.EntityManager;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1")
@AutoConfigureMockMvc
@Transactional
public class BaseTest {

	protected static final String PASSWORD = "pw";

	@Autowired
	protected MockMvc mockMvc;
	@Autowired
	protected ObjectMapper objectMapper;
	@Autowired
	protected UserRepository userRepository;
	@Autowired
	protected PasswordEncoder passwordEncoder;
	@Autowired
	protected EntityManager em;

	@BeforeEach
	protected void baseSetUp() {
		userRepository.save(new SiteUser("writer", passwordEncoder.encode(PASSWORD), "writer@test.com", UserRole.USER));
		userRepository.save(new SiteUser("other", passwordEncoder.encode(PASSWORD), "other@test.com", UserRole.USER));

	}

	protected void flushAndClear() {
		em.flush();
		em.clear();

	}

	protected String toJson(Object body) throws Exception {
		return objectMapper.writeValueAsString(body);
	}

	protected Map<String, Object> login(String username, String password) throws Exception {
		String response = mockMvc
				.perform(post("/api/user/login").contentType(MediaType.APPLICATION_JSON)
						.content(toJson(Map.of("username", username, "password", password))))
				.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
		return objectMapper.readValue(response, new TypeReference<Map<String, Object>>() {
		});
	}
	
	protected String loginAndGetAccessToken(String username) throws Exception {
		return (String) login(username, PASSWORD).get("accessToken");
	}
	
	protected Integer createQuestionAndGetId(String accessToken, String subject, String content) throws Exception {
		MvcResult result = mockMvc.perform(post("/api/question")
				.header("Authorization", "Bearer " + accessToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(toJson(Map.of("subject", subject, "content", content))))
				.andExpect(status().isCreated())
				.andReturn();
		String location = result.getResponse().getHeader("Location");
		return Integer.valueOf(location.substring(location.lastIndexOf('/') + 1));
	}
	
}
