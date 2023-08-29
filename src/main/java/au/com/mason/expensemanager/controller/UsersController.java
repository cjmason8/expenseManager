package au.com.mason.expensemanager.controller;

import au.com.mason.expensemanager.dto.AuthServiceResponseDto;
import au.com.mason.expensemanager.dto.AuthenticateResponseDto;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import au.com.mason.expensemanager.service.UserAuthenticationService;

@RequestMapping
@RestController
public class UsersController {
	
	private static Logger LOGGER = LogManager.getLogger(UsersController.class);

	@Autowired
	private UserAuthenticationService userAuthenticationService;

	@RequestMapping(value = "/users/{token}/authenticate", method = RequestMethod.GET)
	AuthenticateResponseDto authenticate(@PathVariable String token) throws Exception {
		LOGGER.info("entering UsersController authenticate");
		AuthServiceResponseDto authServiceResponse = userAuthenticationService.authenticate(token);
		LOGGER.info("leaving UsersController authenticate");

		return authServiceResponse.isValid()
				? new AuthenticateResponseDto("success", authServiceResponse.getUser())
				: new AuthenticateResponseDto("failed", null);
	}
}