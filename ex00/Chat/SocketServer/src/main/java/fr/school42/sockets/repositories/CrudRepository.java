package fr.school42.sockets.repositories;

import java.util.Optional;

public interface CrudRepository<T> {
	Optional<T> findById(Long id);
	void save(T entity);
	void update(T entity);
	void delete(Long id);
}
