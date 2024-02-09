package au.com.mason.expensemanager.controller;

import au.com.mason.expensemanager.dto.EmailTrawlerResponseDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import au.com.mason.expensemanager.robot.EmailTrawler;

@RestController
public class EmailTrawlerController {
	
	@Autowired
	private EmailTrawler emailTrawler;
	
	@RequestMapping(value = "/runEmailTrawler", method = RequestMethod.GET, produces = "application/json")
	EmailTrawlerResponseDto expensesForWeek() {
		
		emailTrawler.check();
		
		return new EmailTrawlerResponseDto("true");
    }

}
