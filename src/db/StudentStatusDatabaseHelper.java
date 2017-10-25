package db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import core.entities.Student.Status;

public class StudentStatusDatabaseHelper {
	public static int getStatusPkId(Status status) {
		int pkId = -1;
		
		try {
			DatabaseConnection connection = new DatabaseConnection();

			// First fetch key of the status to apply
			String statusQuery = "SELECT pk_id FROM StudentStatus WHERE status=?";
			PreparedStatement statusStatement = connection.getConnection().prepareStatement(statusQuery);
			statusStatement.setString(1, status.name());
			ResultSet statusResultSet = statusStatement.executeQuery();
			if (statusResultSet.next()) {
				pkId = statusResultSet.getInt("pk_id");
			} else {
				// If no status found, Student object must be malformed
				return pkId;
			}

			// Close unused resources
			statusResultSet.close();
			statusStatement.close();
			connection.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return pkId;
	}
}
