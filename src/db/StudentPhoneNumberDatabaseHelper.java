package db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import core.entities.PhoneNumber;

public class StudentPhoneNumberDatabaseHelper {

	/**
	 * 
	 * Links a Phone Number entity and a Student entity.
	 * 
	 * @param phoneNumberPkId
	 *            The primary key of the Phone Number entity.
	 * @param studentPkId
	 *            The primary key of the Student entity.
	 * @return true if link was successful, false otherwise.
	 */
	public static boolean link(int phoneNumberPkId, int studentPkId) {
		boolean result = false;

		try {
			DatabaseConnection connection = new DatabaseConnection();

			// First fetch key of the phoneType
			String linkQuery = "INSERT INTO StudentHasPhoneNumber (StudentPk_id, PhoneNumberPk_id) VALUES (?, ?)";
			PreparedStatement linkStatement = connection.getConnection().prepareStatement(linkQuery,
					Statement.RETURN_GENERATED_KEYS);
			linkStatement.setInt(1, studentPkId);
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
	 * Unlinks a Phone Number entity from a Student entity.
	 * 
	 * @param phoneNumberPkId
	 *            The primary key of the Phone Number entity.
	 * @param studentPkId
	 *            The primary key of the Student entity.
	 * @return true if unlink was successful, false otherwise.
	 */
	public static boolean unlink(int phoneNumberPkId, int studentPkId) {
		boolean result = false;

		try {
			DatabaseConnection connection = new DatabaseConnection();

			// First fetch key of the phoneType
			String unlinkQuery = "DELETE FROM StudentHasPhoneNumber WHERE StudentPk_id=? AND PhoneNumberPk_id=?";
			PreparedStatement unlinkStatement = connection.getConnection().prepareStatement(unlinkQuery);
			unlinkStatement.setInt(1, studentPkId);
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
	 * Unlinks all Phone Number entities associated to a Student entity.
	 * 
	 * @param studentPkId
	 *            The primary key of the Student entity.
	 * @return true if unlinks were successful, false otherwise.
	 */
	public static boolean unlinkAll(int studentPkId) {
		boolean result = false;

		try {
			DatabaseConnection connection = new DatabaseConnection();

			// First fetch key of the phoneType
			String unlinkQuery = "DELETE FROM StudentHasPhoneNumber WHERE StudentPk_id=?";
			PreparedStatement unlinkStatement = connection.getConnection().prepareStatement(unlinkQuery);
			unlinkStatement.setInt(1, studentPkId);
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
	 * @param studentPkId
	 *            The primary key of the Student entity.
	 * @return A List of all Phone Number entities associated to the Student entity,
	 *         or an empty List if none.
	 */
	public static List<PhoneNumber> getAll(int studentPkId) {

		List<PhoneNumber> phoneNumbers = new ArrayList<>();

		try {
			DatabaseConnection connection = new DatabaseConnection();

			String phoneNumberQuery = "SELECT VW_PhoneNumber.PhoneNumberPk_id, VW_PhoneNumber.uuid, VW_PhoneNumber.phoneNumber, VW_PhoneNumber.phoneType FROM VW_PhoneNumber LEFT JOIN StudentHasPhoneNumber ON VW_PhoneNumber.PhoneNumberPk_id=StudentHasPhoneNumber.PhoneNumberPk_id WHERE StudentHasPhoneNumber.StudentPk_id=?";
			PreparedStatement phoneNumberStatement = connection.getConnection().prepareStatement(phoneNumberQuery);
			phoneNumberStatement.setInt(1, studentPkId);
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
