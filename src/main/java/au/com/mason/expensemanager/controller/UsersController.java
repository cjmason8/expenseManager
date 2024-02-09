package au.com.mason.expensemanager.controller;

import au.com.mason.expensemanager.dto.AuthenticateResponseDto;
import au.com.mason.expensemanager.service.UserAuthenticationService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping
@RestController
public class UsersController {
	
	private static final Logger LOGGER = LogManager.getLogger(UsersController.class);

	@Autowired
	private UserAuthenticationService userAuthenticationService;

	@GetMapping(value = "/users/{token}/authenticate")
	AuthenticateResponseDto authenticate(@PathVariable String token) throws Exception {
		LOGGER.info("entering UsersController authenticate");
		JSONObject json = userAuthenticationService.authenticate(token);
		String status = json.getString("tokenStatus");
		LOGGER.info("leaving UsersController authenticate");

		return status.equals("valid") ? new AuthenticateResponseDto("success", json.getString("user"))
				: new AuthenticateResponseDto("failed", null);
	}
}