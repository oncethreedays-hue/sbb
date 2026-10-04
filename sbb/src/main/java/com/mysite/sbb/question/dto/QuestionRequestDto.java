package com.mysite.sbb.question.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class QuestionRequestDto {

	@NotBlank(message = "제목은 필수항목입니다.")
	@Size(max = 200, message = "제목은 200자 이하로 입력해주세요.")
	private String subject;

	@NotBlank(message = "내용은 필수항목입니다.")
	private String content;
}
