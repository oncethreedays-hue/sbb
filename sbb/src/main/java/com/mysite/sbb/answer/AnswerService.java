package com.mysite.sbb.answer;

import com.mysite.sbb.user.service.UserService;
import com.mysite.sbb.user.entity.SiteUser;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.mysite.sbb.DataNotFoundException;
import com.mysite.sbb.answer.entity.Answer;
import com.mysite.sbb.question.entity.Question;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class AnswerService {

	private final AnswerRepository answerRepository;

	public Answer create(Question question, String content, SiteUser author) {
		Answer answer = new Answer(content, LocalDateTime.now(), question, author);
		this.answerRepository.save(answer);
		return answer;
	}

	public Answer getAnswer(Integer id) {
		return this.answerRepository.findById(id).orElseThrow(() -> new DataNotFoundException("answer not found"));
	}

	public void modify(Answer answer, String content) {
		answer.updateContent(content, LocalDateTime.now());
		this.answerRepository.save(answer);
	}

	public void delete(Answer answer) {
		this.answerRepository.delete(answer);
	}

	public void vote(Answer answer, SiteUser siteUser) {
		answer.getVoter().add(siteUser);
		this.answerRepository.save(answer);
	}
}
