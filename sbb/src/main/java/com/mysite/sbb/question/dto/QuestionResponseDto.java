package com.mysite.sbb.question.dto;

import java.time.LocalDateTime;

import com.mysite.sbb.question.entity.Question;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class QuestionResponseDto {

	private final Integer id;
	private final String subject;
	private final LocalDateTime createDate;
	private final String authorUsername;
	private final int answerCount;

	public QuestionResponseDto(Question question) {
		this.id = question.getId();
		this.subject = question.getSubject();
		this.createDate = question.getCreateDate();
		this.authorUsername = (question.getAuthor() != null) ? question.getAuthor().getUsername() : null;
		this.answerCount = (question.getAnswerList() != null) ? question.getAnswerList().size() : 0;

	}
}
