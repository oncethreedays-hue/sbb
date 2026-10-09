package com.mysite.sbb.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserLoginRequestDto {

	@NotBlank(message = "아이디는 필수항목입니다.")
	private String username;
	@NotBlank(message = "비밀번호는 필수항목입니다.")
	private String password;

}
