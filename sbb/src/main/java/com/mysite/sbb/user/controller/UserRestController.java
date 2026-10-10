package com.mysite.sbb.user.controller;

import java.security.Principal;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mysite.sbb.user.service.UserService;
import com.mysite.sbb.user.dto.TokenReissueRequestDto;
import com.mysite.sbb.user.dto.UserCreateRequestDto;
import com.mysite.sbb.user.dto.UserDetailDto;
import com.mysite.sbb.user.dto.UserLoginRequestDto;
import com.mysite.sbb.user.dto.UserLoginResponseDto;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserRestController {

	private final UserService userService;

	@PostMapping("/signup")
	public ResponseEntity<?> signup(@Valid @RequestBody UserCreateRequestDto request) {
		if (!request.getPassword1().equals(request.getPassword2())) {
			return ResponseEntity.badRequest().body(Map.of("password2", "2개의 패스워드가 일치하지 않습니다."));
		}
		userService.signup(request.getUsername(), request.getEmail(), request.getPassword1());
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}

	@PostMapping("/login")
	public ResponseEntity<UserLoginResponseDto> login(@Valid @RequestBody UserLoginRequestDto request) {
		return ResponseEntity.ok(userService.login(request.getUsername(), request.getPassword()));
	}

	@PostMapping("/reissue")
	public ResponseEntity<Map<String, String>> reissue(@Valid @RequestBody TokenReissueRequestDto request) {
		return ResponseEntity.ok(Map.of("accessToken", userService.reissue(request.getRefreshToken())));
	}

	@PreAuthorize("isAuthenticated()")
	@PostMapping("/logout")
	public ResponseEntity<Void> logout(Principal principal) {
		userService.logout(principal.getName());
		return ResponseEntity.noContent().build();
	}

	@PreAuthorize("isAuthenticated()")
	@GetMapping("/me")
	public ResponseEntity<UserDetailDto> me(Principal principal) {
		return ResponseEntity.ok(userService.getMyDetail(principal.getName()));
	}
}