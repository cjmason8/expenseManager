package au.com.mason.expensemanager.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import au.com.mason.expensemanager.dao.MetadataKeyDao;
import au.com.mason.expensemanager.domain.MetadataKey;

@Component
public class MetadataKeyService {

	@Autowired
	private MetadataKeyDao metadataKeyDao;

	public List<MetadataKey> getAll() {
		return metadataKeyDao.getAll();
	}

	public MetadataKey create(MetadataKey metadataKey) {
		return metadataKeyDao.create(metadataKey);
	}

	public void delete(Long id) {
		metadataKeyDao.deleteById(id);
	}

	public MetadataKey getById(Long id) {
		return metadataKeyDao.getById(id);
	}

}
