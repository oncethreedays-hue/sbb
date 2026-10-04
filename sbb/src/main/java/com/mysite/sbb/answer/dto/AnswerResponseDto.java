package com.mysite.sbb.answer.dto;

import java.time.LocalDateTime;

import com.mysite.sbb.answer.entity.Answer;

import lombok.Getter;

@Getter
public class AnswerResponseDto {

	private final Integer id;
	private final String content;
	private final LocalDateTime createDate;
	private final LocalDateTime modifyDate;
	private final String authorUsername;
	private final Integer questionId;
	private final int voterCount;

	public AnswerResponseDto(Answer answer) {
		this.id = answer.getId();
		this.content = answer.getContent();
		this.createDate = answer.getCreateDate();
		this.modifyDate = answer.getModifyDate();
		this.authorUsername = (answer.getAuthor() != null) ? answer.getAuthor().getUsername() : null;
		this.questionId = (answer.getQuestion() != null) ? answer.getQuestion().getId() : null;
		this.voterCount = (answer.getVoter() != null) ? answer.getVoter().size() : 0;

	}
}
