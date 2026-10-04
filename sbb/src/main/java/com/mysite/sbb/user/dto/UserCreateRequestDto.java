package com.mysite.sbb.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserCreateRequestDto {
	
	@Size(min = 3, max = 25)
	@NotEmpty(message = "사용자ID는 필수 항목입니다.")
	@Pattern(regexp = "^(?!admin|root|관리자$).*$", message = "admin은 사용할 수 없는 아이디입니다.")
	private String username;
	
	@NotEmpty(message = "비밀번호는 필수 항목입니다.")
	private String password1;
	
	@NotEmpty(message = "비밀번호 확인은 필수 항목입니다.")	
	private String password2;
	
	@NotEmpty(message = "이메일은 필수 항목입니다.")
	@Email
	private String email;
	
}
