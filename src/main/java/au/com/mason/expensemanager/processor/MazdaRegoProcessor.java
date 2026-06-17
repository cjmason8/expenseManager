package au.com.mason.expensemanager.processor;

import org.springframework.stereotype.Component;

@Component
public class MazdaRegoProcessor extends VicRoadsProcessor {

	@Override
	String getFilePrefix() {
		return "MazdaRego";
	}

}
