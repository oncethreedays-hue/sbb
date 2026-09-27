package com.mysite.sbb.answer;

import java.security.Principal;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.mysite.sbb.answer.dto.AnswerRequestDto;
import com.mysite.sbb.answer.dto.AnswerResponseDto;
import com.mysite.sbb.question.Question;
import com.mysite.sbb.question.QuestionService;
import com.mysite.sbb.user.SiteUser;
import com.mysite.sbb.user.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequestMapping("/api")
@RequiredArgsConstructor
@RestController
public class AnswerController {

	private final QuestionService questionService;
	private final AnswerService answerService;
	private final UserService userService;

	@PreAuthorize("isAuthenticated()")
	@PostMapping("/questions/{questionId}/answers")
	public ResponseEntity<AnswerResponseDto> createAnswer(@PathVariable("questionId") Integer questionId,
			@Valid @RequestBody AnswerRequestDto requestDto, Principal principal) {

		Question question = this.questionService.getQuestion(questionId);
		SiteUser siteUser = this.userService.getUser(principal.getName());

		Answer answer = this.answerService.create(question, requestDto.getContent(), siteUser);

		return ResponseEntity.status(HttpStatus.CREATED).body(new AnswerResponseDto(answer));
	}

	@PreAuthorize("isAuthenticated()")
	@PutMapping("/answer/{id}")
	public ResponseEntity<AnswerResponseDto> modifyAnswer(@PathVariable("id") Integer id,
			@Valid @RequestBody AnswerRequestDto requestDto, Principal principal) {

		Answer answer = answerService.getAnswer(id);

		if (!answer.getAuthor().getUsername().equals(principal.getName())) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "수정 권한이 없습니다.");
		} else {
			this.answerService.modify(answer, requestDto.getContent());
		}
		return ResponseEntity.ok(new AnswerResponseDto(answer));

	}

	@PreAuthorize("isAuthenticated()")
	@DeleteMapping("/answer/{id}")
	public ResponseEntity<Void> deleteAnswer(@PathVariable("id") Integer id, Principal principal) {

		Answer answer = answerService.getAnswer(id);

		if (!answer.getAuthor().getUsername().equals(principal.getName())) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "삭제 권한이 없습니다.");
		} else {
			this.answerService.delete(answer);
		}
		return ResponseEntity.ok().build();
	}

	@PreAuthorize("isAuthenticated()")
	@GetMapping("/answer/{id}/vote")
	public ResponseEntity<AnswerResponseDto> voteAnswer(@PathVariable("id") Integer id, Principal principal) {

		Answer answer = answerService.getAnswer(id);
		SiteUser siteUser = this.userService.getUser(principal.getName());

		this.answerService.vote(answer, siteUser);
		return ResponseEntity.ok(new AnswerResponseDto(answer));
	}

}
