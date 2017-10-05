package service;

import java.util.List;

import core.Student;
import db.StudentDatabaseHelper;

public class StudentDao extends GenericDao<Student> {
	public Student getByCedula(String cedula) {
		return StudentDatabaseHelper.getByUniqueIdentifier("cedula", cedula);
	}

	public Student getByStudentId(String studentId) {
		return StudentDatabaseHelper.getByUniqueIdentifier("studentId", studentId);
	}
	
	@Override
	public List<Student> getAll() {
		return StudentDatabaseHelper.getAll();
	}

	@Override
	public Student create(Student student) {
		return StudentDatabaseHelper.create(student);
	}

	@Override
	public boolean delete(String uuid) {
		return StudentDatabaseHelper.deleteByUniqueIdentifier("uuid", uuid);
	}

	@Override
	public Student get(String uuid) {
		return StudentDatabaseHelper.getByUniqueIdentifier("uuid", uuid);
	}

	@Override
	public boolean update(String uuid, Student updatedStudent) {
		return StudentDatabaseHelper.updateByUniqueIdentifier("uuid", uuid, updatedStudent);
	}
}