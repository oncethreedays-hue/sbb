package com.mysite.sbb.question;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import com.mysite.sbb.answer.Answer;
import com.mysite.sbb.user.entity.SiteUser;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Question {
	
	@Id
	@GeneratedValue( strategy = GenerationType.IDENTITY)
	private Integer id;
	
	@Column(length = 200)
	private String subject;
	
	@Column(columnDefinition = "TEXT")
	private String content;
	
	private LocalDateTime createDate;
	
	@OneToMany(mappedBy = "question", cascade = CascadeType.REMOVE)
	private List<Answer> answerList;
	
	@ManyToOne
	private SiteUser author;
	
	private LocalDateTime modifyDate;
	
	@ManyToMany
	Set<SiteUser> voter;
	
	public Question(String subject, String content, SiteUser author) {
		this.subject = subject;
		this.content = content;
		this.author = author;
		this.createDate = LocalDateTime.now();
	}
	
	public void update(String subject, String content) {
		this.subject = subject;
		this.content = content;
		this.modifyDate = LocalDateTime.now();
	}
}