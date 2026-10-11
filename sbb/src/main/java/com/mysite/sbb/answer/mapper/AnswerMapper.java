package com.mysite.sbb.answer.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.mysite.sbb.answer.dto.AnswerResponseDto;

@Mapper
public interface AnswerMapper {
	List<AnswerResponseDto> findByQuestionId(@Param("questionId") Integer questionId, @Param("sort") String sort,
			@Param("offset") int offset, @Param("limit") int limit);

	long countByQuestionId(@Param("questionId") Integer questionId);
}
