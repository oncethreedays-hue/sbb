package com.mysite.sbb.user.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MyQuestionDto {
	
	private final Integer id;
	private final String subject;
	private final LocalDateTime createDate;
	private final int answerCount;
}
