package au.com.mason.expensemanager.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import au.com.mason.expensemanager.dto.EmailTrawlerResponseDto;
import au.com.mason.expensemanager.robot.EmailTrawler;
import au.com.mason.expensemanager.robot.EmailTrawlerResult;

@RestController
public class EmailTrawlerController {

	private static final Logger LOGGER = LogManager.getLogger(EmailTrawlerController.class);

	@Autowired
	private EmailTrawler emailTrawler;

	@RequestMapping(value = "/runEmailTrawler", method = RequestMethod.GET, produces = "application/json")
	EmailTrawlerResponseDto runEmailTrawler() {
		LOGGER.info("Received request to run EmailTrawler");

		EmailTrawlerResult result = emailTrawler.check();

		LOGGER.info("EmailTrawler request completed: success={}, summary={}", result.isSuccess(), result.summary());

		return new EmailTrawlerResponseDto(result.isSuccess(), result.summary(), result.getDetails(),
			result.getError());
	}

}
