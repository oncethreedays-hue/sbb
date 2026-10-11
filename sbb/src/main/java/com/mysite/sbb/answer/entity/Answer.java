package com.mysite.sbb.answer.entity;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import com.mysite.sbb.question.entity.Question;
import com.mysite.sbb.user.entity.SiteUser;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Answer {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(columnDefinition = "TEXT")
	private String content;

	private LocalDateTime createDate;
	private LocalDateTime modifyDate;

	@ManyToOne(fetch = FetchType.LAZY)
	private Question question;

	@ManyToOne(fetch = FetchType.LAZY)
	private SiteUser author;

	@ManyToMany
	private Set<SiteUser> voter = new HashSet<>();

	public Answer(String content, Question question, SiteUser author) {
		this.content = content;
		this.question = question;
		this.author = author;
		this.createDate = LocalDateTime.now();

	}

	public void update(String content) {
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