package com.mysite.sbb.user.service;

import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.mysite.sbb.DataNotFoundException;
import com.mysite.sbb.jwt.JwtTokenProvider;
import com.mysite.sbb.user.UserRole;
import com.mysite.sbb.user.dto.UserLoginResponseDto;
import com.mysite.sbb.user.entity.SiteUser;
import com.mysite.sbb.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtTokenProvider jwtTokenProvider;

	@Override
	public SiteUser create(String username, String email, String password) {
		String encodedPassword = passwordEncoder.encode(password);
		SiteUser user = new SiteUser(username, encodedPassword, email, UserRole.USER);
		this.userRepository.save(user);
		return null;
	}

	@Override
	public SiteUser getUser(String username) {
		Optional<SiteUser> siteUser = this.userRepository.findByUsername(username);
		if (siteUser.isPresent()) {
			return siteUser.get();
		} else {
			throw new DataNotFoundException("siteuser not founded");
		}
	}

	@Override
	public UserLoginResponseDto loginAndIssueToken(String username, String password) {
		// 1. 유저 조회
		SiteUser siteUser = this.userRepository.findByUsername(username)
				.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
		
		if (!passwordEncoder.matches(password, siteUser.getPassword())) {
			throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
		}
		
		// 2. AccessToken, RefreshToken 발급
		String accessToken = jwtTokenProvider.createAccessToken(siteUser.getUsername());
		String refreshToken = jwtTokenProvider.createRefreshToken(siteUser.getUsername());
		
		// 3. 발급된 Refresh Token을 DB에 저장
		siteUser.updateRefreshToken(refreshToken);
		this.userRepository.save(siteUser);
		
		return new UserLoginResponseDto(accessToken, refreshToken, siteUser);
	}

}
