package au.com.mason.expensemanager.controller;

import au.com.mason.expensemanager.domain.RefData;
import au.com.mason.expensemanager.dto.RefDataDto;
import au.com.mason.expensemanager.mapper.BaseMapper;
import java.util.List;
import java.util.stream.Collectors;

public abstract class BaseController<V, F> {

	private BaseMapper<V, F> baseMapper;

	public BaseController(BaseMapper<V, F> baseMapper) {
		this.baseMapper = baseMapper;
	}

	public List<F> convertList(List<V> items) throws Exception {
		return items.stream()
		          .map(item -> convertToDtoWrapper(item))
		          .collect(Collectors.toList());
	}
	
	private F convertToDtoWrapper(V item) throws RuntimeException {
		try {
			return convertToDto(item);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public F convertToDto(V item) {
		return baseMapper.entityToDto(item);
	}
	
	public V convertToEntity(F item) {
		return baseMapper.dtoToEntity(item);
	}
}
