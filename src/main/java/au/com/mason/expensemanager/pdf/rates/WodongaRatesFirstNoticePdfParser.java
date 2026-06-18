package au.com.mason.expensemanager.pdf.rates;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import au.com.mason.expensemanager.pdf.PdfExtractor;
import au.com.mason.expensemanager.pdf.PdfTextExtractor;

@Component
public class WodongaRatesFirstNoticePdfParser {

	private final PdfTextExtractor pdfTextExtractor;

	public WodongaRatesFirstNoticePdfParser(PdfTextExtractor pdfTextExtractor) {
		this.pdfTextExtractor = pdfTextExtractor;
	}

	public List<RatesInstalmentData> parse(byte[] pdfBytes, String firstInstalmentAmount, int year) throws IOException {
		PdfExtractor pdf = PdfExtractor.from(pdfTextExtractor, pdfBytes);

		String reqLine = pdf.lines().stream().filter(line -> line.startsWith("$" + firstInstalmentAmount)).findFirst()
			.orElseThrow(() -> new IllegalStateException(
				"Wodonga rates notice PDF missing instalment line for amount: " + firstInstalmentAmount));

		String[] instalmentAmounts = reqLine.split(" ");
		if (instalmentAmounts.length < 4) {
			throw new IllegalStateException("Wodonga rates notice PDF instalment line incomplete");
		}

		List<RatesInstalmentData> instalments = new ArrayList<>();
		instalments.add(new RatesInstalmentData(LocalDate.of(year, 9, 30), instalmentAmounts[0].substring(1), 1));
		instalments.add(new RatesInstalmentData(LocalDate.of(year, 11, 30), instalmentAmounts[1].substring(1), 2));
		instalments.add(new RatesInstalmentData(LocalDate.of(year + 1, 2, 28), instalmentAmounts[2].substring(1), 3));
		instalments.add(new RatesInstalmentData(LocalDate.of(year + 1, 5, 31), instalmentAmounts[3].substring(1), 4));
		return instalments;
	}

}
