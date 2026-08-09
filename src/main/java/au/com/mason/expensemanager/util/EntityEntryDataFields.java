package au.com.mason.expensemanager.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
	private static final String NOTES = "notes";

	private EntityEntryDataFields() {
	}

	public static void flattenToDto(EntityType type, Map<String, Object> data, EntityEntryDto dto) {
		if (data == null || data.isEmpty()) {
			dto.setDataChunk(null);
			if (type == EntityType.RECIPE) {
				dto.setNotes(List.of());
			}
			return;
		}
		Map<String, Object> remaining = new LinkedHashMap<>(data);
		if (type == EntityType.RECIPE) {
			Object link = remaining.remove(LINK);
			dto.setLink(link == null ? null : String.valueOf(link));
			dto.setNotes(extractNotes(remaining.remove(NOTES)));
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
			List<String> notes = normalizeNotes(dto.getNotes());
			if (notes.isEmpty()) {
				data.remove(NOTES);
			} else {
				data.put(NOTES, notes);
			}
		}
		entity.setData(data.isEmpty() ? null : data);
	}

	private static List<String> extractNotes(Object notesObj) {
		if (notesObj == null) {
			return List.of();
		}
		if (notesObj instanceof List<?> list) {
			return list.stream()
				.filter(Objects::nonNull)
				.map(String::valueOf)
				.filter(StringUtils::isNotBlank)
				.toList();
		}
		return List.of();
	}

	private static List<String> normalizeNotes(List<String> notes) {
		if (notes == null || notes.isEmpty()) {
			return List.of();
		}
		List<String> normalized = new ArrayList<>();
		for (String note : notes) {
			if (StringUtils.isNotBlank(note)) {
				normalized.add(note.trim());
			}
		}
		return normalized;
	}

}
