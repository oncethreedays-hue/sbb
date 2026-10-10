package com.mysite.sbb.user.service;

import com.mysite.sbb.user.dto.UserDetailDto;
import com.mysite.sbb.user.dto.UserLoginResponseDto;

public interface UserService {

	Long signup(String username, String email, String password);

	UserLoginResponseDto login(String username, String password);

	String reissue(String refreshToken);

	void logout(String username);
	
	UserDetailDto getMyDetail(String username);

}
