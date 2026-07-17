package au.com.mason.expensemanager.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import au.com.mason.expensemanager.domain.EntityMetadata;

public final class EntityMetadataMaps {

	private EntityMetadataMaps() {
	}

	public static Map<String, Object> toObjectMap(List<EntityMetadata> entityMetadata) {
		Map<String, Object> result = new LinkedHashMap<>();
		if (entityMetadata == null) {
			return result;
		}
		for (EntityMetadata item : entityMetadata) {
			if (item.getMetadataValue() == null || item.getMetadataValue().getMetadataKey() == null) {
				continue;
			}
			String key = item.getMetadataValue().getMetadataKey().getName();
			String value = item.getMetadataValue().getValue();
			Object existing = result.get(key);
			if (existing == null) {
				result.put(key, value);
			} else if (existing instanceof List<?> list) {
				@SuppressWarnings("unchecked")
				List<Object> mutable = (List<Object>) list;
				mutable.add(value);
			} else {
				List<Object> values = new ArrayList<>();
				values.add(existing);
				values.add(value);
				result.put(key, values);
			}
		}
		return result;
	}

	public static Map<String, String> toStringMap(List<EntityMetadata> entityMetadata) {
		Map<String, String> result = new LinkedHashMap<>();
		Map<String, Object> objectMap = toObjectMap(entityMetadata);
		for (Map.Entry<String, Object> entry : objectMap.entrySet()) {
			Object value = entry.getValue();
			if (value instanceof Collection<?> collection) {
				result.put(entry.getKey(), collection.isEmpty() ? null : String.valueOf(collection.iterator().next()));
			} else if (value != null) {
				result.put(entry.getKey(), String.valueOf(value));
			}
		}
		return result;
	}

	public static List<String> flattenValues(Object value) {
		List<String> values = new ArrayList<>();
		if (value == null) {
			return values;
		}
		if (value instanceof Collection<?> collection) {
			for (Object item : collection) {
				if (item != null) {
					values.add(String.valueOf(item));
				}
			}
		} else {
			values.add(String.valueOf(value));
		}
		return values;
	}

}
