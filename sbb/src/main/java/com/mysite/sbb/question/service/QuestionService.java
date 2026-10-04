package com.mysite.sbb.question.service;

import org.springframework.data.domain.Page;

import com.mysite.sbb.question.dto.QuestionDetailDto;
import com.mysite.sbb.question.dto.QuestionResponseDto;

public interface QuestionService {
	Page<QuestionResponseDto> getList(int page, String kw);

	QuestionDetailDto getQuestionDetail(Integer id);

	Integer create(String subject, String content, String username);

	void modify(Integer id, String subject, String content, String username);

	void delete(Integer id, String username);

	void vote(Integer id, String username);
	
	void cancelVote(Integer id, String username);
}
