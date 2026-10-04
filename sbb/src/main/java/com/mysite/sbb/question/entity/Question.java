package com.mysite.sbb.question.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.mysite.sbb.answer.entity.Answer;
import com.mysite.sbb.user.entity.SiteUser;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Question {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(length = 200)
	private String subject;

	@Column(columnDefinition = "TEXT")
	private String content;

	private LocalDateTime createDate;

	@OneToMany(mappedBy = "question", cascade = CascadeType.REMOVE)
	private List<Answer> answerList = new ArrayList<>();

	@ManyToOne(fetch = FetchType.LAZY)
	private SiteUser author;

	private LocalDateTime modifyDate;

	@ManyToMany
	private Set<SiteUser> voter = new HashSet<>();

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

	public void addVoter(SiteUser user) {
		this.voter.add(user);
	}
	
	public void removeVoter(SiteUser user) {
		this.voter.remove(user);
	}
}