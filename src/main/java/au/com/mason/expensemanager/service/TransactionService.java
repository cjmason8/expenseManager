package au.com.mason.expensemanager.service;

import java.time.LocalDate;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import au.com.mason.expensemanager.dao.TransactionDao;
import au.com.mason.expensemanager.domain.Document;
import au.com.mason.expensemanager.domain.EntityMetadataType;
import au.com.mason.expensemanager.domain.Income;
import au.com.mason.expensemanager.domain.RecurringUnit;
import au.com.mason.expensemanager.domain.Transaction;
import au.com.mason.expensemanager.util.DateUtil;

@Component
public abstract class TransactionService<V extends Transaction, D extends TransactionDao<V>> {

	@Autowired
	protected D transactionDao;

	@Autowired
	protected DocumentService documentService;

	@Autowired
	protected EntityMetadataService entityMetadataService;

	public List<V> getAllRecurring(boolean includeAll) throws Exception {
		List<V> results = transactionDao.getAllRecurring(includeAll);
		hydrateTransactions(results);
		return results;
	}

	public List<V> getForWeek(LocalDate startOfWeek) throws Exception {
		List<V> results = transactionDao.getForWeek(startOfWeek);
		hydrateTransactions(results);
		return results;
	}

	public V getById(Long id) throws Exception {
		V transaction = transactionDao.getById(id);
		hydrateTransaction(transaction);
		return transaction;
	}

	public V addTransaction(V expense) throws Exception {
		attachDocumentForCreate(expense);
		createTransaction(expense);
		handleRecurring(expense);
		hydrateTransaction(expense);
		return expense;
	}

	private void attachDocumentForCreate(V transaction) throws Exception {
		Document doc = transaction.getDocument();
		if (!isDocumentAttached(doc)) {
			transaction.setDocument(null);
			return;
		}
		transaction.setDocument(resolveDocument(doc));
	}

	private void attachDocumentForUpdate(V transaction) throws Exception {
		Document doc = transaction.getDocument();
		if (!isDocumentAttached(doc)) {
			return;
		}
		transaction.setDocument(resolveDocument(doc));
	}

	private boolean isDocumentAttached(Document doc) {
		return doc != null && (doc.getId() != null || StringUtils.isNotBlank(doc.getFileName()));
	}

	private Document resolveDocument(Document doc) throws Exception {
		if (doc.getId() != null) {
			doc = documentService.getById(doc.getId());
		}
		if (StringUtils.isBlank(doc.getOriginalFileName()) && StringUtils.isNotBlank(doc.getFileName())) {
			doc.setOriginalFileName(doc.getFileName());
		}
		syncDocumentDisplayName(doc);
		return doc;
	}

	private void syncDocumentDisplayName(Document doc) throws Exception {
		if (doc == null || StringUtils.isBlank(doc.getOriginalFileName()) || StringUtils.isBlank(doc.getFileName())) {
			return;
		}
		if (!doc.getOriginalFileName().equals(doc.getFileName())) {
			documentService.updateDocument(doc);
		}
	}

	private void handleRecurring(V expense) throws Exception {
		if (expense.getRecurringType() != null) {
			V newExpenseForStartDate = createTransaction();
			newExpenseForStartDate.setEntryType(expense.getEntryType());
			newExpenseForStartDate.setMetaData(expense.getMetaData());
			newExpenseForStartDate.setAmount(expense.getAmount());
			newExpenseForStartDate.setDueDate(expense.getStartDate());
			newExpenseForStartDate.setNotes(expense.getNotes());
			newExpenseForStartDate.setRecurringTransaction(expense);

			createTransaction(newExpenseForStartDate);
			createSubsequentWeeks(expense);
		}
	}

	private void createTransaction(V expense) throws Exception {
		if (expense.getRecurringType() == null) {
			initialiseWeek(DateUtil.getMonday(expense.getDueDate()), expense.getRecurringTransaction());
		}

		transactionDao.create(expense);
		persistMetadata(expense);
	}

	private void createSubsequentWeeks(V newExpense) throws Exception {
		RecurringUnit recurringUnit = RecurringUnit.valueOf(newExpense.getRecurringType().getDescriptionUpper());

		LocalDate dueDate = newExpense.getStartDate().plus(recurringUnit.getUnits(), recurringUnit.getUnitType());

		while (getPastDate(DateUtil.getMonday(dueDate)) > 0) {
			if (countForWeekForAll(DateUtil.getMonday(dueDate)) > 0) {
				V newExpenseForSubsequent = createTransaction();
				newExpenseForSubsequent.setEntryType(newExpense.getEntryType());
				newExpenseForSubsequent.setMetaData(newExpense.getMetaData());
				newExpenseForSubsequent.setAmount(newExpense.getAmount());
				newExpenseForSubsequent.setDueDate(dueDate);
				newExpenseForSubsequent.setNotes(newExpense.getNotes());
				newExpenseForSubsequent.setRecurringTransaction(newExpense);

				transactionDao.create(newExpenseForSubsequent);
				persistMetadata(newExpenseForSubsequent);
			}

			dueDate = dueDate.plus(recurringUnit.getUnits(), recurringUnit.getUnitType());

			if (newExpense.getEndDate() != null && dueDate.isAfter(newExpense.getEndDate())) {
				break;
			}
		}
	}

