package au.com.mason.expensemanager.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import java.util.List;

@Transactional
public class BaseDao<T> {

	private Class<T> className;
	protected EntityManager entityManager;
	
	public BaseDao(Class<T> className, EntityManager entityManager) {
		this.className = className;
		this.entityManager = entityManager;
	}

	public T create(T item) {
		entityManager.persist(item);

		return item;
	}

	public void delete(T item) {
		if (entityManager.contains(item))
			entityManager.remove(item);
		else
			entityManager.remove(entityManager.merge(item));
		return;
	}

	public void deleteById(Long id) {
		T item = entityManager.find(className, id);
		entityManager.remove(item);
	}

	public T getById(long id) {
		return entityManager.find(className, id);
	}

	public T update(T item) {
		return entityManager.merge(item);
	}


	public List<T> findAll() {
		Query query = entityManager.createQuery("FROM " + className.getName());
		return query.getResultList();
	}

}
