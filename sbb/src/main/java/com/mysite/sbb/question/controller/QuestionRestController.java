package com.mysite.sbb.question.controller;

import java.net.URI;
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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.mysite.sbb.question.dto.QuestionDetailDto;
import com.mysite.sbb.question.dto.QuestionRequestDto;
import com.mysite.sbb.question.dto.QuestionResponseDto;
import com.mysite.sbb.question.entity.Question;
import com.mysite.sbb.question.service.QuestionService;
import com.mysite.sbb.user.service.UserService;
import com.mysite.sbb.user.entity.SiteUser;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/questions")
public class QuestionRestController {

	private final QuestionService questionService;

	// 1. 목록 조회 (GET /api/questions?page=0&kw=)
	@GetMapping
	public ResponseEntity<Page<QuestionResponseDto>> getQuestionList(
			@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "kw", defaultValue = "") String kw) {

		return ResponseEntity.ok(questionService.getList(page, kw));

	}

	// 2. 상세 조회 (GET /api/questions/{id})
	@GetMapping("/{id}")
	public ResponseEntity<QuestionDetailDto> getQuestionDetail(@PathVariable("id") Integer id) {
		return ResponseEntity.ok(questionService.getQuestionDetail(id));
	}

	// 3. 등록 (POST /api/questions) -> 201 + Location
	@PreAuthorize("isAuthenticated()")
	@PostMapping
	public ResponseEntity<Void> createQuestion(@Valid @RequestBody QuestionRequestDto request, Principal principal) {
		Integer id = questionService.create(request.getSubject(), request.getContent(), principal.getName());
		return ResponseEntity.created(URI.create("/api/questions/" + id)).build();
	}

	// 4. 수정 (PUT /api/questions/{id}) -> 204
	@PreAuthorize("isAuthenticated()")
	@PutMapping("/{id}")
	public ResponseEntity<Void> modifyQuestion(@PathVariable("id") Integer id,
			@Valid @RequestBody QuestionRequestDto request, Principal principal) {
		questionService.modify(id, request.getSubject(), request.getContent(), principal.getName());
		return ResponseEntity.noContent().build();
	}

	// 5. 삭제 (DELETE /api/questions/{id}) -> 204
	@PreAuthorize("isAuthenticated()")
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteQuestion(@PathVariable("id") Integer id, Principal principal) {
		questionService.delete(id, principal.getName());
		return ResponseEntity.noContent().build();
	}

	// 6. 추천 (POST /api/questions/{id}/vote) -> 204
	@PreAuthorize("isAuthenticated()")
	@PostMapping("/{id}/vote")
	public ResponseEntity<Void> voteQuestion(@PathVariable("id") Integer id, Principal principal) {
		questionService.vote(id, principal.getName());
		return ResponseEntity.noContent().build();
	}

	// 7. 추천 취소 (DELETE /api/questions/{id}/vote) -> 204
	@PreAuthorize("isAuthenticated()")
	@DeleteMapping("/{id}/vote")
	public ResponseEntity<Void> cancelVote(@PathVariable("id") Integer id, Principal principal) {
		questionService.cancelVote(id, principal.getName());
		return ResponseEntity.noContent().build();
	}
}
