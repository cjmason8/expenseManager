package au.com.mason.expensemanager.util;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import au.com.mason.expensemanager.domain.EntityEntry;
import au.com.mason.expensemanager.domain.EntityType;
import au.com.mason.expensemanager.dto.EntityEntryDto;
import au.com.mason.expensemanager.mapper.MappingConverters;

/**
 * Maps type-specific fields between {@code entities.data} JSON and flat DTO
 * fields exposed to the frontend.
 */
public final class EntityEntryDataFields {

	private static final String LINK = "link";

	private EntityEntryDataFields() {
	}

	public static void flattenToDto(EntityType type, Map<String, Object> data, EntityEntryDto dto) {
		if (data == null || data.isEmpty()) {
			dto.setDataChunk(null);
			return;
		}
		Map<String, Object> remaining = new LinkedHashMap<>(data);
		if (type == EntityType.RECIPE) {
			Object link = remaining.remove(LINK);
			dto.setLink(link == null ? null : String.valueOf(link));
		}
		dto.setDataChunk(MappingConverters.objectMapToJson(remaining.isEmpty() ? null : remaining));
	}

	public static void mergeFromDto(EntityEntryDto dto, EntityEntry entity) {
		EntityType type = entity.getType();
		if (type == null && StringUtils.isNotBlank(dto.getType())) {
			type = EntityType.valueOf(dto.getType());
		}
		Map<String, Object> data = entity.getData() != null
			? new LinkedHashMap<>(entity.getData())
			: new LinkedHashMap<>();
		if (type == EntityType.RECIPE) {
			if (StringUtils.isNotBlank(dto.getLink())) {
				data.put(LINK, dto.getLink());
			} else {
				data.remove(LINK);
			}
		}
		entity.setData(data.isEmpty() ? null : data);
	}

}
