package au.com.mason.expensemanager.controller;

import java.util.List;

import au.com.mason.expensemanager.mapper.BaseMapper;

public abstract class BaseController<V, F> {

	private final BaseMapper<V, F> baseMapper;

	public BaseController(BaseMapper<V, F> baseMapper) {
		this.baseMapper = baseMapper;
	}

	public List<F> convertList(List<V> items) {
		return baseMapper.entityListToDto(items);
	}

	public F convertToDto(V item) {
		return baseMapper.entityToDto(item);
	}

	public V convertToEntity(F item) {
		return baseMapper.dtoToEntity(item);
	}

}
