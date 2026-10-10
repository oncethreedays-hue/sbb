package com.mysite.sbb.user.mapper;

import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.mysite.sbb.user.dto.MyAnswerDto;
import com.mysite.sbb.user.dto.MyQuestionDto;
import com.mysite.sbb.user.dto.UserSummaryRow;

@Mapper
public interface UserMapper {
	Optional<UserSummaryRow> findSummaryByUsername(@Param("username") String username);

	List<MyQuestionDto> findRecentQuestion(@Param("userId") Long userId, @Param("limit") int limit);

	List<MyAnswerDto> findRecentAnswer(@Param("userId") Long userId, @Param("limit") int limit);
}
