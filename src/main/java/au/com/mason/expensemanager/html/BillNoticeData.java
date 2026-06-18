package au.com.mason.expensemanager.html;

import java.time.LocalDate;

public record BillNoticeData(LocalDate dueDate, String amount) {

}
