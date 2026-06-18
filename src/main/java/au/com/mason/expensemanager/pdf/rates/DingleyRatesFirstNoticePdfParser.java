package au.com.mason.expensemanager.pdf.rates;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import au.com.mason.expensemanager.pdf.PdfExtractor;
import au.com.mason.expensemanager.pdf.PdfTextExtractor;

@Component
public class DingleyRatesFirstNoticePdfParser {

	private final PdfTextExtractor pdfTextExtractor;

	public DingleyRatesFirstNoticePdfParser(PdfTextExtractor pdfTextExtractor) {
		this.pdfTextExtractor = pdfTextExtractor;
	}

	public List<RatesInstalmentData> parse(byte[] pdfBytes, String firstInstalment, int year) throws IOException {
		PdfExtractor pdf = PdfExtractor.from(pdfTextExtractor, pdfBytes);

		List<RatesInstalmentData> instalments = new ArrayList<>();
		instalments.add(new RatesInstalmentData(LocalDate.of(year, 9, 30), firstInstalment, 1));

		boolean foundFirst = false;
		int counter = 1;
		for (String line : pdf.lines()) {
			if (!line.startsWith("$")) {
				continue;
			}
			if (line.contains(firstInstalment)) {
				foundFirst = true;
			} else if (foundFirst && counter <= 3) {
				instalments.add(
					new RatesInstalmentData(instalmentDueDate(year, counter + 1), line.replace("$", ""), counter + 1));
				counter++;
			}
		}

		if (instalments.size() != 4) {
			throw new IllegalStateException("Dingley rates notice PDF missing instalment amounts");
		}

		return instalments;
	}

	private LocalDate instalmentDueDate(int year, int instalmentNumber) {
		return switch (instalmentNumber) {
			case 2 -> LocalDate.of(year, 11, 30);
			case 3 -> LocalDate.of(year + 1, 2, 28);
			case 4 -> LocalDate.of(year + 1, 5, 31);
			default -> throw new IllegalArgumentException("Unexpected instalment number: " + instalmentNumber);
		};
	}

}
