package com.mysite.sbb.answer.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.mysite.sbb.answer.dto.AnswerResponseDto;
import com.mysite.sbb.answer.entity.Answer;
import com.mysite.sbb.answer.mapper.AnswerMapper;
import com.mysite.sbb.answer.repository.AnswerRepository;
import com.mysite.sbb.question.entity.Question;
import com.mysite.sbb.question.repository.QuestionRepository;
import com.mysite.sbb.user.entity.SiteUser;
import com.mysite.sbb.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnswerServiceImpl implements AnswerService {

	private static final int PAGE_SIZE = 10;

	private final AnswerRepository answerRepository;
	private final AnswerMapper answerMapper;
	private final QuestionRepository questionRepository;
	private final UserRepository userRepository;

	@Override
	public Page<AnswerResponseDto>getList(Integer questionId, int page, String sort) {
		if (!questionRepository.existsById(questionId)) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "question not found");

		}
		Pageable pageable = PageRequest.of(Math.max(page, 0), PAGE_SIZE);
		String sortKey = "votes".equals(sort) ? "votes" : "latest";

		List<AnswerResponseDto> content = answerMapper.findByQuestionId(questionId, sortKey, (int) pageable.getOffset(),
				pageable.getPageSize());
		long total = answerMapper.countByQuestionId(questionId);
		return new PageImpl<>(content, pageable, total);

	}

	@Override
	@Transactional
	public AnswerResponseDto create(Integer questionId, String content, String username) {
		Question question = questionRepository.findById(questionId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "question not found"));
		SiteUser author = findUser(username);
		Answer saved = answerRepository.save(new Answer(content, question, author));
		return AnswerResponseDto.from(saved);

	}

	@Override
	@Transactional
	public void modify(Integer id, String content, String username) {
		Answer answer = findAnswer(id);
		checkAuthor(answer, username);
		answer.update(content);

	}

	@Override
	@Transactional
	public void delete(Integer id, String username) {
		Answer answer = findAnswer(id);
		checkAuthor(answer, username);
		answerRepository.delete(answer);

	}

	@Override
	@Transactional
	public void vote(Integer id, String username) {
		findAnswer(id).addVoter(findUser(username));

	}

	@Override
	@Transactional
	public void cancelVote(Integer id, String username) {
		findAnswer(id).removeVoter(findUser(username));

	}

	private SiteUser findUser(String username) {
		return userRepository.findByUsername(username)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "user not found"));
	}

	private void checkAuthor(Answer answer, String username) {
		SiteUser author = answer.getAuthor();
		if (author == null || !author.getUsername().equals(username)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "권한이 없습니다.");
		}

	}

	private Answer findAnswer(Integer id) {
		return answerRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "answer not found"));
	}

}
