package db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.mysql.jdbc.Statement;

import core.Employee;
import core.PhoneNumber;

public class EmployeeDatabaseHelper {

	/**
	 * 
	 * @return A list of all the Employees in the database or an empty list, if none
	 *         exist.
	 */
	public static List<Employee> getAll() {

		List<Employee> employeeList = new ArrayList<>();

		try {
			DatabaseConnection connection = new DatabaseConnection();

			// First fetch Employee data
			String employeeQuery = "SELECT * FROM VW_Employee";
			PreparedStatement employeeStatement = connection.getConnection().prepareStatement(employeeQuery);
			ResultSet employeeResultSet = employeeStatement.executeQuery();

			while (employeeResultSet.next()) {
				int employeePkId = employeeResultSet.getInt("EmployeePk_id");
				String employeeEntityId = employeeResultSet.getString("uuid");
				String firstName = employeeResultSet.getString("firstName");
				String middleName = employeeResultSet.getString("middleName");
				String firstSurname = employeeResultSet.getString("firstSurname");
				String secondSurname = employeeResultSet.getString("secondSurname");
				String cedula = employeeResultSet.getString("cedula");
				String email = employeeResultSet.getString("email");
				String role = employeeResultSet.getString("role");
				Employee employee = new Employee();
				employee.setEntityId(employeeEntityId);
				employee.setFirstName(firstName);
				employee.setMiddleName(middleName);
				employee.setFirstSurname(firstSurname);
				employee.setSecondSurname(secondSurname);
				employee.setCedula(cedula);
				employee.setEmail(email);
				employee.setRole(role);
				// Then we fetch the associated phone numbers for the current employee
				List<PhoneNumber> phoneNumbers = EmployeePhoneNumberDatabaseHelper.getAll(employeePkId);
				employee.setPhoneNumbers(phoneNumbers);

				// Add to the list
				employeeList.add(employee);
			}
			// Close unused resources
			employeeResultSet.close();
			employeeStatement.close();
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return employeeList;
	}

	/**
	 * 
	 * @param identifier
	 *            The name of the column with an unique identifier that we want to
	 *            use to locate the Employee.
	 * @param value
	 *            The value stored in the identifier for the particular employee we
	 *            want to fetch.
	 * @return An Employee object representing the Employee found under the
	 *         specified identifier, or null if none found.
	 */
	public static Employee getByUniqueIdentifier(String identifier, String value) {

		Employee employee = null;

		try {
			DatabaseConnection connection = new DatabaseConnection();

			// First we fetch Employee data
			String employeeQuery = String.format("SELECT * FROM VW_Employee WHERE %s=?", identifier);
			PreparedStatement employeeStatement = connection.getConnection().prepareStatement(employeeQuery);
			employeeStatement.setString(1, value);
			ResultSet employeeResultSet = employeeStatement.executeQuery();

			/*
			 * Since we're querying via an unique identifier, it's safe to assume the result
			 * set contains either zero or one results.
			 */
			if (employeeResultSet.next()) {
				employee = new Employee();
				int employeePkId = employeeResultSet.getInt("EmployeePk_id");
				String employeeEntityId = employeeResultSet.getString("uuid");
				String firstName = employeeResultSet.getString("firstName");
				String middleName = employeeResultSet.getString("middleName");
				String firstSurname = employeeResultSet.getString("firstSurname");
				String secondSurname = employeeResultSet.getString("secondSurname");
				String cedula = employeeResultSet.getString("cedula");
				String email = employeeResultSet.getString("email");
				String role = employeeResultSet.getString("role");
				employee.setEntityId(employeeEntityId);
				employee.setFirstName(firstName);
				employee.setMiddleName(middleName);
				employee.setFirstSurname(firstSurname);
				employee.setSecondSurname(secondSurname);
				employee.setCedula(cedula);
				employee.setEmail(email);
				employee.setRole(role);

				// Then we fetch the associated phone numbers
				List<PhoneNumber> phoneNumbers = EmployeePhoneNumberDatabaseHelper.getAll(employeePkId);
				employee.setPhoneNumbers(phoneNumbers);
			}

			// Close unused resources
			employeeResultSet.close();
			employeeStatement.close();
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return employee;
	}

	/**
	 * 
	 * Inserts the values contained in the Employee object passed into the database,
	 * creating a new entity to represent it.
	 * 
	 * @param newEmployee
	 *            The Employee to be inserted into the database.
	 * @return A Employee object representing the newly created entity, including its
	 *         associated entity ID, or null if the entity could not be created.
	 */
	public static Employee create(Employee newEmployee) {
		Employee createdEmployee = null;

		try {
			DatabaseConnection connection = new DatabaseConnection();

			// Check if unique identifiers are in use
			if (getByUniqueIdentifier("cedula", newEmployee.getCedula()) != null) {
				return createdEmployee;
			} else if (getByUniqueIdentifier("uuid", newEmployee.getEntityId()) != null) {
				return createdEmployee;
			}

			// First fetch key of the role to apply
			int rolePkId = EmployeeRoleDatabaseHelper.getPkId(newEmployee.getRole());
			if (rolePkId < 0) {
				// If no role found, Employee object must be malformed
				return createdEmployee;
			}

			// First insert Employee data
			String employeeQuery = "INSERT INTO Employee "
					+ "(firstName, middleName, firstSurname, secondSurname, cedula, email, RolePk_id) "
					+ "VALUES (?, ?, ?, ?, ?, ?, ?)";
			PreparedStatement employeeStatement = connection.getConnection().prepareStatement(employeeQuery,
					Statement.RETURN_GENERATED_KEYS);
			employeeStatement.setString(1, newEmployee.getFirstName());
			employeeStatement.setString(2, newEmployee.getMiddleName());
			employeeStatement.setString(3, newEmployee.getFirstSurname());
			employeeStatement.setString(4, newEmployee.getSecondSurname());
			employeeStatement.setString(5, newEmployee.getCedula());
			employeeStatement.setString(6, newEmployee.getEmail());
			employeeStatement.setInt(7, rolePkId);
			employeeStatement.executeUpdate();
			ResultSet employeeResultSet = employeeStatement.getGeneratedKeys();

			// Check for generated keys, if any
			int employeePkId;
			if (employeeResultSet.next()) {
				employeePkId = employeeResultSet.getInt(1);
			} else {
				// An unknown error occurred while inserting the employee
				return createdEmployee;
			}

			// Close unused resources
			employeeResultSet.close();
			employeeStatement.close();
			connection.close();

			// If employee data was inserted correctly, associate telephone numbers, if any
			if (newEmployee.getPhoneNumbers() != null) {
				for (PhoneNumber p : newEmployee.getPhoneNumbers()) {
					int phoneNumberPkId = PhoneNumberDatabaseHelper.resolve(p);
					EmployeePhoneNumberDatabaseHelper.link(phoneNumberPkId, employeePkId);
				}
			}

			// Operation completed if reached this line
			createdEmployee = getByUniqueIdentifier("EmployeePk_id", String.valueOf(employeePkId));
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return createdEmployee;
	}

	/**
	 * 
	 * @param identifier
	 *            The name of the column with an unique identifier that we want to
	 *            use to locate the Employee.
	 * @param value
	 *            The value stored in the identifier for the particular employee we
	 *            want to delete.
	 * @return True if Employee was successfully deleted, false otherwise.
	 */
	public static boolean deleteByUniqueIdentifier(String identifier, String value) {
		boolean opResult = false;

		try {
			DatabaseConnection connection = new DatabaseConnection();

			// Apply updates
			String deleteQuery = String.format("UPDATE Employee SET MD_isDeleted=TRUE WHERE %s=?", identifier);
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
	 *            use to locate the Employee.
	 * @param value
	 *            The value stored in the identifier for the particular employee we
	 *            want to update.
	 * @param updatedEmployee
	 *            An Employee object containing the new data for the entity.
	 * @return true if update was successful, false otherwise.
	 */
	public static boolean updateByUniqueIdentifier(String identifier, String value, Employee updatedEmployee) {
		boolean opResult = false;

		try {
			DatabaseConnection connection = new DatabaseConnection();

			// Fetch employee pk_id
			int employeePkId = getPkIdByUniqueIdentifier(identifier, value);
			if (employeePkId < 0) {
				// Employee not found
				return opResult;
			}

			// Fetch employee role pk_id
			int rolePkId = EmployeeRoleDatabaseHelper.getPkId(updatedEmployee.getRole());
			if (rolePkId < 0) {
				// Employee object malformed
				return opResult;
			}

			String updateQuery = "UPDATE Employee SET " + "firstName=?, middleName=?, firstSurname=?, secondSurname=?, "
					+ "cedula=?, email=?, RolePk_id=? " + String.format("WHERE %s=?", identifier);
			PreparedStatement updateStatement = connection.getConnection().prepareStatement(updateQuery);
			updateStatement.setString(1, updatedEmployee.getFirstName());
			updateStatement.setString(2, updatedEmployee.getMiddleName());
			updateStatement.setString(3, updatedEmployee.getFirstSurname());
			updateStatement.setString(4, updatedEmployee.getSecondSurname());
			updateStatement.setString(5, updatedEmployee.getCedula());
			updateStatement.setString(6, updatedEmployee.getEmail());
			updateStatement.setInt(7, rolePkId);
			updateStatement.setString(8, value);
			int affectedRows = updateStatement.executeUpdate();

			// Close unused resources
			updateStatement.close();
			connection.close();

			if (affectedRows > 0) {
				/*
				 * If employee data was inserted correctly, re-associate telephone numbers, if
				 * any
				 */
				EmployeePhoneNumberDatabaseHelper.unlinkAll(employeePkId);
				if (updatedEmployee.getPhoneNumbers() != null) {
					for (PhoneNumber p : updatedEmployee.getPhoneNumbers()) {
						int phoneNumberPkId = PhoneNumberDatabaseHelper.resolve(p);
						EmployeePhoneNumberDatabaseHelper.link(phoneNumberPkId, employeePkId);
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
	 *            use to locate the Employee.
	 * @param value
	 *            The value stored in the identifier for the particular employee we
	 *            want to locate.
	 * @return The primary key value of the employee stored under the identifier, or
	 *         -1 if not found.
	 */
	public static int getPkIdByUniqueIdentifier(String identifier, String value) {
		int pkId = -1;

		try {
			DatabaseConnection connection = new DatabaseConnection();

			String idQuery = String.format("SELECT EmployeePk_id FROM VW_Employee WHERE %s=?", identifier);
			PreparedStatement idStatement = connection.getConnection().prepareStatement(idQuery);
			idStatement.setString(1, value);
			ResultSet idResultSet = idStatement.executeQuery();

			if (idResultSet.next()) {
				pkId = idResultSet.getInt("EmployeePk_id");
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return pkId;
	}
}
