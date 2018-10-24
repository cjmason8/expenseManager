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
	
	protected void addExpense(LocalDate dueDate, String amount, Document document, Expense expense) {
		expense.setDueDate(dueDate);
		expense.setAmount(new BigDecimal(amount));
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
	
	protected void updateExpense(RefData refData, LocalDate dueDate, String amount, Document document) throws Exception {
		expenseService.initialiseWeek(dueDate);
		expenseService.initialiseWeek(dueDate.minusDays(7));
		expenseService.initialiseWeek(dueDate.plusDays(7));
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
		if (expenses.size() == 0 || reqExpense == null) {
			Expense newExpense = new Expense();
			newExpense.setDueDate(dueDate);
			newExpense.setAmount(new BigDecimal(amount.replace("$", "")));
			if (document != null) {
				newExpense.setDocument(document);
			}
			newExpense.setEntryType(refData);
			expenseService.create(newExpense);
			
			Notification notification = new Notification();
			notification.setExpense(newExpense);
			notification.setMessage("Created new expense");
			
			LOGGER.info("created new expense for " + newExpense.getEntryType().getDescription() + " and due date" + newExpense.getDueDate() +" and id " + newExpense.getId());
			
			notificationService.create(notification);
		}
		else {
			addExpense(dueDate, amount, document, expenses.get(0));
		}
	}
	
	public abstract void execute(Message message, RefData refData) throws Exception;
}