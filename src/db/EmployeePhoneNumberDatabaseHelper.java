package db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import core.PhoneNumber;

public class EmployeePhoneNumberDatabaseHelper {

	/**
	 * 
	 * Links a Phone Number entity and an Employee entity.
	 * 
	 * @param phoneNumberPkId
	 *            The primary key of the Phone Number entity.
	 * @param employeePkId
	 *            The primary key of the Employee entity.
	 * @return true if link was successful, false otherwise.
	 */
	public static boolean link(int phoneNumberPkId, int employeePkId) {
		boolean result = false;

		try {
			DatabaseConnection connection = new DatabaseConnection();

			String linkQuery = "INSERT INTO EmployeeHasPhoneNumber (EmployeePk_id, PhoneNumberPk_id) VALUES (?, ?)";
			PreparedStatement linkStatement = connection.getConnection().prepareStatement(linkQuery,
					Statement.RETURN_GENERATED_KEYS);
			linkStatement.setInt(1, employeePkId);
			linkStatement.setInt(2, phoneNumberPkId);
			int affectedRows = linkStatement.executeUpdate();

			// Close unused resources
			linkStatement.close();
			connection.close();

			if (affectedRows > 0) {
				// Values were inserted
				result = true;
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return result;
	}

	/**
	 * 
	 * Unlinks a Phone Number entity from an Employee entity.
	 * 
	 * @param phoneNumberPkId
	 *            The primary key of the Phone Number entity.
	 * @param employeePkId
	 *            The primary key of the Employee entity.
	 * @return true if unlink was successful, false otherwise.
	 */
	public static boolean unlink(int phoneNumberPkId, int employeePkId) {
		boolean result = false;

		try {
			DatabaseConnection connection = new DatabaseConnection();

			String unlinkQuery = "DELETE FROM EmployeeHasPhoneNumber WHERE EmployeePk_id=? AND PhoneNumberPk_id=?";
			PreparedStatement unlinkStatement = connection.getConnection().prepareStatement(unlinkQuery);
			unlinkStatement.setInt(1, employeePkId);
			unlinkStatement.setInt(2, phoneNumberPkId);
			int affectedRows = unlinkStatement.executeUpdate();

			if (affectedRows > 0) {
				// Values were updated
				result = true;
			}

			// Close unused resources
			unlinkStatement.close();
			connection.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return result;
	}

	/**
	 * 
	 * Unlinks all Phone Number entities associated to an Employee entity.
	 * 
	 * @param employeePkId
	 *            The primary key of the Employee entity.
	 * @return true if unlinks were successful, false otherwise.
	 */
	public static boolean unlinkAll(int employeePkId) {
		boolean result = false;

		try {
			DatabaseConnection connection = new DatabaseConnection();

			// First fetch key of the phoneType
			String unlinkQuery = "DELETE FROM EmployeeHasPhoneNumber WHERE EmployeePk_id=?";
			PreparedStatement unlinkStatement = connection.getConnection().prepareStatement(unlinkQuery);
			unlinkStatement.setInt(1, employeePkId);
			int affectedRows = unlinkStatement.executeUpdate();

			if (affectedRows > 0) {
				// Values were updated
				result = true;
			}

			// Close unused resources
			unlinkStatement.close();
			connection.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return result;
	}

	/**
	 * @param employeePkId
	 *            The primary key of the Employee entity.
	 * @return A List of all Phone Number entities associated to the Employee entity,
	 *         or an empty List if none.
	 */
	public static List<PhoneNumber> getAll(int employeePkId) {

		List<PhoneNumber> phoneNumbers = new ArrayList<>();

		try {
			DatabaseConnection connection = new DatabaseConnection();

			String phoneNumberQuery = "SELECT VW_PhoneNumber.PhoneNumberPk_id, VW_PhoneNumber.uuid, VW_PhoneNumber.phoneNumber, VW_PhoneNumber.phoneType FROM VW_PhoneNumber LEFT JOIN EmployeeHasPhoneNumber ON VW_PhoneNumber.PhoneNumberPk_id=EmployeeHasPhoneNumber.PhoneNumberPk_id WHERE EmployeeHasPhoneNumber.EmployeePk_id=?";
			PreparedStatement phoneNumberStatement = connection.getConnection().prepareStatement(phoneNumberQuery);
			phoneNumberStatement.setInt(1, employeePkId);
			ResultSet phoneNumberResultSet = phoneNumberStatement.executeQuery();

			while (phoneNumberResultSet.next()) {
				String phoneNumberEntityId = phoneNumberResultSet.getString("uuid");
				String number = phoneNumberResultSet.getString("phoneNumber");
				String type = phoneNumberResultSet.getString("phoneType");

				PhoneNumber phoneNumber = new PhoneNumber();
				phoneNumber.setEntityId(phoneNumberEntityId);
				phoneNumber.setPhoneNumber(number);
				phoneNumber.setType(type);
				phoneNumbers.add(phoneNumber);
			}

			// Close unused resources
			phoneNumberResultSet.close();
			phoneNumberStatement.close();
			connection.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return phoneNumbers;
	}
}
