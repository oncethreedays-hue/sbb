package com.mysite.sbb.user;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.mysite.sbb.user.entity.SiteUser;
import com.mysite.sbb.user.repository.UserRepository;

@Configuration
public class DataInitializer {

	@Bean CommandLineRunner initAdminUser(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		return args -> {

			String adminUsername = "admin";
			if (userRepository.findByUsername(adminUsername).isEmpty()) {
				String encodedPassword = passwordEncoder.encode("1234");

				SiteUser admin = new SiteUser(adminUsername, encodedPassword, "admin@sbb.com", UserRole.ADMIN //
				);

				userRepository.save(admin);
				System.out.println(">>> 초기 관리자 계정(admin)이 자동으로 생성되었습니다.");
			}
		};
	}
}