package au.com.mason.expensemanager.repository;

import java.sql.Types;

import org.hibernate.dialect.PostgreSQL94Dialect;

public class MyPostgreSQL94Dialect extends PostgreSQL94Dialect {

	public MyPostgreSQL94Dialect() {
		this.registerColumnType(Types.JAVA_OBJECT, "jsonb");
	}
}
