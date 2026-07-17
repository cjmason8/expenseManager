package au.com.mason.expensemanager.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import au.com.mason.expensemanager.dao.EntityMetadataDao;
import au.com.mason.expensemanager.dao.MetadataKeyDao;
import au.com.mason.expensemanager.dao.MetadataValueDao;
import au.com.mason.expensemanager.domain.EntityMetadata;
import au.com.mason.expensemanager.domain.EntityMetadataType;
import au.com.mason.expensemanager.domain.MetadataKey;
import au.com.mason.expensemanager.domain.MetadataValue;
import au.com.mason.expensemanager.util.EntityMetadataMaps;

@Component
public class EntityMetadataService {

	@Autowired
	private EntityMetadataDao entityMetadataDao;

	@Autowired
	private MetadataKeyDao metadataKeyDao;

	@Autowired
	private MetadataValueDao metadataValueDao;

	public List<EntityMetadata> getByEntity(EntityMetadataType type, String entityId) {
		return entityMetadataDao.getByEntity(type, entityId);
	}

	public <T> void hydrate(EntityMetadataType type, T entity, Function<T, String> entityIdFn,
		MetaDataSetter<T> setter) {
		if (entity == null) {
			return;
		}
		hydrateList(type, List.of(entity), entityIdFn, setter);
	}

	public <T> void hydrateList(EntityMetadataType type, List<T> entities, Function<T, String> entityIdFn,
		MetaDataSetter<T> setter) {
		if (entities == null || entities.isEmpty()) {
			return;
		}
		List<String> entityIds = entities.stream().map(entityIdFn).filter(Objects::nonNull).distinct().toList();
		Map<String, List<EntityMetadata>> byEntityId = entityMetadataDao.getByEntities(type, entityIds).stream()
			.collect(Collectors.groupingBy(EntityMetadata::getEntityId));

		for (T entity : entities) {
			String entityId = entityIdFn.apply(entity);
			List<EntityMetadata> metadata = byEntityId.getOrDefault(entityId, Collections.emptyList());
			setter.set(entity, metadata, EntityMetadataMaps.toObjectMap(metadata),
				EntityMetadataMaps.toStringMap(metadata));
		}
	}

	public void replace(EntityMetadataType type, String entityId, Map<String, ?> metaData) {
		if (StringUtils.isBlank(entityId)) {
			return;
		}
		if (metaData == null) {
			return;
		}

		entityMetadataDao.deleteByEntity(type, entityId);
		if (metaData.isEmpty()) {
			return;
		}

		for (Map.Entry<String, ?> entry : metaData.entrySet()) {
			if (StringUtils.isBlank(entry.getKey())) {
				continue;
			}
			for (String value : EntityMetadataMaps.flattenValues(entry.getValue())) {
				if (StringUtils.isBlank(value)) {
					continue;
				}
				EntityMetadata entityMetadata = new EntityMetadata();
				entityMetadata.setType(type);
				entityMetadata.setEntityId(entityId);
				entityMetadata.setMetadataValue(findOrCreateValue(entry.getKey(), value));
				entityMetadataDao.create(entityMetadata);
			}
		}
	}

	public void deleteForEntity(EntityMetadataType type, String entityId) {
		entityMetadataDao.deleteByEntity(type, entityId);
	}

	public MetadataValue findOrCreateValue(String keyName, String value) {
		MetadataKey key = metadataKeyDao.findByName(keyName);
		if (key == null) {
			key = new MetadataKey();
			key.setName(keyName);
			key = metadataKeyDao.create(key);
		}

		MetadataValue existing = metadataValueDao.findByKeyAndValue(key.getId(), value);
		if (existing != null) {
			return existing;
		}

		MetadataValue metadataValue = new MetadataValue();
		metadataValue.setMetadataKey(key);
		metadataValue.setValue(value);
		return metadataValueDao.create(metadataValue);
	}

	public Map<String, List<String>> indexValuesByEntityId(EntityMetadataType type, Collection<String> entityIds) {
		Map<String, List<String>> result = new HashMap<>();
		for (EntityMetadata item : entityMetadataDao.getByEntities(type, entityIds)) {
			if (item.getMetadataValue() == null) {
				continue;
			}
			result.computeIfAbsent(item.getEntityId(), ignored -> new ArrayList<>())
				.add(item.getMetadataValue().getValue());
		}
		return result;
	}

	@FunctionalInterface
	public interface MetaDataSetter<T> {
		void set(T entity, List<EntityMetadata> entityMetadata, Map<String, Object> objectMap,
			Map<String, String> stringMap);
	}

}
