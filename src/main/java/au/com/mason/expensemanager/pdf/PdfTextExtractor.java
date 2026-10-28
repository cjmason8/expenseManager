package au.com.mason.expensemanager.pdf;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;

@Component
public class PdfTextExtractor {

	public String extractText(byte[] content) throws IOException {
		return extract(content);
	}

	public List<String> extractLines(byte[] content) throws IOException {
		return Arrays.asList(extractText(content).split("\n"));
	}

	/**
	 * Orders text by its position on the page, which keeps labels and values on the
	 * same line for PDFs that store them as separate columns.
	 */
	public String extractTextSortedByPosition(byte[] content) throws IOException {
		try (PDDocument document = PDDocument.load(content)) {
			PDFTextStripper stripper = new PDFTextStripper();
			stripper.setSortByPosition(true);
			return stripper.getText(document);
		}
	}

	public static String extract(byte[] content) throws IOException {
		try (PDDocument document = PDDocument.load(content)) {
			PDFTextStripper stripper = new PDFTextStripper();
			return stripper.getText(document);
		}
	}

}
