package com.assistance.studentstaff.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.assistance.studentstaff.model.CourseModel;

@Repository
public interface ICourseRepo extends JpaRepository<CourseModel, String> {

	@Query("SELECT course FROM CourseModel course where course.courseName = :courseName")
	CourseModel findByName(String courseName);

	@Query("SELECT course FROM CourseModel course where course.courseId = :id")
	CourseModel findCourseById(String id);

	@Query("SELECT course FROM CourseModel course where course.deptId = :deptId and course.progId = :progId")
	List<CourseModel> findByDeptAndProgram(String deptId, String progId);
}
