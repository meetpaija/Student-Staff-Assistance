package com.assistance.studentstaff.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@Entity
@Table(name = "enroll_course")
@JsonInclude(value = Include.NON_NULL)
public class EnrollCourseModel implements Serializable {

	private static final long serialVersionUID = 2789226849578701827L;

	@EmbeddedId
	private EnrollCourseIdModel enrollCourseId;

	@Column(name = "approved", nullable = false, columnDefinition = "TINYINT(1)")
	private Boolean approved;

	public EnrollCourseIdModel getEnrollCourseId() {
		return enrollCourseId;
	}

	public void setEnrollCourseId(EnrollCourseIdModel enrollCourseId) {
		this.enrollCourseId = enrollCourseId;
	}

	public Boolean isApproved() {
		return approved;
	}

	public void setApproved(Boolean approved) {
		this.approved = approved;
	}

}
