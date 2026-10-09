package com.mysite.sbb.user.service;

import com.mysite.sbb.question.repository.QuestionRepository;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.mysite.sbb.DataNotFoundException;
import com.mysite.sbb.jwt.JwtTokenProvider;
import com.mysite.sbb.user.UserRole;
import com.mysite.sbb.user.dto.UserLoginResponseDto;
import com.mysite.sbb.user.entity.SiteUser;
import com.mysite.sbb.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtTokenProvider jwtTokenProvider;

	@Override
	@Transactional
	public Long signup(String username, String email, String password) {
		if (userRepository.existsByUsername(username)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 사용 중인 아이디입니다.");
		}
		if (userRepository.existsByEmail(email)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다.");
		}
		SiteUser user = new SiteUser(username, passwordEncoder.encode(password), email, UserRole.USER);
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
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'reissue'");
	}

	@Override
	public void logout(String username) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'logout'");
	}

}
