package com.mysite.sbb.user.service;

import java.util.Set;

import org.springframework.http.HttpStatus;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.mysite.sbb.jwt.JwtTokenProvider;
import com.mysite.sbb.user.UserRole;
import com.mysite.sbb.user.dto.UserDetailDto;
import com.mysite.sbb.user.dto.UserLoginResponseDto;
import com.mysite.sbb.user.dto.UserSummaryRow;
import com.mysite.sbb.user.entity.SiteUser;
import com.mysite.sbb.user.mapper.UserMapper;
import com.mysite.sbb.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtTokenProvider jwtTokenProvider;
	private static final int RECENT_LIMIT = 5;
	private final UserMapper userMapper;
	private static final Set<String> RESERVED_USERNAMES = Set.of("admin", "root", "administrator", "system");

	@Override
	@Transactional
	public Long signup(String username, String email, String password) {
		if (RESERVED_USERNAMES.contains(username.toLowerCase())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "사용할 수 없는 아이디입니다.");
		}

		if (userRepository.existsByUsername(username)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 사용 중인 아이디입니다.");
		}
		String normalizedEmail = email.trim().toLowerCase();
		if (userRepository.existsByEmail(normalizedEmail)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다.");
		}

		SiteUser user = new SiteUser(username, passwordEncoder.encode(password), normalizedEmail, UserRole.USER);
		return userRepository.save(user).getId();
	}

	@Override
	@Transactional
	public UserLoginResponseDto login(String username, String password) {
		SiteUser user = userRepository.findByUsername(username)
				.filter(u -> passwordEncoder.matches(password, u.getPassword()))
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "아이디 또는 비밀번호가 올바르지 않습니다."));

		String accessToken = jwtTokenProvider.createAccessToken(user.getUsername());
		String refreshToken = jwtTokenProvider.createRefreshToken(user.getUsername());

		user.updateRefreshToken(refreshToken);
		return new UserLoginResponseDto(accessToken, refreshToken, user);
	}

	@Override
	public String reissue(String refreshToken) {
		if (!jwtTokenProvider.validateToken(refreshToken) || !jwtTokenProvider.isRefreshToken(refreshToken)) {
			throw invalidRefreshToken();
		}
		String username = jwtTokenProvider.getUsernameFromToken(refreshToken);
		SiteUser user = userRepository.findByUsername(username).orElseThrow(this::invalidRefreshToken);

		if (!refreshToken.equals(user.getRefreshToken())) {
			throw invalidRefreshToken();
		}
		return jwtTokenProvider.createAccessToken(username);
	}

	private ResponseStatusException invalidRefreshToken() {
		return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh Token이 유효하지 않습니다. 다시 로그인 해주세요.");
	}

	@Override
	@Transactional
	public void logout(String username) {
		userRepository.findByUsername(username).ifPresent(u -> u.updateRefreshToken(null));
	}

	@Override
	public UserDetailDto getMyDetail(String username) {
		UserSummaryRow s = userMapper.findSummaryByUsername(username)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));
		return new UserDetailDto(s.getUsername(), s.getEmail(), s.getQuestionCount(), s.getAnswerCount(),
				userMapper.findRecentQuestions(s.getId(), RECENT_LIMIT),
				userMapper.findRecentAnswers(s.getId(), RECENT_LIMIT));
	}

}
