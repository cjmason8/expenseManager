package au.com.mason.expensemanager.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import au.com.mason.expensemanager.dao.RentalPaymentDao;
import au.com.mason.expensemanager.domain.Document;
import au.com.mason.expensemanager.domain.RentalPayment;
import au.com.mason.expensemanager.util.S3Keys;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RentalPaymentServiceTest {

	private static final String EXPECTED_STATEMENTS_PREFIX =
			"/docs/expenseManager/filofax/IPs/Wodonga/2023-2024/Statements";

	@Mock
	private RentalPaymentDao rentalPaymentDao;

	@Mock
	private DocumentService documentService;

	private RentalPaymentService rentalPaymentService;

	@BeforeEach
	void setUp() {
		rentalPaymentService = new RentalPaymentService("/tmp/test-docs", rentalPaymentDao, documentService);
		lenient().doAnswer(inv -> {
			inv.getArgument(0, Document.class).setFolderPath(inv.getArgument(1, String.class));
			return null;
		}).when(documentService).moveDocumentToParentFolder(any(), any());
	}

	@Test
	void getRentalPayment_returnsPaymentFromDao() {
		RentalPayment expected = new RentalPayment();
		expected.setId(1L);
		expected.setProperty("WODONGA");
		when(rentalPaymentDao.getById(1L)).thenReturn(expected);

		RentalPayment result = rentalPaymentService.getRentalPayment(1L);

		assertSame(expected, result);
		verify(rentalPaymentDao).getById(1L);
	}

	@Test
	void getAll_filtersByFinancialYearEnd() {
		RentalPayment inYear = createMinimalRentalPayment();
		inYear.setStatementFrom(LocalDate.of(2024, 3, 1));

		RentalPayment outYear = createMinimalRentalPayment();
		outYear.setStatementFrom(LocalDate.of(2023, 3, 1));

		when(rentalPaymentDao.getByProperty("WODONGA")).thenReturn(List.of(inYear, outYear));

		List<RentalPayment> result = rentalPaymentService.getAll("WODONGA", 2024);

		assertEquals(List.of(inYear), result);
		verify(rentalPaymentDao).getByProperty("WODONGA");
	}

	@Test
	void deleteRentalPayment_callsDaoDeleteById() {
		rentalPaymentService.deleteRentalPayment(42L);

		verify(rentalPaymentDao).deleteById(42L);
	}

	@Test
	void createRentalPayment_withoutDocument_createsViaDaoAndReturns() throws Exception {
		RentalPayment payment = createMinimalRentalPayment();
		payment.setId(0);
		payment.setDocument(null);

		RentalPayment result = rentalPaymentService.createRentalPayment(payment);

		assertSame(payment, result);
		verify(rentalPaymentDao).create(payment);
		verify(documentService, never()).updateDocument(any());
	}

	@Test
	void createRentalPayment_withDocumentNullFileName_createsViaDaoOnly() throws Exception {
		RentalPayment payment = createMinimalRentalPayment();
		Document doc = new Document();
		doc.setFileName(null);
		doc.setOriginalFileName(null);
		payment.setDocument(doc);

		RentalPayment result = rentalPaymentService.createRentalPayment(payment);

		assertSame(payment, result);
		verify(rentalPaymentDao).create(payment);
		verify(documentService, never()).updateDocument(any());
	}

	@Test
	void createRentalPayment_withDocumentAndOriginalFileName_movesFileAndUpdatesDocument() throws Exception {
		RentalPayment payment = createMinimalRentalPayment();
		payment.setStatementFrom(LocalDate.of(2024, 3, 1));
		payment.setProperty("WODONGA");
		Document doc = new Document();
		doc.setFileName("statement.pdf");
		doc.setOriginalFileName("uploaded.pdf");
		doc.setFolderPath("tmp/test-docs/upload");
		payment.setDocument(doc);

		RentalPayment result = rentalPaymentService.createRentalPayment(payment);

		assertSame(payment, result);
		verify(rentalPaymentDao).create(payment);
		verify(documentService).moveDocumentToParentFolder(doc, EXPECTED_STATEMENTS_PREFIX);
		verify(documentService).updateDocument(doc);
		assertEquals(EXPECTED_STATEMENTS_PREFIX, doc.getFolderPath());
		assertEquals("statement.pdf", doc.getFileName());
	}

	@Test
	void updateRentalPayment_withoutDocument_updatesViaDaoAndReturns() throws Exception {
		RentalPayment payment = createMinimalRentalPayment();
		payment.setId(1L);
		payment.setDocument(null);
		when(rentalPaymentDao.getById(1L)).thenReturn(payment);

		RentalPayment result = rentalPaymentService.updateRentalPayment(payment);

		assertSame(payment, result);
		verify(rentalPaymentDao).update(payment);
		verify(documentService, never()).updateDocument(any());
	}

	@Test
	void updateRentalPayment_withDocumentNullFileName_clearsDocumentAndUpdates() throws Exception {
		RentalPayment existing = createMinimalRentalPayment();
		existing.setId(1L);
		existing.setDocument(new Document());
		RentalPayment payment = createMinimalRentalPayment();
		payment.setId(1L);
		Document docWithNoFileName = new Document();
		docWithNoFileName.setFileName(null);
		payment.setDocument(docWithNoFileName);
		when(rentalPaymentDao.getById(1L)).thenReturn(existing);

		RentalPayment result = rentalPaymentService.updateRentalPayment(payment);

		assertSame(payment, result);
		assertNull(payment.getDocument());
		verify(rentalPaymentDao).update(payment);
		verify(documentService, never()).updateDocument(any());
	}

	@Test
	void updateRentalPayment_withDocumentUnchangedFileName_updatesViaDaoOnly() throws Exception {
		RentalPayment existing = createMinimalRentalPayment();
		existing.setId(1L);
		Document existingDoc = new Document();
		existingDoc.setFileName("same.pdf");
		existing.setDocument(existingDoc);

		RentalPayment payment = createMinimalRentalPayment();
		payment.setId(1L);
		Document doc = new Document();
		doc.setFileName("same.pdf");
		doc.setOriginalFileName("same.pdf");
		payment.setDocument(doc);
		when(rentalPaymentDao.getById(1L)).thenReturn(existing);

		RentalPayment result = rentalPaymentService.updateRentalPayment(payment);

		assertSame(payment, result);
		verify(rentalPaymentDao).update(payment);
		verify(documentService, never()).updateDocument(any());
	}

	@Test
	void updateRentalPayment_withDocumentChangedFileName_movesFileAndUpdatesDocument() throws Exception {
		RentalPayment existing = createMinimalRentalPayment();
		existing.setId(1L);
		Document existingDoc = new Document();
		existingDoc.setFileName("old.pdf");
		existing.setDocument(existingDoc);

		RentalPayment payment = createMinimalRentalPayment();
		payment.setId(1L);
		payment.setStatementFrom(LocalDate.of(2024, 3, 1));
		payment.setProperty("WODONGA");
		Document doc = new Document();
		doc.setFileName("new.pdf");
		doc.setOriginalFileName("new.pdf");
		doc.setFolderPath("tmp/test-docs/upload");
		payment.setDocument(doc);
		when(rentalPaymentDao.getById(1L)).thenReturn(existing);

		RentalPayment result = rentalPaymentService.updateRentalPayment(payment);

		assertSame(payment, result);
		verify(rentalPaymentDao).update(payment);
		verify(documentService).moveDocumentToParentFolder(doc, EXPECTED_STATEMENTS_PREFIX);
		verify(documentService).updateDocument(doc);
	}

	@Test
	void updateRentalPayment_throwsWhenDaoThrows() {
		RentalPayment payment = createMinimalRentalPayment();
		payment.setId(1L);
		when(rentalPaymentDao.getById(1L)).thenThrow(new RuntimeException("DB error"));

		assertThrows(RuntimeException.class, () -> rentalPaymentService.updateRentalPayment(payment));
	}

	private static RentalPayment createMinimalRentalPayment() {
		RentalPayment p = new RentalPayment();
		p.setStatementFrom(LocalDate.of(2024, 1, 1));
		p.setStatementTo(LocalDate.of(2024, 1, 31));
		p.setProperty("WODONGA");
		p.setTotalRent(BigDecimal.valueOf(1000));
		p.setManagementFee(BigDecimal.ZERO);
		p.setAdminFee(BigDecimal.ZERO);
		p.setOtherFee(BigDecimal.ZERO);
		return p;
	}
}
