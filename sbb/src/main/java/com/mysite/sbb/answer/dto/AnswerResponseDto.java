package com.mysite.sbb.answer.dto;

import java.time.LocalDateTime;

import com.mysite.sbb.answer.entity.Answer;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AnswerResponseDto {

	private final Integer id;
	private final String content;
	private final LocalDateTime createDate;
	private final LocalDateTime modifyDate;
	private final String authorUsername;
	private final Integer questionId;
	private final int voterCount;

	public static AnswerResponseDto from(Answer answer) {
		return new AnswerResponseDto(answer.getId(), answer.getContent(), answer.getCreateDate(),
				answer.getModifyDate(), answer.getAuthor() != null ? answer.getAuthor().getUsername() : null,
				answer.getQuestion().getId(), answer.getVoter().size());

	}
}
