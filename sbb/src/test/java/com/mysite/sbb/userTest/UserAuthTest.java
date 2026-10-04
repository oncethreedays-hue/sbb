package com.mysite.sbb.userTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mysite.sbb.user.UserRole;
import com.mysite.sbb.user.dto.UserCreateRequestDto;
import com.mysite.sbb.user.entity.SiteUser;
import com.mysite.sbb.user.repository.UserRepository;

import jakarta.transaction.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class UserAuthTest {
	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	@DisplayName("Datainitializer admin 생성")
	void testInitAdminUser() {

		// when
		SiteUser admin = userRepository.findByUsername("admin").orElse(null);

		// then
		assertThat(admin).isNotNull();
		assertThat(admin.getRole()).isEqualTo(UserRole.ADMIN);
		assertThat(admin.getEmail()).isEqualTo("admin@sbb.com");
	}

	@ParameterizedTest
	@ValueSource(strings = { "admin", "root", "관리자" })
	@DisplayName("관리자로 회원가입 시도 시 유효성 검사")
	void testSignupWithForbiddenUsername(String forbiddenUsername) throws Exception {
		UserCreateRequestDto requestDto = new UserCreateRequestDto(
				forbiddenUsername,
				"1234",
				"1234",
				forbiddenUsername + "@test.com"
				);
		
		mockMvc.perform(post("/api/user/signup")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(requestDto)))
				.andExpect(status().isBadRequest());
	}

}