	public V update(V transaction) {
		V updated = transactionDao.update(transaction);
		persistMetadata(updated);
		return updated;
	}

	public V create(V transaction) {
		V created = transactionDao.create(transaction);
		persistMetadata(created);
		return created;
	}

	public V updateTransaction(V expense) throws Exception {
		attachDocumentForUpdate(expense);
		transactionDao.update(expense);
		persistMetadata(expense);
		handleRecurringForUpdate(expense);
		hydrateTransaction(expense);
		return expense;
	}

	private void handleRecurringForUpdate(V updatedExpense) {
		if (updatedExpense.getRecurringType() != null) {
			List<V> expenses = transactionDao.getPastDate(LocalDate.now(), updatedExpense);
			for (V expense : expenses) {
				expense.setAmount(updatedExpense.getAmount());
				transactionDao.update(expense);
			}
		}
	}

	public void deleteTransaction(Long id) {
		V expense = transactionDao.getById(id);
		EntityMetadataType type = metadataType(expense);
		if (expense.getRecurringType() != null) {
			transactionDao.deleteTransactions(id);
			if (transactionDao.getForRecurring(expense).size() > 0) {
				expense.setDeleted(true);
				transactionDao.update(expense);
			} else {
				entityMetadataService.deleteForEntity(type, String.valueOf(expense.getId()));
				transactionDao.delete(expense);
			}
		} else {
			entityMetadataService.deleteForEntity(type, String.valueOf(expense.getId()));
			transactionDao.delete(expense);
		}
	}

	public void createRecurringTransactions(LocalDate startOfWeek, Transaction currentRecurringExpense) {
		List<V> recurringExpenses = transactionDao.getAllRecurring(true);
		hydrateTransactions(recurringExpenses);

		for (V recurringExpense : recurringExpenses) {
			if (currentRecurringExpense != null && recurringExpense.getId() == currentRecurringExpense.getId()) {
				continue;
			}

			LocalDate dueDate = recurringExpense.getStartDate();
			while (DateUtil.getMonday(dueDate).isBefore(startOfWeek)) {
				RecurringUnit recurringUnit = RecurringUnit
					.valueOf(recurringExpense.getRecurringType().getDescriptionUpper());
				dueDate = dueDate.plus(recurringUnit.getUnits(), recurringUnit.getUnitType());
			}

			if (DateUtil.getMonday(dueDate).isEqual(startOfWeek) && checkEndDate(recurringExpense, dueDate)) {
				V newExpense = createTransaction();
				newExpense.setEntryType(recurringExpense.getEntryType());
				newExpense.setAmount(recurringExpense.getAmount());
				newExpense.setDueDate(dueDate);
				newExpense.setRecurringTransaction(recurringExpense);
				newExpense.setMetaData(recurringExpense.getMetaData());
				newExpense.setNotes(recurringExpense.getNotes());

				transactionDao.create(newExpense);
				persistMetadata(newExpense);
			}
		}
	}

	private boolean checkEndDate(V recurringExpense, LocalDate dueDate) {
		return recurringExpense.getEndDate() == null || dueDate.isBefore(recurringExpense.getEndDate());
	}

	public int countForWeek(LocalDate startOfWeek) throws Exception {
		return transactionDao.getForWeek(startOfWeek).size();
	}

	public List<V> getPastDateList(LocalDate date) {
		List<V> results = transactionDao.getPastDate(date);
		hydrateTransactions(results);
		return results;
	}

	protected void hydrateTransaction(V transaction) {
		if (transaction == null) {
			return;
		}
		hydrateTransactions(List.of(transaction));
	}

	protected void hydrateTransactions(List<V> transactions) {
		if (transactions == null || transactions.isEmpty()) {
			return;
		}
		EntityMetadataType type = metadataType(transactions.get(0));
		entityMetadataService.hydrateList(type, transactions, t -> String.valueOf(t.getId()),
			(entity, entityMetadata, objectMap, stringMap) -> {
				entity.setEntityMetadata(entityMetadata);
				entity.setMetaData(objectMap);
			});
		for (V transaction : transactions) {
			if (transaction.getDocument() != null) {
				documentService.hydrateDocument(transaction.getDocument());
			}
		}
	}

	protected void persistMetadata(V transaction) {
		if (transaction == null || transaction.getId() == 0) {
			return;
		}
		entityMetadataService.replace(metadataType(transaction), String.valueOf(transaction.getId()),
			transaction.getMetaData());
	}

	protected EntityMetadataType metadataType(V transaction) {
		return transaction instanceof Income ? EntityMetadataType.INCOME : EntityMetadataType.EXPENSE;
	}

	abstract int countForWeekForAll(LocalDate startOfWeek) throws Exception;

	abstract V createTransaction();

	abstract void initialiseWeek(LocalDate localDate, Transaction currentRecurringTransaction) throws Exception;

	abstract int getPastDate(LocalDate date) throws Exception;

}
