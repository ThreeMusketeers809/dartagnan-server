package db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.mysql.jdbc.Statement;

import core.PhoneNumber;
import core.Student;

/**
 * Helper class for accessing and modifying Student data.
 * 
 * @author Francisco Frias
 * @author Abel Guzman
 * @author Amin Guzman
 *
 */
public class StudentDatabaseHelper {

	/**
	 * 
	 * @return A list of all the Students in the database or an empty list, if no
	 *         Students exist.
	 */
	public static List<Student> getAll() {

		List<Student> studentList = new ArrayList<Student>();

		try {
			DatabaseConnection connection = new DatabaseConnection();

			// First fetch Student data
			String studentQuery = "SELECT * FROM VW_Student";
			PreparedStatement studentStatement = connection.getConnection().prepareStatement(studentQuery);
			ResultSet studentResultSet = studentStatement.executeQuery();

			while (studentResultSet.next()) {
				int studentPkId = studentResultSet.getInt("StudentPk_id");
				String studentEntityId = studentResultSet.getString("uuid");
				String studentId = studentResultSet.getString("studentId");
				String firstName = studentResultSet.getString("firstName");
				String middleName = studentResultSet.getString("middleName");
				String firstSurname = studentResultSet.getString("firstSurname");
				String secondSurname = studentResultSet.getString("secondSurname");
				String cedula = studentResultSet.getString("cedula");
				String email = studentResultSet.getString("email");
				String address = studentResultSet.getString("address");
				String status = studentResultSet.getString("status");
				Student student = new Student();
				student.setEntityId(studentEntityId);
				student.setStudentId(studentId);
				student.setFirstName(firstName);
				student.setMiddleName(middleName);
				student.setFirstSurname(firstSurname);
				student.setSecondSurname(secondSurname);
				student.setCedula(cedula);
				student.setEmail(email);
				student.setAddress(address);
				student.setStatus(status);
				// Then we fetch the associated phone numbers for the current student
				List<PhoneNumber> phoneNumbers = StudentPhoneNumberDatabaseHelper.getAll(studentPkId);
				student.setPhoneNumbers(phoneNumbers);

				// Add to the list
				studentList.add(student);
			}
			// Close unused resources
			studentResultSet.close();
			studentStatement.close();
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return studentList;
	}

	/**
	 * 
	 * @param identifier
	 *            The name of the column with an unique identifier that we want to
	 *            use to locate the Student.
	 * @param value
	 *            The value stored in the identifier for the particular student we
	 *            want to fetch.
	 * @return An Student object representing the Student found under the specified
	 *         identifier, or null if none found.
	 */
	public static Student getByUniqueIdentifier(String identifier, String value) {

		Student student = null;

		try {
			DatabaseConnection connection = new DatabaseConnection();

			// First we fetch Student data
			String studentQuery = String.format("SELECT * FROM VW_Student WHERE %s=?", identifier);
			PreparedStatement studentStatement = connection.getConnection().prepareStatement(studentQuery);
			studentStatement.setString(1, value);
			ResultSet studentResultSet = studentStatement.executeQuery();

			/*
			 * Since we're querying via an unique identifier, it's safe to assume the result
			 * set contains either zero or one results.
			 */
			if (studentResultSet.next()) {
				student = new Student();
				int studentPk_id = studentResultSet.getInt("StudentPk_id");
				String studentEntityId = studentResultSet.getString("uuid");
				String studentId = studentResultSet.getString("studentId");
				String firstName = studentResultSet.getString("firstName");
				String middleName = studentResultSet.getString("middleName");
				String firstSurname = studentResultSet.getString("firstSurname");
				String secondSurname = studentResultSet.getString("secondSurname");
				String cedula = studentResultSet.getString("cedula");
				String email = studentResultSet.getString("email");
				String address = studentResultSet.getString("address");
				String status = studentResultSet.getString("status");
				student.setEntityId(studentEntityId);
				student.setStudentId(studentId);
				student.setFirstName(firstName);
				student.setMiddleName(middleName);
				student.setFirstSurname(firstSurname);
				student.setSecondSurname(secondSurname);
				student.setCedula(cedula);
				student.setEmail(email);
				student.setAddress(address);
				student.setStatus(status);

				// Then we fetch the associated phone numbers
				List<PhoneNumber> phoneNumbers = StudentPhoneNumberDatabaseHelper.getAll(studentPk_id);
				student.setPhoneNumbers(phoneNumbers);
			}

			// Close unused resources
			studentResultSet.close();
			studentStatement.close();
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return student;
	}

	/**
	 * 
	 * Inserts the values contained in the Student object passed into the database,
	 * creating a new entity to represent it.
	 * 
	 * @param newStudent
	 *            The student to be inserted into the database.
	 * @return A Student object representing the newly created entity, including its
	 *         associated entity ID, or null if the entity could not be created.
	 */
	public static Student create(Student newStudent) {
		Student createdStudent = null;

		try {
			DatabaseConnection connection = new DatabaseConnection();

			// Check if unique identifiers are in use
			if (getByUniqueIdentifier("cedula", newStudent.getCedula()) != null) {
				return createdStudent;
			} else if (getByUniqueIdentifier("studentId", newStudent.getStudentId()) != null) {
				return createdStudent;
			} else if (getByUniqueIdentifier("uuid", newStudent.getEntityId()) != null) {
				return createdStudent;
			}

			// First fetch key of the status to apply
			int statusPkId = StudentStatusDatabaseHelper.getStatusPkId(newStudent.getStatus());
			if (statusPkId < 0) {
				// If no status found, Student object must be malformed
				return createdStudent;
			}

			// First insert Student data
			String studentQuery = "INSERT INTO Student "
					+ "(studentId, firstName, middleName, firstSurname, secondSurname, cedula, email, address, StatusPk_id) "
					+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
			PreparedStatement studentStatement = connection.getConnection().prepareStatement(studentQuery,
					Statement.RETURN_GENERATED_KEYS);
			studentStatement.setString(1, newStudent.getStudentId());
			studentStatement.setString(2, newStudent.getFirstName());
			studentStatement.setString(3, newStudent.getMiddleName());
			studentStatement.setString(4, newStudent.getFirstSurname());
			studentStatement.setString(5, newStudent.getSecondSurname());
			studentStatement.setString(6, newStudent.getCedula());
			studentStatement.setString(7, newStudent.getEmail());
			studentStatement.setString(8, newStudent.getAddress());
			studentStatement.setInt(9, statusPkId);
			studentStatement.executeUpdate();
			ResultSet studentResultSet = studentStatement.getGeneratedKeys();

			// Check for generated keys, if any
			int studentPkId;
			if (studentResultSet.next()) {
				studentPkId = studentResultSet.getInt(1);
			} else {
				// An unknown error occurred while inserting the student
				return createdStudent;
			}

			// Close unused resources
			studentResultSet.close();
			studentStatement.close();
			connection.close();

			// If student data was inserted correctly, associate telephone numbers, if any
			if (newStudent.getPhoneNumbers() != null) {
				for (PhoneNumber p : newStudent.getPhoneNumbers()) {
					int phoneNumberPkId = PhoneNumberDatabaseHelper.resolve(p);
					StudentPhoneNumberDatabaseHelper.link(phoneNumberPkId, studentPkId);
				}
			}

			// Operation completed if reached this line
			createdStudent = getByUniqueIdentifier("StudentPk_id", String.valueOf(studentPkId));
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return createdStudent;
	}

	/**
	 * 
	 * @param identifier
	 *            The name of the column with an unique identifier that we want to
	 *            use to locate the Student.
	 * @param value
	 *            The value stored in the identifier for the particular student we
	 *            want to delete.
	 * @return True if Student was successfully deleted, false otherwise.
	 */
	public static boolean deleteByUniqueIdentifier(String identifier, String value) {
		boolean opResult = false;

		try {
			DatabaseConnection connection = new DatabaseConnection();

			// Apply updates
			String deleteQuery = String.format("UPDATE Student SET MD_isDeleted=TRUE WHERE %s=?", identifier);
			PreparedStatement deleteStatement = connection.getConnection().prepareStatement(deleteQuery);
			deleteStatement.setString(1, value);
			int affectedRows = deleteStatement.executeUpdate();

			// Close unused resources
			deleteStatement.close();
			connection.close();

			// Operation failed if affected rows <= 0
			if (affectedRows > 0) {
				opResult = true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return opResult;
	}

	/**
	 * 
	 * @param identifier
	 *            The name of the column with an unique identifier that we want to
	 *            use to locate the Student.
	 * @param value
	 *            The value stored in the identifier for the particular student we
	 *            want to update.
	 * @param updatedStudent
	 *            A Student object containing the new data for the entity.
	 * @return true if update was successful, false otherwise.
	 */
	public static boolean updateByUniqueIdentifier(String identifier, String value, Student updatedStudent) {
		boolean opResult = false;

		try {
			DatabaseConnection connection = new DatabaseConnection();

			// Fetch student pk_id
			int studentPkId = getPkIdByUniqueIdentifier(identifier, value);
			if (studentPkId < 0) {
				// Student not found
				return opResult;
			}

			// Fetch student status pk_id
			int statusPkId = StudentStatusDatabaseHelper.getStatusPkId(updatedStudent.getStatus());
			if (statusPkId < 0) {
				// Student object malformed
				return opResult;
			}

			String updateQuery = "UPDATE Student SET "
					+ "studentId=?, firstName=?, middleName=?, firstSurname=?, secondSurname=?, "
					+ "cedula=?, email=?, address=?, StatusPk_id=? " + String.format("WHERE %s=?", identifier);
			PreparedStatement updateStatement = connection.getConnection().prepareStatement(updateQuery);
			updateStatement.setString(1, updatedStudent.getStudentId());
			updateStatement.setString(2, updatedStudent.getFirstName());
			updateStatement.setString(3, updatedStudent.getMiddleName());
			updateStatement.setString(4, updatedStudent.getFirstSurname());
			updateStatement.setString(5, updatedStudent.getSecondSurname());
			updateStatement.setString(6, updatedStudent.getCedula());
			updateStatement.setString(7, updatedStudent.getEmail());
			updateStatement.setString(8, updatedStudent.getAddress());
			updateStatement.setInt(9, statusPkId);
			updateStatement.setString(10, value);
			int affectedRows = updateStatement.executeUpdate();

			// Close unused resources
			updateStatement.close();
			connection.close();

			if (affectedRows > 0) {
				// If student data was inserted correctly, re-associate telephone numbers, if
				// any
				StudentPhoneNumberDatabaseHelper.unlinkAll(studentPkId);
				if (updatedStudent.getPhoneNumbers() != null) {
					for (PhoneNumber p : updatedStudent.getPhoneNumbers()) {
						int phoneNumberPkId = PhoneNumberDatabaseHelper.resolve(p);
						StudentPhoneNumberDatabaseHelper.link(phoneNumberPkId, studentPkId);
					}
				}
				opResult = true;
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return opResult;
	}

	/**
	 * 
	 * @param identifier
	 *            The name of the column with an unique identifier that we want to
	 *            use to locate the Student.
	 * @param value
	 *            The value stored in the identifier for the particular student we
	 *            want to locate.
	 * @return The primary key value of the student stored under the identifier, or
	 *         -1 if not found.
	 */
	public static int getPkIdByUniqueIdentifier(String identifier, String value) {
		int pkId = -1;

		try {
			DatabaseConnection connection = new DatabaseConnection();

			String idQuery = String.format("SELECT StudentPk_id FROM VW_Student WHERE %s=?", identifier);
			PreparedStatement idStatement = connection.getConnection().prepareStatement(idQuery);
			idStatement.setString(1, value);
			ResultSet idResultSet = idStatement.executeQuery();

			if (idResultSet.next()) {
				pkId = idResultSet.getInt("StudentPk_id");
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return pkId;
	}

}
