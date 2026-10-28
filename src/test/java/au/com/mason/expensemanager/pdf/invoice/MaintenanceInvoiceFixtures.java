package au.com.mason.expensemanager.pdf.invoice;

final class MaintenanceInvoiceFixtures {

	static final String XERO_TOTAL_AUD = """
		PAYMENT ADVICE
		Amount Due 176.00
		Due Date 28 Jun 2020
		Amount Enclosed
		Enter the amount you are paying above
		Description Quantity Unit Price GST Amount AUD
		Repair leaking tap 1.00 160.00 10% 160.00
		Subtotal 160.00
		TOTAL  GST  10% 16.00
		TOTAL AUD 176.00
		Due Date: 28 Jun 2020
		""";

	static final String TRADIFY_TOTAL_FIRST = """
		Subtotal $668.64
		GST Amount $66.86
		Total $735.50
		Due Date: 3rd May 2020
		Name Quantity Price Total
		Asbestos test 1.00 $113.64 $113.64
		$668.64
		100% of original quoted price 1.00 $555.00 $555.00
		""";

	static final String SERVICE_GRAND_TOTAL = """
		Incl. TotalExcl. TotalTax %Disc %Excl. PriceQuantityDescription
		DUE DATE:
		$770.00$700.0010.00%0.00%$700.001.00Details of Job - Patching and painting
		$770.00$700.0010.00%0.00%$700.001.00Details of Job - Caulking grouting
		$1,540.00Sub Total:
		Grand Total: $1,540.00
		$140.00
		$1,400.00
		$0.00
		Total Tax:
		Total Exclusive:
		Total Discount:
		BALANCE DUE
		$1,540.00
		""";

	static final String SHARED_FENCE = """
		Details of Job - Rotten timber fence
		Supply and install new colour bond fencing @ total height of 2 m using:
		ADDRESS = 1 Example Street $575inc
		ADDRESS = 3 Example Street $575inc
		Please note, upon acceptance of this quotation a 50% deposit is required before commencing with the job with balance due upon completion
		$1,150.00Sub Total:
		Grand Total: $1,150.00
		$104.55
		$1,045.45
		$0.00
		Total Tax:
		Total Exclusive:
		Total Discount:
		BALANCE DUE
		$575.00
		""";

	static final String DEPOSIT_PAID = """
		DESCRIPTION QTY UNIT PRICE TOTAL PRICE
		Replace hot water system 1 $3,000.00 $3,000.00
		SUBTOTAL: $3,000.00
		GST: $300.00
		TOTAL: $3,300.00
		PAID: $500.00
		BALANCE DUE: $2,800.00
		""";

	static final String FULLY_PAID = """
		FULLY PAID
		AMOUNT
		$379.00
		Item Quantity Unit Price Total (inc GST)
		Drain clearing 1.0 $379.00 $379.00
		Payments are subject to funds being cleared. Total $379.00
		If you have any queries, please do not hesitate to contact us. Incl. GST of $34.45
		Terms and Conditions apply. Amount Paid $379.00
		Balance Due $0.00
		""";

	static final String TOTAL_INC_GST = """
		Description Quantity Rate GST Total
		Smoke alarm service
		1 $90.00 $9.00 $99.00
		Subtotal $90.00
		GST $9.00
		Total Inc GST $99.00
		""";

	static final String MYOB_TOTAL_AMOUNT = """
		Tax invoice Invoice number Issue date Due date
		(exc. tax)Subtotal $100.00
		Tax $10.00
		(inc. tax)Total Amount $110.00
		Total paid $0.00
		Balance due $110.00
		Page 1 of 1 Invoice no: 00010298      Due date: 14/12/2024      Balance due: $110.00
		""";

	static final String AMOUNT_BEFORE_LABEL = """
		Code Description Unit Ordered Supplied Tax% Price Inc Subtotal
		EACH 3.00 $5.00 $15.003.00Extra key cut
		Acc. Fee $0.00
		Freight $0.00
		$1.36 $15.00Total
		$15.00$1.36
		Sub Total
		""";

	static final String TOTAL_SPLIT_ACROSS_LINES = """
		Description of works completed: Unit Price GST  Amount
		Supply and install toilet suite
		740.00
		SUBTOTAL 740.00
		GST 74.00
		TOTAL
		AUD
		814.00
		Due Date: 7 days from the date of Invoice.
		""";

	static final String DOLLAR_WITH_SPACE = """
		Kitchen/ Meals  $ 10.00  $ 110.00
		Sub Total  $ -00  $ 110.00
		Account No: 000 000 000 TOTAL  $ 110.00
		""";

	static final String QUICKBOOKS_BALANCE_DUE = """
		DATE ACTIVITY QTY RATE AMOUNT
		Replace globes $35
		INCLUDES GST TOTAL 31.36
		TOTAL 345.00
		BALANCE DUE A$345.00
		""";

	static final String ONLY_AMOUNT_DUE = """
		Callout and repair
		Please Pay $324.50
		""";

	static final String PROSE_MENTIONS_TOTAL = """
		Ezyfix Total Property
		Description Quantity Unit Price GST Amount AUD
		Subtotal 340.00
		TOTAL  GST  10% 34.00
		TOTAL AUD 374.00
		An administrative charge of 10% of the total amount
		will be added. Late fee $999.00
		""";

	static final String QUOTE_WITHOUT_TOTAL = """
		Sub Total  $ -00  $ 275.00
		Quote Only  $ 275.00
		""";

	static final String BLANK_TOTAL_FORM = """
		CARPET STEAM CLEANING $ 80.00
		UPHOLSTERY CLEANING $
		TOTAL INC GST    $
		""";

	static final String LETTER_SPACED_TOTAL = """
		Q t y A d d r e s s D e s c r i p t i o n   T o t a l
		1 Example Drive
		Cleaning
		Payment Details $385.00
		T o t a l $385.00
		""";

	static final String LABELS_AND_VALUES_IN_COLUMNS = """
		Balance
		Applied
		Total
		GST
		Sub - Total
		TO SUPPLY AND INSTALL
		$568.00$516.36SHEET VINYL TO DINING ROOM
		$516.36
		$51.64
		$568.00
		$0.00
		$568.00
		""";

	static final String LABELS_AND_VALUES_SORTED_BY_POSITION = """
		TO SUPPLY AND INSTALL                      Sub - Total $516.36
		balance must be paid in full                       GST $51.64
		installation/delivery.                           Total $568.00
		                                               Applied $0.00
		payment in full has been received            Balance $568.00
		""";

	private MaintenanceInvoiceFixtures() {
	}

}
