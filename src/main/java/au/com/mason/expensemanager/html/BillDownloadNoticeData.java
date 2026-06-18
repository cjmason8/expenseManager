package au.com.mason.expensemanager.html;

import java.time.LocalDate;

public record BillDownloadNoticeData(
		LocalDate dueDate,
		String amount,
		String downloadUrl) {

}
