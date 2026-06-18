package au.com.mason.expensemanager.pdf.invoice;

final class GlobirdInvoiceFixtures {

	static final String STANDARD_INVOICE = """
		Issue Date 01-Jan-2026
		Due Date 15-Jan-2026
		Amount Due
		$123.45
		""";

	static final String ZERO_CREDIT_INVOICE = """
		Issue Date 01-Feb-2026
		Amount Due
		$0.00
		""";

	private GlobirdInvoiceFixtures() {
	}

}
