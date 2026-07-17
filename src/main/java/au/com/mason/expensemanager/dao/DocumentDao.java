package au.com.mason.expensemanager.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import au.com.mason.expensemanager.domain.Document;
import au.com.mason.expensemanager.domain.EntityMetadataType;
import au.com.mason.expensemanager.domain.Statics;
import au.com.mason.expensemanager.dto.SearchParamsDto;
import au.com.mason.expensemanager.service.EntityMetadataService;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@Repository
@Transactional
public class DocumentDao extends BaseDao<Document> {

	private final Gson gson = new GsonBuilder().serializeNulls().create();

	@Autowired
	private EntityMetadataService entityMetadataService;

	public DocumentDao(@Qualifier("entityManagerFactory") EntityManager entityManager) {
		super(Document.class, entityManager);
	}

	public List<Document> getAll(String folderPath, boolean includeArchived) {
		String queryName = includeArchived
			? Document.GET_ALL_BY_FOLDER_PATH_INCLUDE_ARCHIVED
			: Document.GET_ALL_BY_FOLDER_PATH;
		Query query = entityManager.createNamedQuery(queryName, Document.class);
		query.setParameter("folderPath", folderPath);
		return query.getResultList();
	}

	public Document getFolder(String folderPath, String folderName) {
		Query query = entityManager.createNamedQuery(Document.GET_ALL_BY_FOLDER_PATH_AND_FILENAME, Document.class);
		query.setParameter("folderPath", folderPath);
		query.setParameter("fileName", folderName);
		@SuppressWarnings("unchecked")
		List<Document> results = query.getResultList();
		return results.isEmpty() ? null : results.get(0);
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
		if (StringUtils.isEmpty(searchParamsDto.getKeyWords())
			&& StringUtils.isEmpty(searchParamsDto.getMetaDataChunk())) {
			return new ArrayList<Document>();
		}

		String jpql = "FROM Document d ";
		if (!StringUtils.isEmpty(searchParamsDto.getKeyWords())) {
			jpql += "WHERE lower(d.fileName) LIKE lower(:keyWords) ";
		}
		TypedQuery<Document> tq = entityManager.createQuery(jpql, Document.class);
		if (!StringUtils.isEmpty(searchParamsDto.getKeyWords())) {
			tq.setParameter("keyWords", "%" + searchParamsDto.getKeyWords() + "%");
		}
		List<Document> results = tq.getResultList();
		hydrateDocuments(results);

		if (!StringUtils.isEmpty(searchParamsDto.getMetaDataChunk())) {
			return filterByMetadata(searchParamsDto.getMetaDataChunk(), results);
		}

		return results.stream().limit(Statics.MAX_RESULTS.getIntValue()).collect(Collectors.toList());
	}

	private void hydrateDocuments(List<Document> documents) {
		entityMetadataService.hydrateList(EntityMetadataType.DOCUMENT, documents,
			document -> document.getId() == null ? null : document.getId().toString(),
			(entity, entityMetadata, objectMap, stringMap) -> {
				entity.setEntityMetadata(entityMetadata);
				entity.setMetaData(objectMap);
			});
	}

	@SuppressWarnings("unchecked")
	private List<Document> filterByMetadata(String metaDataChunk, List<Document> results) {
		Map<String, Object> criteria = gson.fromJson(metaDataChunk, Map.class);
		if (criteria == null || criteria.isEmpty()) {
			return results;
		}
		List<Document> filtered = new ArrayList<>();
		for (Document document : results) {
			Map<String, Object> metaData = document.getMetaData();
			if (metaData == null) {
				continue;
			}
			boolean matches = true;
			for (Map.Entry<String, Object> entry : criteria.entrySet()) {
				Object stored = metaData.get(entry.getKey());
				if (!valueMatches(stored, entry.getValue())) {
					matches = false;
					break;
				}
			}
			if (matches) {
				filtered.add(document);
			}
		}
		return filtered;
	}

	private boolean valueMatches(Object stored, Object criteria) {
		if (stored == null) {
			return false;
		}
		if (criteria instanceof Collection<?> criteriaList) {
			for (Object item : criteriaList) {
				if (valueEqualsIgnoreCase(stored, item)) {
					return true;
				}
			}
			return false;
		}
		return valueEqualsIgnoreCase(stored, criteria);
	}

	private boolean valueEqualsIgnoreCase(Object stored, Object criteria) {
		if (stored instanceof Collection<?> storedList) {
			return storedList.stream().anyMatch(v -> Objects.equals(toLower(v), toLower(criteria)));
		}
		return Objects.equals(toLower(stored), toLower(criteria));
	}

	private String toLower(Object val) {
		return val == null ? null : String.valueOf(val).toLowerCase();
	}

}
