package au.com.mason.expensemanager.service;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import au.com.mason.expensemanager.dao.EntityEntryDao;
import au.com.mason.expensemanager.domain.Document;
import au.com.mason.expensemanager.domain.EntityEntry;
import au.com.mason.expensemanager.domain.EntityMetadataType;
import au.com.mason.expensemanager.domain.EntityType;

@Component
public class EntityEntryService {

	@Autowired
	private EntityEntryDao entityEntryDao;

	@Autowired
	private DocumentService documentService;

	@Autowired
	private EntityMetadataService entityMetadataService;

	public EntityEntry createEntityEntry(EntityEntry entityEntry) throws Exception {
		resolveDocument(entityEntry);
		EntityEntry created = entityEntryDao.create(entityEntry);
		persistMetadata(created);
		hydrateEntityEntry(created);
		return created;
	}

	public EntityEntry updateEntityEntry(EntityEntry entityEntry) throws Exception {
		resolveDocument(entityEntry);
		EntityEntry updated = entityEntryDao.update(entityEntry);
		persistMetadata(updated);
		hydrateEntityEntry(updated);
		return updated;
	}

	public void deleteEntityEntry(Long id) {
		entityMetadataService.deleteForEntity(EntityMetadataType.ENTITY, String.valueOf(id));
		entityEntryDao.deleteById(id);
	}

	public EntityEntry getById(Long id) throws Exception {
		EntityEntry entityEntry = entityEntryDao.getById(id);
		hydrateEntityEntry(entityEntry);
		return entityEntry;
	}

	public List<EntityEntry> getAll(boolean includeArchived) throws Exception {
		List<EntityEntry> results = entityEntryDao.getAll(includeArchived);
		hydrateEntityEntries(results);
		return results;
	}

	public List<EntityEntry> getAllByType(EntityType type, boolean includeArchived) throws Exception {
		List<EntityEntry> results = entityEntryDao.getAllByType(type, includeArchived);
		hydrateEntityEntries(results);
		return results;
	}

	private void resolveDocument(EntityEntry entityEntry) throws Exception {
		if (entityEntry.getDocument() != null && isDocumentAttached(entityEntry.getDocument())) {
			entityEntry.setDocument(documentService.getById(entityEntry.getDocument().getId()));
		} else {
			entityEntry.setDocument(null);
		}
	}

	private void hydrateEntityEntry(EntityEntry entityEntry) {
		if (entityEntry == null) {
			return;
		}
		hydrateEntityEntries(List.of(entityEntry));
	}

	private void hydrateEntityEntries(List<EntityEntry> entityEntries) {
		entityMetadataService.hydrateList(EntityMetadataType.ENTITY, entityEntries, e -> String.valueOf(e.getId()),
			(entity, entityMetadata, objectMap, stringMap) -> {
				entity.setEntityMetadata(entityMetadata);
				entity.setMetaData(stringMap);
			});
		for (EntityEntry entityEntry : entityEntries) {
			if (entityEntry.getDocument() != null) {
				documentService.hydrateDocument(entityEntry.getDocument());
			}
		}
	}

	private void persistMetadata(EntityEntry entityEntry) {
		if (entityEntry == null || entityEntry.getId() == 0) {
			return;
		}
		entityMetadataService.replace(EntityMetadataType.ENTITY, String.valueOf(entityEntry.getId()),
			entityEntry.getMetaData());
	}

	private static boolean isDocumentAttached(Document doc) {
		return doc != null && (doc.getId() != null || StringUtils.isNotBlank(doc.getFileName()));
	}

}
