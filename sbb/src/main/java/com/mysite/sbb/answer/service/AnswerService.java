package com.mysite.sbb.answer.service;

import org.springframework.data.domain.Page;

import com.mysite.sbb.answer.dto.AnswerResponseDto;

public interface AnswerService {
	Page<AnswerResponseDto> getList(Integer questionId, int page, String sort);

	AnswerResponseDto create(Integer questionId, String content, String username);

	void modify(Integer  id, String content, String username);

	void delete(Integer id, String username);

	void vote(Integer id, String username);

	void cancelVote(Integer id, String username);

}
