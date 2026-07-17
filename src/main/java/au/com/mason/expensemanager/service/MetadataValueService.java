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

	public MetadataValue create(MetadataValue metadataValue) {
		return metadataValueDao.create(metadataValue);
	}

	public void delete(Long id) {
		metadataValueDao.deleteById(id);
	}

	public MetadataValue getById(Long id) {
		return metadataValueDao.getById(id);
	}

}
