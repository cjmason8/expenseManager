package au.com.mason.expensemanager.processor;

import au.com.mason.expensemanager.domain.RefData;
import javax.mail.Message;
import org.springframework.stereotype.Component;

@Component
public class DingleyElectricityProcessor extends LumoProccesor {
    @Override
    public void execute(Message message, RefData refData) throws Exception {
        process(message, refData, "DingleyElec-");
    }
}
