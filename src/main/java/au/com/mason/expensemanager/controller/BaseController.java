package au.com.mason.expensemanager.controller;

import java.util.List;
import java.util.stream.Collectors;

public abstract class BaseController<V, F> {

	public List<V> convertList(List<F> items) throws Exception {
		return items.stream()
		          .map(item -> convertToDtoWrapper(item))
		          .collect(Collectors.toList());
	}
	
	private V convertToDtoWrapper(F item) throws RuntimeException {
		try {
			return convertToDto(item);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public abstract V convertToDto(F item) throws Exception;
	
	public abstract F convertToEntity(V item) throws Exception;
}
