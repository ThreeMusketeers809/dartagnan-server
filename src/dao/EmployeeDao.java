package dao;

import java.util.List;

import core.entities.Employee;
import db.EmployeeDatabaseHelper;

public class EmployeeDao extends AbstractDao<Employee> {

	@Override
	public List<Employee> getAll() {
		return EmployeeDatabaseHelper.getAll();
	}

	@Override
	public Employee create(Employee entity) {
		return EmployeeDatabaseHelper.create(entity);
	}

	@Override
	public boolean delete(String entityId) {
		return EmployeeDatabaseHelper.deleteByUniqueIdentifier("uuid", entityId);
	}

	@Override
	public Employee get(String entityId) {
		return EmployeeDatabaseHelper.getByUniqueIdentifier("uuid", entityId);
	}

	@Override
	public boolean update(String entityId, Employee entity) {
		return EmployeeDatabaseHelper.updateByUniqueIdentifier("uuid", entityId, entity);
	}

	public Employee getByCedula(String cedula) {
		return EmployeeDatabaseHelper.getByUniqueIdentifier("cedula", cedula);
	}
}