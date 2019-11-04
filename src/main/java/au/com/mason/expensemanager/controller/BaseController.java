package au.com.mason.expensemanager.controller;

import java.util.List;
import java.util.stream.Collectors;

public abstract class BaseController<V, F> {

	public List<V> convertList(List<F> items) {
		return items.stream()
		          .map(item -> convertToDto(item))
		          .collect(Collectors.toList());
	}
	
	public abstract V convertToDto(F item);
	
	public abstract F convertToEntity(V item);
}
