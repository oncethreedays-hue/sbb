package com.mysite.sbb.question;

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
import com.mysite.sbb.user.service.UserService;
import com.mysite.sbb.user.entity.SiteUser;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/questions")
public class QuestionRestController {

	private final QuestionService questionService;
	private final UserService userService;

	// 1. 질문 목록 조회 API(GET /api/questions?page=0&kw=)
	@GetMapping
	public ResponseEntity<Page<QuestionResponseDto>> getQuestionList(
			@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "kw", defaultValue = "") String kw) {

		// 1. 서비스의 페이징/검색 메서드 호출
		Page<QuestionResponseDto> questionResponseDtoPage = this.questionService.getList(page, kw);

		// 2. Page<QuestionDto> 객체를 그래도 JSON으로 변환
		return ResponseEntity.ok(questionResponseDtoPage);
	}

	// 2. 질문 상세 조회(GET /api/question/{id})
	@GetMapping("/{id}")
	public ResponseEntity<QuestionDetailDto> getQuestionDetail(@PathVariable("id") Integer id) {
		Question question = this.questionService.getQuestion(id);
		QuestionDetailDto questionDetailDto = new QuestionDetailDto(question);
		return ResponseEntity.ok(questionDetailDto);
	}

	// 3. 질문 등록 (POST /api/question)
	@PreAuthorize("isAuthenticated()")
	@PostMapping
	public ResponseEntity<QuestionResponseDto> createQuestion(@Valid @RequestBody QuestionRequestDto questionRequestDto,
			Principal principal) {
		SiteUser siteUser = this.userService.getUser(principal.getName());

		Question savedQuestion = this.questionService.create(questionRequestDto.getSubject(),
				questionRequestDto.getContent(), siteUser);

		// TODO: 테스트 용
		return ResponseEntity.status(HttpStatus.CREATED).body(new QuestionResponseDto(savedQuestion));
//		return ResponseEntity.status(HttpStatus.CREATED).build();
	}

	// 4. 질문 수정 (PUT /api/question/{id})
	@PreAuthorize("isAuthenticated()")
	@PutMapping("/{id}")
	public ResponseEntity<Void> modifyQuestion(@Valid @RequestBody QuestionRequestDto questionRequestDto,
			@PathVariable("id") Integer id, Principal principal) {
		Question question = this.questionService.getQuestion(id);

		if (!question.getAuthor().getUsername().equals(principal.getName())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정권한이 없습니다.");
		}

		this.questionService.modify(question, questionRequestDto.getSubject(), questionRequestDto.getContent());
		return ResponseEntity.ok().build();
	}

	// 5. 질문 삭제 (DELETE /api/question/{id})
	@PreAuthorize("isAuthenticated()")
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteQuestion(@PathVariable("id") Integer id, Principal principal) {
		Question question = this.questionService.getQuestion(id);

		if (!question.getAuthor().getUsername().equals(principal.getName())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "삭제권한이 없습니다.");
		}
		questionService.delete(question);
		return ResponseEntity.noContent().build();
	}

	// 6. 질문 추천 (POST /api/questions/vote/{id})
	@PreAuthorize("isAuthenticated()")
	@PostMapping("/vote/{id}")
	public ResponseEntity<Void> voteQuestion(@PathVariable Integer id, Principal principal) {
		Question question = this.questionService.getQuestion(id);
		SiteUser siteUser = this.userService.getUser(principal.getName());

		this.questionService.vote(question, siteUser);
		return ResponseEntity.ok().build();
	}
}
