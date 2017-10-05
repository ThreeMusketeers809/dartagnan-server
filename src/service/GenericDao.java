package service;

import java.util.List;

public abstract class GenericDao<T> {
	public abstract List<T> getAll();

	public abstract T create(T entity);

	public abstract boolean delete(String entityId);

	public abstract T get(String entityId);

	public abstract boolean update(String entityId, T entity);
}
