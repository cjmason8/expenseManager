package au.com.mason.expensemanager.pdf.payslip;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PayslipData(LocalDate payToDate, BigDecimal annualLeaveFullTimeYtdHours) {
}
