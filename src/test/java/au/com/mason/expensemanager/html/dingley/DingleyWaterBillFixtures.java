package au.com.mason.expensemanager.html.dingley;

final class DingleyWaterBillFixtures {

	static final String STANDARD_BILL = """
		<html><body>
		<p>Total: <span>$123.45</span></p>
		<tr><td>Date due</td><td colspan="2" align="right">15 March 2026</td></tr>
		</body></html>
		""";

	static final String BILL_WITH_WBR = """
		<html><body>
		<p>Total: <span>$99.00</span></p>
		<tr><td>Date due</td><td colspan="2" align="right">15 M<wbr>arch 2026</td></tr>
		</body></html>
		""";

	private DingleyWaterBillFixtures() {
	}

}
