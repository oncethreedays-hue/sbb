package com.mysite.sbb.userTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mysite.sbb.BaseTest;
import com.mysite.sbb.jwt.JwtTokenProvider;
import com.mysite.sbb.user.UserRole;
import com.mysite.sbb.user.dto.UserCreateRequestDto;
import com.mysite.sbb.user.dto.UserLoginRequestDto;
import com.mysite.sbb.user.dto.UserLoginResponseDto;
import com.mysite.sbb.user.entity.SiteUser;
import com.mysite.sbb.user.repository.UserRepository;
import com.mysite.sbb.user.service.UserService;

import jakarta.transaction.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class UserServiceTest extends BaseTest {

	@Autowired
	UserService userService;
	@Autowired
	JwtTokenProvider jwtTokenProvider;

	private void assertStatus(ThrowingCallable call, HttpStatus expected) {
		assertThatThrownBy(call).isInstanceOfSatisfying(ResponseStatusException.class,
				e -> assertThat(e.getStatusCode()).isEqualTo(expected));
	}

	private String reasonOf(ThrowingCallable call) {
		try {
			call.call();
		} catch (ResponseStatusException e) {
			return e.getReason();
		} catch (Throwable t) {
			throw new AssertionError("예상하지 못한 예외", t);
		}
		throw new AssertionError("예외가 발생해야 합니다.");
	}

	// ---------- 회원가입 ----------
	@Test
	@DisplayName("가입: 비밀번호는 인코딩되어 저장되고 role은 USER")
	void signup() {
		Long id = userService.signup("newuser", "new@test.com", "Passw0rd!");
		flushAndClear();

		SiteUser saved = userRepository.findById(id).orElseThrow();
		assertThat(saved.getUsername()).isEqualTo("newuser");
		assertThat(saved.getPassword()).isNotEqualTo("Passw0rd!");
		assertThat(passwordEncoder.matches("Passw0rd!", saved.getPassword())).isTrue();
		assertThat(saved.getRole()).isEqualTo(UserRole.USER);
	}

	@Test
	@DisplayName("가입: 아이디 중복 / 이메일 중복은 409")
	void signupDuplicate() {
		assertStatus(() -> userService.signup("writer", "another@test.com", "passw0rd!"), HttpStatus.CONFLICT);
		assertStatus(() -> userService.signup("another", "writer@test.com", "passw0rd!"), HttpStatus.CONFLICT);
	}

	// ---------- 로그인 ----------
	@Test
	@DisplayName("로그인: 토큰 2종 발급, 용도 구분, Refresh Token DB 저장")
	void login() {
		UserLoginResponseDto result = userService.login("writer", PASSWORD);

		assertThat(jwtTokenProvider.validateToken(result.getAccessToken())).isTrue();
		assertThat(jwtTokenProvider.isAccessToken(result.getAccessToken())).isTrue();
		assertThat(jwtTokenProvider.isRefreshToken(result.getRefreshToken())).isTrue();
		assertThat(jwtTokenProvider.getUsernameFromToken(result.getAccessToken())).isEqualTo("writer");

		flushAndClear();
		assertThat(userRepository.findByUsername("writer").orElseThrow().getRefreshToken())
				.isEqualTo(result.getRefreshToken());

	}
	
	@Test
	@DisplayName("로그인 실패: 비밀번호 불일치, 없는 사용자 모두 401이고 메시지가 같다.")
	void loginFail() {
		assertStatus(() -> userService.login("writer", "wrong"), HttpStatus.UNAUTHORIZED);
		assertStatus(() -> userService.login("ghost", PASSWORD), HttpStatus.UNAUTHORIZED);
		
		// 메시지가 다르면 어떤 아이디가 가입돼 있는지 알아낼 수 있다.
		String wrongPassword = reasonOf(() -> userService.login("writer", "wrong"));
		String unknowUser = reasonOf(() -> userService.login("ghost", PASSWORD));
		assertThat(wrongPassword).isEqualTo(unknowUser);
	}
}
