package com.mysite.sbb.question.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.mysite.sbb.question.dto.QuestionResponseDto;
import com.mysite.sbb.question.entity.Question;

@Mapper
public interface QuestionMapper {
	List<QuestionResponseDto> findAllByKeyword(@Param("kw") String kw, @Param("offset") int offset,
			@Param("limit") int limit);

	long countByKeyword(@Param("kw") String kw);

}
