package au.com.mason.expensemanager.processor;

import org.springframework.stereotype.Component;

@Component
public class ForesterRegoProcessor extends VicRoadsProcessor {

	@Override
	String getFilePrefix() {
		return "ForesterRego";
	}

}
