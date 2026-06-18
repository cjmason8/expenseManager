package au.com.mason.expensemanager.pdf;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;

class PdfTextExtractorTest {

	@Test
	void extractText_readsStatementPdf() throws Exception {
		byte[] pdfBytes = Files.readAllBytes(Path.of("statement.pdf"));

		String text = new PdfTextExtractor().extractText(pdfBytes);

		assertTrue(text.contains("Money In $2,151.00"));
		assertTrue(text.contains("You Received $2,004.09"));
		assertTrue(text.contains("Rent paid to 10/12/2025"));
	}

	@Test
	void extractLines_splitsStatementPdfIntoLines() throws Exception {
		byte[] pdfBytes = Files.readAllBytes(Path.of("statement.pdf"));

		List<String> lines = new PdfTextExtractor().extractLines(pdfBytes);

		assertTrue(lines.stream().anyMatch(line -> line.startsWith("Money In")));
		assertTrue(lines.stream().anyMatch(line -> line.contains("Management fee")));
	}

	@Test
	void extract_staticMethodMatchesInstanceMethod() throws Exception {
		byte[] pdfBytes = Files.readAllBytes(Path.of("statement.pdf"));

		String fromStatic = PdfTextExtractor.extract(pdfBytes);
		String fromInstance = new PdfTextExtractor().extractText(pdfBytes);

		assertTrue(fromStatic.contains("Statement #93"));
		assertTrue(fromInstance.contains("Statement #93"));
	}

}
