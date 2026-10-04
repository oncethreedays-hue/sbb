package com.mysite.sbb.answer.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AnswerRequestDto {
	
	@NotBlank(message = "답변 내용은 필수 항목입니다.")
	private String content;
	
}
