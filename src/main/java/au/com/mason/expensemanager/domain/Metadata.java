package au.com.mason.expensemanager.domain;

import java.util.Map;

public interface Metadata {
	default Map<String, Object> getMetaData() {
		return null;
	}
}
