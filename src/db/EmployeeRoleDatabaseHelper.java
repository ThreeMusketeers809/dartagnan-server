package db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import core.Employee.Role;

public class EmployeeRoleDatabaseHelper {
	public static int getPkId(Role role) {
		int pkId = -1;
		
		try {
			DatabaseConnection connection = new DatabaseConnection();

			// First fetch key of the role to apply
			String roleQuery = "SELECT pk_id FROM EmployeeRole WHERE name=?";
			PreparedStatement roleStatement = connection.getConnection().prepareStatement(roleQuery);
			roleStatement.setString(1, role.name());
			ResultSet roleResultSet = roleStatement.executeQuery();
			if (roleResultSet.next()) {
				pkId = roleResultSet.getInt("pk_id");
			} else {
				// If no role found, Employee object must be malformed
				return pkId;
			}

			// Close unused resources
			roleResultSet.close();
			roleStatement.close();
			connection.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return pkId;
	}
}
