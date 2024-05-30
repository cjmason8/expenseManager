package au.com.mason.expensemanager.processor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import javax.mail.Message;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import au.com.mason.expensemanager.domain.Document;
import au.com.mason.expensemanager.domain.Expense;
import au.com.mason.expensemanager.domain.Notification;
import au.com.mason.expensemanager.domain.RefData;
import au.com.mason.expensemanager.service.DocumentService;
import au.com.mason.expensemanager.service.ExpenseService;
import au.com.mason.expensemanager.service.NotificationService;
import au.com.mason.expensemanager.util.DateUtil;

public abstract class Processor {
	
	private static Logger LOGGER = LogManager.getLogger(Processor.class);
	
	@Autowired
	protected ExpenseService expenseService;
	
	@Autowired
	protected DocumentService documentService;
	
	@Autowired
	protected NotificationService notificationService;
	
	protected boolean checkWithinBoundary(LocalDate dueDate, Expense expense) {
		return expense.getDueDate().isAfter(dueDate.minusDays(10)) && expense.getDueDate().isBefore(dueDate.plusDays(10));
	}
	
	protected void addExpense(LocalDate dueDate, BigDecimal amount, Document document, Expense expense) {
		expense.setDueDate(dueDate);
		expense.setAmount(amount);
		if (document != null) {
			expense.setDocument(document);
		}
		expenseService.update(expense);
		
		Notification notification = new Notification();
		notification.setExpense(expense);
		notification.setMessage("Updated expense with new details");
		
		LOGGER.info("updated new expense for " + expense.getEntryType().getDescription() + " and due date" + expense.getDueDate() +" and id " + expense.getId());
		
		notificationService.create(notification);
	}
	
	protected void updateExpense(RefData refData, LocalDate dueDate, String amount, Document document, String notes) throws Exception {
		expenseService.initialiseWeek(DateUtil.getMonday(dueDate), null);
		expenseService.initialiseWeek(DateUtil.getMonday(dueDate.minusDays(7)), null);
		expenseService.initialiseWeek(DateUtil.getMonday(dueDate.plusDays(7)), null);
		List<Expense> expenses = expenseService.findExpense(refData);
		Expense reqExpense = null;
		if (expenses.size() > 0) {
			for (Expense expense : expenses) {
				if (checkWithinBoundary(dueDate, expense)) {
					reqExpense = expense;
					break;
				}
			}
		}
		BigDecimal reqAmount = new BigDecimal(amount.replace("$", "").replace(",", ""));
		if (expenses.size() == 0 || reqExpense == null) {
			createExpense(refData, dueDate, document, notes, reqAmount);
		}
		else {
			if (notes != null) {
				reqExpense.setNotes(notes);
			}
			addExpense(dueDate, reqAmount, document, reqExpense);
		}		
	}

	protected void createExpense(RefData refData, LocalDate dueDate, Document document, String notes,
			BigDecimal reqAmount) {
		Expense newExpense = new Expense();
		newExpense.setDueDate(dueDate);
		newExpense.setAmount(reqAmount);
		if (document != null) {
			newExpense.setDocument(document);
		}
		newExpense.setEntryType(refData);
		newExpense.setNotes(notes);
		expenseService.create(newExpense);
		
		Notification notification = new Notification();
		notification.setExpense(newExpense);
		notification.setMessage("Created new expense");
		
		LOGGER.info("created new expense for " + newExpense.getEntryType().getDescription() + " and due date" + newExpense.getDueDate() +" and id " + newExpense.getId());
		
		notificationService.create(notification);
	}
	
	protected void updateExpense(RefData refData, LocalDate dueDate, String amount, Document document) throws Exception {
		updateExpense(refData, dueDate, amount, document, null);
	}
	
	public abstract void execute(Message message, RefData refData) throws Exception;
}
