package com.mysite.sbb.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mysite.sbb.user.entity.SiteUser;

public interface UserRepository extends JpaRepository<SiteUser, Long> {

	Optional<SiteUser> findByUsername(String username);

	boolean existsByUsername(String username);

	boolean existsByEmail(String email);
}
