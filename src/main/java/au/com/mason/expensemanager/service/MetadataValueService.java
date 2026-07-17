package au.com.mason.expensemanager.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import au.com.mason.expensemanager.dao.MetadataValueDao;
import au.com.mason.expensemanager.domain.MetadataValue;

@Component
public class MetadataValueService {

	@Autowired
	private MetadataValueDao metadataValueDao;

	public List<MetadataValue> getAll() {
		return metadataValueDao.getAll();
	}

	public List<MetadataValue> getAllByKey(Long metadataKeyId) {
		return metadataValueDao.getAllByKey(metadataKeyId);
	}

	public List<MetadataValue> getAllByKeyName(String keyName) {
		return metadataValueDao.getAllByKeyName(keyName);
	}

	public MetadataValue create(MetadataValue metadataValue) {
		if (metadataValue.getMetadataKey() == null || metadataValue.getMetadataKey().getId() == 0) {
			return metadataValueDao.create(metadataValue);
		}
		MetadataValue existing = metadataValueDao.findByKeyAndValue(metadataValue.getMetadataKey().getId(),
			metadataValue.getValue());
		if (existing != null) {
			return existing;
		}
		return metadataValueDao.create(metadataValue);
	}

	public MetadataValue update(MetadataValue metadataValue) {
		MetadataValue existing = metadataValueDao.getById(metadataValue.getId());
		existing.setValue(metadataValue.getValue());
		return metadataValueDao.update(existing);
	}

	public void delete(Long id) {
		metadataValueDao.deleteById(id);
	}

	public void deleteByMetadataKeyId(Long metadataKeyId) {
		metadataValueDao.deleteByMetadataKeyId(metadataKeyId);
	}

	public MetadataValue getById(Long id) {
		return metadataValueDao.getById(id);
	}

}
