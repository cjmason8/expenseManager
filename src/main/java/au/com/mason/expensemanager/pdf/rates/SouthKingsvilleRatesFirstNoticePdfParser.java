package au.com.mason.expensemanager.pdf.rates;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import au.com.mason.expensemanager.pdf.PdfExtractor;
import au.com.mason.expensemanager.pdf.PdfTextExtractor;

@Component
public class SouthKingsvilleRatesFirstNoticePdfParser {

	private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

	private final PdfTextExtractor pdfTextExtractor;

	public SouthKingsvilleRatesFirstNoticePdfParser(PdfTextExtractor pdfTextExtractor) {
		this.pdfTextExtractor = pdfTextExtractor;
	}

	public List<RatesInstalmentData> parse(byte[] pdfBytes) throws IOException {
		PdfExtractor pdf = PdfExtractor.from(pdfTextExtractor, pdfBytes);

		String[] instalmentAmounts = null;
		LocalDate[] dueDates = null;
		String firstInstalment = null;
		int step = 0;

		for (String line : pdf.lines()) {
			if (line.startsWith("Payments received after")) {
				step = 1;
			} else if (step == 1) {
				instalmentAmounts = line.split(" ");
				step = 2;
			} else if (step == 2) {
				dueDates = parseDates(line);
				step = 3;
			} else if (step == 3 && line.contains("1st Instalment")) {
				int startIndex = line.indexOf('$');
				firstInstalment = line.substring(startIndex, line.indexOf(' ', startIndex));
			}
		}

		if (instalmentAmounts == null || dueDates == null || firstInstalment == null) {
			throw new IllegalStateException("South Kingsville rates notice PDF missing required fields");
		}

		List<RatesInstalmentData> instalments = new ArrayList<>();
		instalments.add(new RatesInstalmentData(dueDates[0], firstInstalment, 1));
		instalments.add(new RatesInstalmentData(dueDates[1], instalmentAmounts[0], 2));
		instalments.add(new RatesInstalmentData(dueDates[2], instalmentAmounts[1], 3));
		instalments.add(new RatesInstalmentData(dueDates[3], instalmentAmounts[2], 4));
		return instalments;
	}

	private LocalDate[] parseDates(String line) {
		String[] dateParts = line.split(" ");
		return new LocalDate[]{LocalDate.parse(dateParts[0], DATE_FORMAT), LocalDate.parse(dateParts[1], DATE_FORMAT),
			LocalDate.parse(dateParts[2], DATE_FORMAT), LocalDate.parse(dateParts[3], DATE_FORMAT),};
	}

}
