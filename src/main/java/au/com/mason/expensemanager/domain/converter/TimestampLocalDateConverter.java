package au.com.mason.expensemanager.domain.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.sql.Timestamp;
import java.time.LocalDate;

@Converter
public class TimestampLocalDateConverter implements AttributeConverter<LocalDate, Timestamp> {

	@Override
	public Timestamp convertToDatabaseColumn(LocalDate attribute) {
		if (attribute == null) {
			return null;
		}
		return Timestamp.valueOf(attribute.atStartOfDay());
	}

	@Override
	public LocalDate convertToEntityAttribute(Timestamp dbData) {
		if (dbData == null) {
			return null;
		}
		return dbData.toLocalDateTime().toLocalDate();
	}

}
