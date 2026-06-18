package au.com.mason.expensemanager.pdf.rates;

import java.time.LocalDate;

public record RatesInstalmentNoticeData(LocalDate dueDate, String amount) {

}
