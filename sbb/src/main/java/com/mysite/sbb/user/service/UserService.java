package com.mysite.sbb.user.service;

import com.mysite.sbb.user.dto.UserLoginResponseDto;
import com.mysite.sbb.user.entity.SiteUser;

public interface UserService {
	
	SiteUser create(String username, String email, String password);
	
	SiteUser getUser(String username);
	
	UserLoginResponseDto loginAndIssueToken(String username, String password);
	
}
