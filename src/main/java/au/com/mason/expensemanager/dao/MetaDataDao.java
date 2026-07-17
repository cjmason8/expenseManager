package au.com.mason.expensemanager.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import jakarta.persistence.EntityManager;

import org.apache.commons.lang3.StringUtils;

import au.com.mason.expensemanager.domain.Metadata;
import au.com.mason.expensemanager.dto.SearchParamsDto;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class MetaDataDao<T extends Metadata> extends BaseDao<T> {

	private Gson gson = new GsonBuilder().serializeNulls().create();
	public MetaDataDao(Class<T> className, EntityManager entityManager) {
		super(className, entityManager);
	}

	protected List<T> filterByMetadata(SearchParamsDto searchParamsDto, List<T> results) {
		if (StringUtils.isEmpty(searchParamsDto.getMetaDataChunk())) {
			return results;
		}

		@SuppressWarnings("unchecked")
		Map<String, Object> metaData = gson.fromJson(searchParamsDto.getMetaDataChunk(), Map.class);
		if (metaData == null || metaData.isEmpty()) {
			return results;
		}

		List<T> validResults = new ArrayList<>();
		results.stream().filter(result -> result.getMetaData() != null).forEach(result -> {
			boolean isValid = false;
			for (String key : metaData.keySet()) {
				Object storedValue = result.getMetaData().get(key);
				if (storedValue == null) {
					continue;
				}

				Object searchValue = metaData.get(key);
				if (matchesMetadataValue(storedValue, searchValue)) {
					isValid = true;
				}

				String keyWords = searchParamsDto.getKeyWords();
				if (StringUtils.isNotBlank(keyWords) && metadataContainsKeyword(storedValue, keyWords)) {
					isValid = true;
				}
			}
			if (isValid) {
				validResults.add(result);
			}
		});

		return validResults;
	}

	private boolean matchesMetadataValue(Object storedValue, Object searchValue) {
		if (searchValue instanceof Collection<?> searchValues) {
			for (Object item : searchValues) {
				if (valueEqualsOrContains(storedValue, item)) {
					return true;
				}
			}
			return false;
		}

		return valueEqualsOrContains(storedValue, searchValue);
	}

	private boolean valueEqualsOrContains(Object storedValue, Object searchValue) {
		if (storedValue instanceof Collection<?> storedValues) {
			return storedValues.stream().anyMatch(value -> valuesEqualIgnoreCase(value, searchValue));
		}

		return valuesEqualIgnoreCase(storedValue, searchValue);
	}

	private boolean metadataContainsKeyword(Object storedValue, String keyWords) {
		String needle = convertToStringAndLower(keyWords);
		if (needle == null) {
			return false;
		}

		if (storedValue instanceof Collection<?> storedValues) {
			return storedValues.stream().map(this::convertToStringAndLower)
				.filter(Objects::nonNull)
				.anyMatch(value -> value.contains(needle));
		}

		String haystack = convertToStringAndLower(storedValue);
		return haystack != null && haystack.contains(needle);
	}

	private boolean valuesEqualIgnoreCase(Object left, Object right) {
		return Objects.equals(convertToStringAndLower(left), convertToStringAndLower(right));
	}

	private String convertToStringAndLower(Object val) {
		if (val == null) {
			return null;
		}

		return String.valueOf(val).toLowerCase();
	}
}
