package au.com.mason.expensemanager.processor;

import au.com.mason.expensemanager.domain.RefData;
import javax.mail.Message;
import org.springframework.stereotype.Component;

@Component
public class DingleyGasProccesor extends LumoProccesor {

	@Override
	public void execute(Message message, RefData refData) throws Exception {
		process(message, refData, "DingleyGas-");
	}
}
