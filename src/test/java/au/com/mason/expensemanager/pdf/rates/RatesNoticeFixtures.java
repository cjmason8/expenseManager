package au.com.mason.expensemanager.pdf.rates;

final class RatesNoticeFixtures {

	static final String SOUTH_KINGSVILLE_FIRST_NOTICE = """
		Payments received after 5.00pm incur a penalty
		$200.00 $250.00 $300.00
		01/09/2025 01/11/2025 01/02/2026 01/05/2026
		1st Instalment $150.00 due
		""";

	static final String SOUTH_KINGSVILLE_FIRST_NOTICE_2026 = """
		Due 30/11/2026Due 30/09/2026Immediately Due 28/02/2027 Due 31/05/2027
		Address:
		2026/2027
		Payments received after 1 August 2026 are not included in this notice
		$0.00 $550.90 $548.00 $548.00 $548.00 $2,194.90
		$0.00
		$550.90
		30/09/2026
		writing of any change of address. Please
		""";

	static final String WODONGA_FIRST_NOTICE = """
		$150.00 $200.00 $250.00 $300.00
		Other content
		""";

	static final String DINGLEY_INSTALMENT_NOTICE = """
		Total Amount Due 15 March 2026 $500.00
		""";

	static final String DINGLEY_FIRST_NOTICE = """
		$150.00
		$200.00
		$250.00
		$300.00
		""";

	private RatesNoticeFixtures() {
	}

}
