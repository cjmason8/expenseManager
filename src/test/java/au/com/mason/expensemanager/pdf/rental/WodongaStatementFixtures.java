package au.com.mason.expensemanager.pdf.rental;

final class WodongaStatementFixtures {

	static final String STANDARD_STATEMENT = """
		Statement\tperiod 1 July 2025\t-\t31 July 2025
		Total\tincome\t$1,000.00
		Rent\tCommission\t$100.00
		Sundry\tFee
		$10.00
		Payment\tto\towner
		$890.00
		""";

	static final String ADMIN_FEE_ON_SAME_LINE = """
		Statement\tperiod 1 July 2025\t-\t31 July 2025
		Total\tincome\t$1,000.00
		Rent\tCommission\t$100.00
		Sundry\tFee\t$10.00
		Payment\tto\towner
		$890.00
		""";

	private WodongaStatementFixtures() {
	}

}
