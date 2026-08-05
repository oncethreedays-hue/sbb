package com.mysite.sbb;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.User;

import com.mysite.sbb.answer.Answer;
import com.mysite.sbb.answer.AnswerRepository;
import com.mysite.sbb.question.Question;
import com.mysite.sbb.question.QuestionRepository;
import com.mysite.sbb.question.QuestionService;
import com.mysite.sbb.user.SiteUser;
import com.mysite.sbb.user.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@SpringBootTest
class SbbApplicationTests {

	@Autowired
	private QuestionService questionService;
	@Autowired
	private QuestionRepository questionRepository;
	@Autowired
	private AnswerRepository answerRepository;
	@Autowired
	private UserRepository userRepository;

	@Test
	@Transactional
	void testJpa() {

//		SiteUser testUser = new SiteUser();
//		testUser.setEmail("test@test.com");
//		testUser.setPassword("123");
//		testUser.setUsername("test");
//		this.userRepository.save(testUser);

		Optional<SiteUser> testUser = this.userRepository.findByusername("test");

		if (testUser.isPresent()) {
			SiteUser testUser2 = testUser.get();

			Question question = new Question();
			question.setSubject("Test Subject");
			question.setContent("Test Content");
			question.setCreateDate(LocalDateTime.now());
			question.setAuthor(testUser2);
			this.questionRepository.save(question);

			Optional<Question> qOptional = this.questionRepository.findById(0);
			if (qOptional.isPresent()) {
				Question question2 = qOptional.get();
				assertEquals("Test Subject", question2.getSubject());
			} else {
				System.out.println("Check Question DB");
			}
		} else {
			System.out.println("Check User DB");
		}

//    	List<Question> all = this.questionRepository.findAll();
//    	assertEquals(2, all.size());
//    	
//    	Question q = all.get(0);
//    	assertEquals("sbb가 무엇인가요?", q.getSubject());

//    	Optional<Question> oq = this.questionRepository.findById(1);
//    	
//    	if (oq.isPresent()) {
//    		Question q = oq.get();
//    		assertEquals("sbb가 무엇인가요?", q.getSubject());
//    	}

//    	Question q = this.questionRepository.findBySubject("sbb가 무엇인가요?");
//    	assertEquals(1, q.getId());

//    	Question q = questionRepository.findBySubjectAndContent("sbb는 무엇인가요?", "sbb에 대해서 알소 싶습니다.");
//    	assertEquals(1, q.getId());

//    	List<Question> qList = this.questionRepository.findBySubjectLike({"sbb%");
//    	Question q = qList.get(0);
//    	assertEquals("sbb가 무엇인가요?", q.getSubject());

//    	Optional<Question> oq = this.questionRepository.findById(1);
//    	assertTrue(oq.isPresent());
//    	Question q = oq.get();
//    	q.setSubject("수정된 제목");
//    	this.questionRepository.save(q);

//    	assertEquals(2, this.questionRepository.count());
//    	Optional<Question> oq = this.questionRepository.findById(1);
//    	assertTrue(oq.isPresent());
//    	Question q = oq.get();
//    	this.questionRepository.delete(q);
//    	assertEquals(1, this.questionRepository.count());

//    	Optional<Question> oq = this.questionRepository.findById(2);
//    	assertTrue(oq.isPresent());
//    	Question q = oq.get();
//    	
//    	Answer a = new Answer();
//    	a.setContent("자동으로 생성됩니다.");
//    	a.setQuestion(q);
//    	a.setCreateDate(LocalDateTime.now());
//    	this.answerRepository.save(a);

//    	Optional<Answer> oa = this.answerRepository.findById(1);
//    	assertTrue(oa.isPresent());
//    	Answer a = oa.get();
//    	assertEquals(2, a.getQuestion().getId());

//		Optional<Question> oq = this.questionRepository.findById(2);
//		assertTrue(oq.isPresent());
//		Question q = oq.get();
//
//		List<Answer> aList = q.getAnswerList();
//		assertEquals(1, aList.size());
//		assertEquals("네 자동으로 생성됩니다.", aList.get(0).getContent());

	}
}
