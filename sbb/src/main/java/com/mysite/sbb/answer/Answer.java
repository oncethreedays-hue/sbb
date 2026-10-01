package com.mysite.sbb.answer;

import java.time.LocalDateTime;
import java.util.Set;

import com.mysite.sbb.question.Question;
import com.mysite.sbb.user.entity.SiteUser;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Answer {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(columnDefinition = "TEXT")
	private String content;

	private LocalDateTime createDate;

	@ManyToOne
	private Question question;

	@ManyToOne
	private SiteUser author;

	private LocalDateTime modifyDate;

	@ManyToMany
	Set<SiteUser> voter;

	public Answer(String content, LocalDateTime createDate, Question question, SiteUser author) {
		this.content = content;
		this.createDate = createDate;
		this.question = question;
		this.author = author;
	}

	public void updateContent(String content, LocalDateTime modifyDate) {
		this.content = content;
		this.modifyDate = modifyDate;
	}

}