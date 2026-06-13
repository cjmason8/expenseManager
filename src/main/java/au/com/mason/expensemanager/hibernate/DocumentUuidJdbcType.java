package au.com.mason.expensemanager.hibernate;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import org.hibernate.type.SqlTypes;
import org.hibernate.type.descriptor.ValueBinder;
import org.hibernate.type.descriptor.ValueExtractor;
import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.type.descriptor.java.JavaType;
import org.hibernate.type.descriptor.jdbc.JdbcType;
import org.hibernate.type.descriptor.jdbc.UUIDJdbcType;

/**
 * Reads {@code documents.id} / FK columns as {@link UUID}, or as legacy {@code bigint} by mapping
 * to the same deterministic UUID as {@code migration.sql} (until DB columns are migrated to uuid).
 */
public final class DocumentUuidJdbcType implements JdbcType {

	public static final DocumentUuidJdbcType INSTANCE = new DocumentUuidJdbcType();

	private static final JdbcType DELEGATE = UUIDJdbcType.INSTANCE;

	private DocumentUuidJdbcType() {}

	@Override
	public int getJdbcTypeCode() {
		return SqlTypes.UUID;
	}

	@Override
	public int getDefaultSqlTypeCode() {
		return SqlTypes.UUID;
	}

	@Override
	public <X> ValueBinder<X> getBinder(JavaType<X> javaType) {
		return DELEGATE.getBinder(javaType);
	}

	@Override
	public <X> ValueExtractor<X> getExtractor(JavaType<X> javaType) {
		return new ValueExtractor<X>() {
			@Override
			public X extract(ResultSet rs, int paramIndex, WrapperOptions options) throws SQLException {
				return javaType.wrap(coerce(rs.getObject(paramIndex)), options);
			}

			@Override
			public X extract(CallableStatement statement, int paramIndex, WrapperOptions options) throws SQLException {
				return javaType.wrap(coerce(statement.getObject(paramIndex)), options);
			}

			@Override
			public X extract(CallableStatement statement, String paramName, WrapperOptions options) throws SQLException {
				return javaType.wrap(coerce(statement.getObject(paramName)), options);
			}
		};
	}

	private static Object coerce(Object o) throws SQLException {
		if (o == null) {
			return null;
		}
		if (o instanceof UUID) {
			return o;
		}
		if (o instanceof Number) {
			return LegacyDocumentIdMapping.uuidFromLegacyLong(((Number) o).longValue());
		}
		if (o instanceof String) {
			return UUID.fromString((String) o);
		}
		throw new SQLException("Unsupported JDBC value for document id column: " + o.getClass().getName());
	}
}
