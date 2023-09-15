package au.com.mason.expensemanager.dao;

import au.com.mason.expensemanager.controller.DocumentController;
import au.com.mason.expensemanager.domain.Document;
import au.com.mason.expensemanager.domain.Statics;
import au.com.mason.expensemanager.dto.SearchParamsDto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.apache.commons.lang3.StringUtils;

@Repository
@Transactional
public class DocumentDao extends MetaDataDao<Document> {

	private static Logger LOGGER = LogManager.getLogger(DocumentDao.class);
	
	public DocumentDao(@Qualifier("entityManagerFactory") EntityManager entityManager) {
		super(Document.class, entityManager);
	}

	public List<Document> getAll(String folderPath, boolean includeArchived) {
		String queryName = includeArchived ? Document.GET_ALL_BY_FOLDER_PATH_INCLUDE_ARCHIVED : Document.GET_ALL_BY_FOLDER_PATH;
		LOGGER.info("queryName: " + queryName);
		Query query = entityManager.createNamedQuery(queryName, Document.class);
		query.setParameter("folderPath", folderPath);
		return query.getResultList();
	}
	
	public Document getFolder(String folderPath, String folderName) {
		Query query = entityManager.createNamedQuery(Document.GET_ALL_BY_FOLDER_PATH_AND_FILENAME, Document.class);
		query.setParameter("folderPath", folderPath);
		query.setParameter("fileName", folderName);
		return (Document) query.getSingleResult();
	}	
	
	public void updateDirectoryPaths(String oldPath, String newPath) {
		String sql = "UPDATE documents set folderPath = replace(folderPath, '" + oldPath + "', '" + newPath + "')";
		entityManager.createNativeQuery(sql).executeUpdate();
	}
	
	public void deleteDirectory(String folderPath) {
		String sql = "DELETE from documents where folderPath LIKE '" + folderPath + "%'";
		entityManager.createNativeQuery(sql).executeUpdate();
	}
	
	public List<Document> findDocuments(SearchParamsDto searchParamsDto) {
		if (StringUtils.isEmpty(searchParamsDto.getKeyWords()) && StringUtils.isEmpty(searchParamsDto.getMetaDataChunk())) {
			return new ArrayList<Document>();
		}

		String sql = "SELECT * FROM documents ";
		if (!StringUtils.isEmpty(searchParamsDto.getKeyWords())) {
			sql += "WHERE lower(fileName) LIKE '%" + searchParamsDto.getKeyWords().toLowerCase() + "%' ";
		}
		Query query = entityManager.createNativeQuery(sql, Document.class);
		List<Document> results = query.getResultList();
		
		if (!StringUtils.isEmpty(searchParamsDto.getMetaDataChunk())) {
			return filterByMetadata(searchParamsDto, results);
		}

		return results.stream().limit(Statics.MAX_RESULTS.getIntValue()).collect(Collectors.toList());
		
	}

}
