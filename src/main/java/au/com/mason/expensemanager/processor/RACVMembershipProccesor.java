package au.com.mason.expensemanager.processor;

import javax.mail.Message;

import org.springframework.stereotype.Component;

import au.com.mason.expensemanager.domain.RefData;

@Component
public class RACVMembershipProccesor extends RACVProcessor {
	
	@Override
	public void execute(Message message, RefData refData) throws Exception {
		process(message, refData, "RACVMembership-");
	}

}
