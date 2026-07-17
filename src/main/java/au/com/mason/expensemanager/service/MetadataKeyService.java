package au.com.mason.expensemanager.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import au.com.mason.expensemanager.dao.MetadataKeyDao;
import au.com.mason.expensemanager.domain.MetadataKey;
import au.com.mason.expensemanager.domain.MetadataValue;
import au.com.mason.expensemanager.dto.MetadataKeyDto;
import au.com.mason.expensemanager.dto.MetadataKeyWithValuesDto;
import au.com.mason.expensemanager.dto.MetadataValueDto;

@Component
public class MetadataKeyService {

	@Autowired
	private MetadataKeyDao metadataKeyDao;

	@Autowired
	private MetadataValueService metadataValueService;

	public List<MetadataKey> getAll() {
		return metadataKeyDao.getAll();
	}

	public List<MetadataKeyWithValuesDto> getAllWithValues() {
		List<MetadataKeyWithValuesDto> result = new ArrayList<>();
		for (MetadataKey key : metadataKeyDao.getAll()) {
			MetadataKeyWithValuesDto dto = new MetadataKeyWithValuesDto();
			dto.setId(key.getId());
			dto.setName(key.getName());
			for (MetadataValue value : metadataValueService.getAllByKey(key.getId())) {
				MetadataValueDto valueDto = new MetadataValueDto();
				valueDto.setId(value.getId());
				valueDto.setValue(value.getValue());
				MetadataKeyDto keyDto = new MetadataKeyDto();
				keyDto.setId(key.getId());
				keyDto.setName(key.getName());
				valueDto.setMetadataKey(keyDto);
				dto.getValues().add(valueDto);
			}
			result.add(dto);
		}
		return result;
	}

	public MetadataKey create(MetadataKey metadataKey) {
		if (metadataKey.getName() == null) {
			return metadataKeyDao.create(metadataKey);
		}
		MetadataKey existing = metadataKeyDao.findByName(metadataKey.getName());
		if (existing != null) {
			return existing;
		}
		return metadataKeyDao.create(metadataKey);
	}

	public MetadataKey update(MetadataKey metadataKey) {
		MetadataKey existing = metadataKeyDao.getById(metadataKey.getId());
		existing.setName(metadataKey.getName());
		return metadataKeyDao.update(existing);
	}

	public void delete(Long id) {
		metadataValueService.deleteByMetadataKeyId(id);
		metadataKeyDao.deleteById(id);
	}

	public MetadataKey getById(Long id) {
		return metadataKeyDao.getById(id);
	}

}
