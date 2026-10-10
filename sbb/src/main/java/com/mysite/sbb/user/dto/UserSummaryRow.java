package com.mysite.sbb.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserSummaryRow {
	
	private final Long id;
	private final String username;
	private final String email;
	private final long questionCount;
	private final long answerCount;
}
