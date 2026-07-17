package au.com.mason.expensemanager.dao;

import org.hibernate.dialect.PostgreSQLDialect;

/**
 * Legacy dialect class kept for existing deployments that still set
 * HIBERNATE_DIALECT=au.com.mason.expensemanager.dao.MyPostgreSQL94Dialect.
 * Behaviour is identical to {@link PostgreSQLDialect}.
 */
public class MyPostgreSQL94Dialect extends PostgreSQLDialect {
}
