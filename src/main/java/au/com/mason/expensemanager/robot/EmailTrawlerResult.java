package au.com.mason.expensemanager.robot;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;

@Getter
public class EmailTrawlerResult {

	private boolean success = true;
	private String error;
	private final List<String> details = new ArrayList<>();

	public void addDetail(String detail) {
		details.add(detail);
	}

	public void fail(String message) {
		success = false;
		error = message;
	}

	public String summary() {
		if (!success) {
			return error;
		}
		if (details.isEmpty()) {
			return "EmailTrawler completed with no folder activity";
		}
		return String.join("; ", details);
	}

}
