package com.mysite.sbb.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
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
	
	@NotBlank(message = "사용자ID는 필수 항목입니다.")
	@Size(min = 3, max = 25, message = "사용자ID는 3~25자로 입력해주세요.")
	@Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "사용자ID는 영문, 숫자, 밑줄(_)만 사용할 수 있습니다.")
	private String username;
	
	@NotBlank(message = "비밀번호는 필수 항목입니다.")
	@Size(min = 8, max = 64, message = "비밀번호는 8~64자로 입력해주세요." )
	private String password1;
	
	@NotBlank(message = "비밀번호 확인은 필수 항목입니다.")	
	private String password2;
	
	@NotBlank(message = "이메일은 필수 항목입니다.")
	@Email(message = "올바른 이메일 형식이 아닙니다.")
	@Size(max = 255, message = "이메일은 255자 이하로 입력해주세요.")
	private String email;
	
}
