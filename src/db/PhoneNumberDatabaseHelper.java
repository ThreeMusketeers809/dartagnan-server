package db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import core.entities.PhoneNumber;

public class PhoneNumberDatabaseHelper {
	/**
	 * 
	 * Queries a PhoneNumber object in the database, inserting it if it doesn't
	 * exist.
	 * 
	 * @param phoneNumber
	 *            the PhoneNumber to query
	 * @return the database PRIMARY KEY ID of the PhoneNumber queried, or -1 if
	 *         query failed
	 */
	public static int resolve(PhoneNumber phoneNumber) {
		int pkId = -1;

		try {
			DatabaseConnection connection = new DatabaseConnection();

			// First fetch key of the phoneType
			String phoneTypeQuery = "SELECT pk_id FROM PhoneType WHERE phoneType=?";
			PreparedStatement phoneTypeStatement = connection.getConnection().prepareStatement(phoneTypeQuery);
			phoneTypeStatement.setString(1, phoneNumber.getType().name());
			ResultSet phoneTypeResultSet = phoneTypeStatement.executeQuery();
			int phoneTypePkId;
			if (phoneTypeResultSet.next()) {
				phoneTypePkId = phoneTypeResultSet.getInt("pk_id");
			} else {
				// If no status found, PhoneNumber object must be malformed
				return pkId;
			}

			// Close unused resources
			phoneTypeResultSet.close();
			phoneTypeStatement.close();

			// Search if phone number exists
			String phoneNumberQuery = "SELECT pk_id FROM PhoneNumber WHERE phoneNumber=? AND PhoneTypePk_id=?";
			PreparedStatement phoneNumberStatement = connection.getConnection().prepareStatement(phoneNumberQuery);
			phoneNumberStatement.setString(1, phoneNumber.getPhoneNumber());
			phoneNumberStatement.setInt(2, phoneTypePkId);
			ResultSet phoneNumberResultSet = phoneNumberStatement.executeQuery();

			/*
			 * phoneNumber and PhoneTypePk_id for an unique pair, so only one result
			 * expected
			 */
			if (phoneNumberResultSet.next()) {
				pkId = phoneNumberResultSet.getInt("pk_id");
			} else {
				// Close unused resources
				phoneNumberResultSet.close();
				phoneNumberStatement.close();

				// Insert phone number
				phoneNumberQuery = "INSERT INTO PhoneNumber (phoneNumber, PhoneTypePk_id) VALUES (?, ?)";
				phoneNumberStatement = connection.getConnection().prepareStatement(phoneNumberQuery,
						Statement.RETURN_GENERATED_KEYS);
				phoneNumberStatement.setString(1, phoneNumber.getPhoneNumber());
				phoneNumberStatement.setInt(2, phoneTypePkId);
				phoneNumberStatement.executeUpdate();
				phoneNumberResultSet = phoneNumberStatement.getGeneratedKeys();

				if (phoneNumberResultSet.next()) {
					pkId = phoneNumberResultSet.getInt(1);
				}
				// Close unused resources
				phoneNumberResultSet.close();
				phoneNumberStatement.close();
			}
			// Close unused resources
			connection.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return pkId;
	}
}
