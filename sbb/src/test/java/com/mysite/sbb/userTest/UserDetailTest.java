package com.mysite.sbb.userTest;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;

import com.mysite.sbb.BaseTest;
import com.mysite.sbb.answer.entity.Answer;
import com.mysite.sbb.answer.repository.AnswerRepository;
import com.mysite.sbb.question.entity.Question;
import com.mysite.sbb.question.repository.QuestionRepository;
import com.mysite.sbb.user.entity.SiteUser;

public class UserDetailTest extends BaseTest {

	@Autowired
	QuestionRepository questionRepository;
	@Autowired
	AnswerRepository answerRepository;

	private SiteUser writer;
	private SiteUser other;
	private Question firestQuestion;
	private Question lastQuestion;

	@BeforeEach
	void setUpData() {
		writer = userRepository.findByUsername("writer").orElseThrow();
		other = userRepository.findByUsername("other").orElseThrow();

		Question last = null;
		for (int i = 1; i <= 7; i++) {
			Question q = questionRepository.save(new Question("질문" + i, "내용" + i, writer));
			if (i == 1)
				firestQuestion = q;
			last = q;
		}
		lastQuestion = last;

		for (int i = 1; i <= 6; i++) {
			answerRepository.save(new Answer("내 답변" + i, firestQuestion, writer));
		}
		answerRepository.save(new Answer("남의 답변1", lastQuestion, other));
		answerRepository.save(new Answer("남의 답변2", lastQuestion, other));

		flushAndClear();
	}
}
