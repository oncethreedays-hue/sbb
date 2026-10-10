package com.mysite.sbb.user.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MyAnswerDto {

	private final Integer id;
	private final String content;
	private final LocalDateTime createDate;
	private final Integer questionId;
	private final String questionSubject;

}
