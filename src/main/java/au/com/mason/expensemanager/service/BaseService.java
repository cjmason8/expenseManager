package au.com.mason.expensemanager.service;

import java.util.Optional;
import javax.persistence.EntityManager;
import org.springframework.data.repository.CrudRepository;

public class BaseService<T> {

    protected final Class<T> typeParameterClass;

    protected BaseService(Class<T> typeParameterClass) {
        this.typeParameterClass = typeParameterClass;
    }

    public T findById(CrudRepository<T, Long> crudRepository, Long id) {
        Optional<T> item = crudRepository.findById(id);

        if (item.isEmpty()) {
            throw new RuntimeException(String.format("Item of type %s with id %s does not exist", typeParameterClass, id));
        }

        return item.get();
    }
}
