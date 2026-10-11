package com.mysite.sbb.answer.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mysite.sbb.answer.entity.Answer;

public interface AnswerRepository extends JpaRepository<Answer, Integer>{
	

}
