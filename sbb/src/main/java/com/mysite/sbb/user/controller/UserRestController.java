package com.mysite.sbb.user.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mysite.sbb.jwt.JwtTokenProvider;
import com.mysite.sbb.user.service.UserService;
import com.mysite.sbb.user.dto.UserCreateRequestDto;
import com.mysite.sbb.user.dto.UserLoginRequestDto;
import com.mysite.sbb.user.dto.UserLoginResponseDto;
import com.mysite.sbb.user.entity.SiteUser;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserRestController {

	private final UserService userService;
	private final JwtTokenProvider jwtTokenProvider;

	@PostMapping("/signup")
	public ResponseEntity<?> signup(@Valid @RequestBody UserCreateRequestDto requestDto, BindingResult bindingResult) {
		// 1. 유효성 검사 실패 시 에러 메시지 맵을 JSON으로 변환
		if (bindingResult.hasErrors()) {
			Map<String, String> errors = new HashMap<>();
			for (FieldError error : bindingResult.getFieldErrors()) {
				errors.put(error.getField(), error.getDefaultMessage());
			}
			return ResponseEntity.badRequest().body(errors);

		}
		// 2. 비밀번호 일치 여부 검사
		if (!requestDto.getPassword1().equals(requestDto.getPassword2())) {
			Map<String, String> error = new HashMap<>();
			error.put("password2", "2개의 패스워드가 일치하지 않습니다.");
			return ResponseEntity.badRequest().body(error);
		}
		
		try {
			userService.create(requestDto.getUsername(), requestDto.getEmail(), requestDto.getPassword1());
			return ResponseEntity.status(HttpStatus.CREATED).body("회원 가입이 완료 되었습니다.");
			
		} catch (DataIntegrityViolationException  e) { 
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.CONFLICT).body("이미 등록된 사용자입니다.");
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
		}
	}

	@PostMapping("/login")
	public ResponseEntity<UserLoginResponseDto> login(@RequestBody UserLoginRequestDto loginDto) {

		try {
			// 1. UserService의 login 메서드를 통해 아이디/비밀번호 검증 및 JWT 토큰 발급
			UserLoginResponseDto responseDto = userService.loginAndIssueToken(loginDto.getUsername(),
					loginDto.getPassword());

			// 2. JSON 형태로 응답 반환
			return ResponseEntity.ok(responseDto);

		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.badRequest().build();
		}
	}

	@PostMapping("/reissue")
	public ResponseEntity<?> reissue(@RequestBody Map<String, String> requestMap) {
		String refreshToken = requestMap.get("refreshToken");

		// 1.Refresh Token 유효성 검증
		if (refreshToken != null && jwtTokenProvider.validateToken(refreshToken)) {
			String username = jwtTokenProvider.getUsernameFromToken(refreshToken);

			SiteUser user = userService.getUser(username);

			if (refreshToken.equals(user.getRefreshToken())) {
				String newAccessToken = jwtTokenProvider.createAccessToken(username);
				return ResponseEntity.ok().body(Map.of("accessToken", newAccessToken));
			}
		}
		return ResponseEntity.status(401).body("Refresh Token이 유효하지않습니다. 다시 로그인 해주세요.");
	}
}