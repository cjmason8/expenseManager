package au.com.mason.expensemanager.mapper;

import au.com.mason.expensemanager.domain.RefDataType;
import au.com.mason.expensemanager.dto.DocumentDto;
import au.com.mason.expensemanager.util.DateUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.Condition;
import org.mapstruct.Named;

public final class MappingConverters {

	private static final Gson GSON = new GsonBuilder().serializeNulls().create();

	private MappingConverters() {
	}

	@Named("localDateToString")
	public static String localDateToString(LocalDate date) {
		return date == null ? null : DateUtil.getFormattedDateString(date);
	}

	@Named("stringToLocalDate")
	public static LocalDate stringToLocalDate(String date) {
		if (StringUtils.isBlank(date)) {
			return null;
		}
		return DateUtil.getFormattedDate(date);
	}

	@Named("bigDecimalToString")
	public static String bigDecimalToString(BigDecimal value) {
		return value == null ? null : value.toString();
	}

	@Named("stringToBigDecimal")
	public static BigDecimal stringToBigDecimal(String value) {
		if (StringUtils.isBlank(value)) {
			return null;
		}
		return new BigDecimal(value);
	}

	@Named("objectMapToJson")
	public static String objectMapToJson(Map<String, Object> map) {
		return map == null ? null : GSON.toJson(map);
	}

	@Named("jsonToObjectMap")
	@SuppressWarnings("unchecked")
	public static Map<String, Object> jsonToObjectMap(String json) {
		if (StringUtils.isBlank(json)) {
			return null;
		}
		return GSON.fromJson(json, Map.class);
	}

	@Named("stringMapToJson")
	public static String stringMapToJson(Map<String, String> map) {
		return map == null ? null : GSON.toJson(map);
	}

	@Named("jsonToStringMap")
	@SuppressWarnings("unchecked")
	public static Map<String, String> jsonToStringMap(String json) {
		if (StringUtils.isBlank(json)) {
			return null;
		}
		return GSON.fromJson(json, Map.class);
	}

	@Named("refDataTypeToString")
	public static String refDataTypeToString(RefDataType type) {
		return type == null ? null : type.name();
	}

	@Named("stringToRefDataType")
	public static RefDataType stringToRefDataType(String type) {
		return type == null ? null : Enum.valueOf(RefDataType.class, type);
	}

	@Named("refDataTypeDescription")
	public static String refDataTypeDescription(RefDataType type) {
		return type == null ? null : type.getDescription();
	}

	@Named("hasDocumentFileName")
	@Condition
	public static boolean hasDocumentFileName(DocumentDto documentDto) {
		return documentDto != null
				&& (documentDto.getId() != null || StringUtils.isNotBlank(documentDto.getFileName()));
	}

}
