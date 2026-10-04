package com.mysite.sbb.question.dto;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import com.mysite.sbb.answer.dto.AnswerResponseDto;
import com.mysite.sbb.question.entity.Question;

import lombok.Getter;

@Getter
public class QuestionDetailDto {
	
	private final Integer id;
	private final String subject;
	private final String content;
	private final LocalDateTime createDate;
	private final LocalDateTime modifyDate;
	private final String authorUsername;
	private final int voterCount;
	private final List<AnswerResponseDto> answerList;
	
	public QuestionDetailDto(Question question) {
		this.id = question.getId();
		this.subject = question.getSubject();
		this.content = question.getContent();
		this.createDate = question.getCreateDate();
		this.modifyDate = question.getModifyDate();
		this.authorUsername = ( question.getAuthor() != null ) ? question.getAuthor().getUsername() : null;
		this.voterCount = ( question.getVoter() != null ) ? question.getVoter().size() : 0;		
		this.answerList = ( question.getAnswerList() != null) ? question.getAnswerList().stream().map(AnswerResponseDto::new).toList() : Collections.emptyList();
	}
}
