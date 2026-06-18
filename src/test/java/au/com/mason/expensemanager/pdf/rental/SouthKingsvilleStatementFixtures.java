package au.com.mason.expensemanager.pdf.rental;

final class SouthKingsvilleStatementFixtures {

	static final String STANDARD_STATEMENT = """
		Money In $2,151.00
		Money Out $146.91
		You Received $2,004.09
		Rent paid to 10/12/2025 with part payment of $187.00 (previously paid to 10/11/2025 + $187.00) $2,151.00
		Management fee ... * $141.96
		Accounting Fee * $4.95
		""";

	static final String MOVED_IN_STATEMENT = """
		Money In $2,238.00
		You Received $2,085.35
		Rent paid to 10/07/2026 with tenant moved in 10/06/2026 $2,238.00
		Management fee ... * $147.70
		Accounting Fee * $4.95
		""";

	static final String UNBALANCED_STATEMENT = """
		Money In $2,151.00
		You Received $1.00
		Rent paid to 10/12/2025 with part payment (previously paid to 10/11/2025 + $187.00) $2,151.00
		Management fee ... * $141.96
		Accounting Fee * $4.95
		""";

	private SouthKingsvilleStatementFixtures() {
	}

}
