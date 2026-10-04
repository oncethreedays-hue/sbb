package com.mysite.sbb.question.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.mysite.sbb.question.dto.QuestionDetailDto;
import com.mysite.sbb.question.dto.QuestionResponseDto;
import com.mysite.sbb.question.entity.Question;
import com.mysite.sbb.question.mapper.QuestionMapper;
import com.mysite.sbb.question.repository.QuestionRepository;
import com.mysite.sbb.user.entity.SiteUser;
import com.mysite.sbb.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuestionServiceImpl implements QuestionService {

	private static final int PAGE_SIZE = 10;

	private final QuestionRepository questionRepository;
	private final QuestionMapper questionMapper;
	private final UserRepository userRepository;

	@Override
	public Page<QuestionResponseDto> getList(int page, String kw) {
		Pageable pageable = PageRequest.of(Math.max(page, 0), PAGE_SIZE);
		List<QuestionResponseDto> content = questionMapper.findAllByKeyword(kw, (int) pageable.getOffset(),
				pageable.getPageSize());
		long total = questionMapper.countByKeyword(kw);
		return new PageImpl<>(content, pageable, total);
	}

	@Override
	public QuestionDetailDto getQuestionDetail(Integer id) {
		return new QuestionDetailDto(findQuestion(id));
	}

	private Question findQuestion(Integer id) {
		return questionRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "question not found"));
	}

	@Override
	@Transactional
	public Integer create(String subject, String content, String username) {
		SiteUser author = findUser(username);
		Question question = new Question(subject, content, author);
		return questionRepository.save(question).getId();
	}

	private SiteUser findUser(String username) {
		return userRepository.findByUsername(username)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "user not found"));
	}

	@Override
	@Transactional
	public void modify(Integer id, String subject, String content, String username) {
		Question question = findQuestion(id);
		checkAuthor(question, username);
		question.update(subject, content);
	}

	private void checkAuthor(Question question, String username) {
		String authorname = (question.getAuthor() != null) ? question.getAuthor().getUsername() : null;
		if (!Objects.equals(authorname, username)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "권한이 없습니다.");
		}

	}

	@Override
	@Transactional
	public void delete(Integer id, String username) {
		Question question = findQuestion(id);
		checkAuthor(question, username);
		questionRepository.delete(question);

	}

	@Override
	@Transactional
	public void vote(Integer id, String username) {
		Question question = findQuestion(id);
		question.addVoter(findUser(username));

	}

	@Override
	@Transactional
	public void cancelVote(Integer id, String username) {
		Question question = findQuestion(id);
		question.removeVoter(findUser(username));

	}

}
