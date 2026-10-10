package com.mysite.sbb.user.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserDetailDto {
	
	private final String username;
	private final String email;
	private final long questionCount;
	private final long answerCount;
	private final List<MyQuestionDto> recentQuestions;
	private final List<MyAnswerDto> recentAnswers;
}
