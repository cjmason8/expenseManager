package au.com.mason.expensemanager.processor;

import jakarta.mail.Message;

import org.springframework.stereotype.Component;

import au.com.mason.expensemanager.domain.RefData;

@Component
public class DingleyElectricityProcessor extends LumoProcessor {
	@Override
	public void execute(Message message, RefData refData) throws Exception {
		process(message, refData, "DingleyElec-");
	}
}
