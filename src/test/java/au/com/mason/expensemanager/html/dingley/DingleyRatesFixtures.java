package au.com.mason.expensemanager.html.dingley;

final class DingleyRatesFixtures {

	static final String INSTALMENT_EMAIL = """
		<html><body>
		<a href="https://example.com/rates-instalment.pdf">Click here to follow link</a>
		</body></html>
		""";

	static final String FIRST_NOTICE_EMAIL = """
		<html><body>
		<a href="https://example.com/rates-first.pdf">Click here to follow link</a>
		<p>First instalment &#36;150.00&#160;</p>
		<span>2024&#47;2025</span>
		<span>&#47;2025</span>
		</body></html>
		""";

	private DingleyRatesFixtures() {
	}

}
