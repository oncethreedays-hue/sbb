package com.mysite.sbb.question.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mysite.sbb.question.entity.Question;

public interface QuestionRepository extends JpaRepository<Question, Integer> {

}
