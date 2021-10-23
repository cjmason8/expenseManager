package au.com.mason.expensemanager.processor;

import org.springframework.stereotype.Component;

@Component
public class VeradaRegoProcessor extends VicRoadsProcessor {

	@Override
	String getFilePrefix() {
		return "VeradaRego";
	}

}
