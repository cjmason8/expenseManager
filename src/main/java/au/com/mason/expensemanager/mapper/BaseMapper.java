package au.com.mason.expensemanager.mapper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public interface BaseMapper<T, V> {

	T dtoToEntity(V dto);

	V entityToDto(T entity);

	default List<V> entityListToDto(List<T> entities) {
		if (entities == null) {
			return null;
		}
		return entities.stream().map(this::entityToDto).collect(Collectors.toCollection(ArrayList::new));
	}

}
