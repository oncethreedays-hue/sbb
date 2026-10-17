package com.mysite.sbb.answer.controller;

import java.security.Principal;

import org.springframework.data.domain.Page;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mysite.sbb.answer.dto.AnswerRequestDto;
import com.mysite.sbb.answer.dto.AnswerResponseDto;
import com.mysite.sbb.answer.service.AnswerService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class AnswerRestController {

	private final AnswerService answerService;

	// 목록 (공개): GET /api/questions/{questionId}/answers?page=0&sort=latest|votes
	@GetMapping("/questions/{questionId}/answers")
	public ResponseEntity<Page<AnswerResponseDto>> getAnswers(@PathVariable("questionId") Integer questionId,
			@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "sort", defaultValue = "latest") String sort) {
		return ResponseEntity.ok(answerService.getList(questionId, page, sort));
	}

	// 등록: 201 + 생성된 답변 (화면이 목록에 바로 추가할 수 있도록 본문 반환)
	@PreAuthorize("isAuthenticated()")
	@PostMapping("/questions/{questionId}/answers")
	public ResponseEntity<AnswerResponseDto> createAnswer(@PathVariable("questionId") Integer questionId,
			@Valid @RequestBody AnswerRequestDto request, Principal prinPrincipal) {
		AnswerResponseDto created = answerService.create(questionId, request.getContent(), prinPrincipal.getName());
		return ResponseEntity.status(HttpStatus.CREATED).body(created);
	}

	@PreAuthorize("isAuthenticated()")
	@PutMapping("/answers/{id}")
	public ResponseEntity<Void> modifyAnswer(@PathVariable("id") Integer id,
			@Valid @RequestBody AnswerRequestDto request, Principal principal) {
		answerService.modify(id, request.getContent(), principal.getName());
		return ResponseEntity.noContent().build();
	}

	@PreAuthorize("isAuthenticated()")
	@DeleteMapping("/answers/{id}")
	public ResponseEntity<Void> deleteAnswer(@PathVariable("id") Integer id, Principal prinPrincipal) {
		answerService.delete(id, prinPrincipal.getName());
		return ResponseEntity.noContent().build();
	}

	@PreAuthorize("isAuthenticated()")
	@PostMapping("/answers/{id}/vote")
	public ResponseEntity<Void> voteAnswer(@PathVariable("id") Integer id, Principal principal) {
		answerService.vote(id, principal.getName());
		return ResponseEntity.noContent().build();
	}

	@PreAuthorize("isAuthenticated()")
	@DeleteMapping("/answers/{id}/vote")
	public ResponseEntity<Void> cancel(@PathVariable("id") Integer id, Principal principal) {
		answerService.cancelVote(id, principal.getName());
		return ResponseEntity.noContent().build();
	}

}
