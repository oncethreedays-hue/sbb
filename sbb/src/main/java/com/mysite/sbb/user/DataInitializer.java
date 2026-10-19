package com.mysite.sbb.user;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.mysite.sbb.user.entity.SiteUser;
import com.mysite.sbb.user.repository.UserRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class DataInitializer {

	@Bean
	CommandLineRunner initAdminUser(UserRepository userRepository, PasswordEncoder passwordEncoder,
			@Value("${app.admin.username:}") String username, @Value("${app.admin.password:}") String password,
			@Value("${app.admin.email:}") String email) {
		return args -> {
			if (username.isBlank() || password.isBlank() || email.isBlank()) {
				return;
			}
			if (userRepository.existsByUsername(username)) {
				return;
			}
			userRepository.save(
					new SiteUser(username, passwordEncoder.encode(password), email.toLowerCase(), UserRole.ADMIN));
			log.info("초기 관리자 계정을 생성했습니다. username={}", username);
		};
	}
}