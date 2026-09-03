package au.com.mason.expensemanager.dto;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class DocumentDtoTest {

	@Test
	void compareDocuments_sortsFilesByEmbeddedDateNewestFirst() {
		DocumentDto older = file("Payslip - 01-01-2026.pdf");
		DocumentDto newer = file("Payslip - 15-01-2026.pdf");

		assertTrue(DocumentDto.compareDocuments(newer, older) < 0);
		assertTrue(DocumentDto.compareDocuments(older, newer) > 0);
	}

	@Test
	void compareDocuments_sortsUndatedFilesAlphabetically() {
		DocumentDto alpha = file("Alpha Report.pdf");
		DocumentDto beta = file("Beta Report.pdf");

		assertTrue(DocumentDto.compareDocuments(alpha, beta) < 0);
		assertTrue(DocumentDto.compareDocuments(beta, alpha) > 0);
	}

	@Test
	void compareDocuments_fallsBackToAlphabeticalWhenOnlyOneFileHasDate() {
		DocumentDto dated = file("Payslip - 01-01-2026.pdf");
		DocumentDto undated = file("Annual Leave.pdf");

		assertTrue(DocumentDto.compareDocuments(undated, dated) < 0);
	}

	@Test
	void compareDocuments_keepsFoldersBeforeFiles() {
		DocumentDto folder = folder("2025-2026");
		DocumentDto file = file("Payslip - 01-01-2026.pdf");

		assertTrue(DocumentDto.compareDocuments(folder, file) < 0);
	}

	private static DocumentDto file(String fileName) {
		DocumentDto document = new DocumentDto();
		document.setFileName(fileName);
		document.setIsFolder(false);
		return document;
	}

	private static DocumentDto folder(String fileName) {
		DocumentDto document = new DocumentDto();
		document.setFileName(fileName);
		document.setIsFolder(true);
		return document;
	}

}
