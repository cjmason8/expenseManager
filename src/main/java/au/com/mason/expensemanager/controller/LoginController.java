package au.com.mason.expensemanager.controller;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Enumeration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import au.com.mason.expensemanager.dto.LoginInput;
import au.com.mason.expensemanager.service.UserAuthenticationService;

@RestController
@CrossOrigin
public class LoginController {
	
	@Autowired
	private UserAuthenticationService userAuthenticationService;
	
	private static Logger LOGGER = LogManager.getLogger(LoginController.class);
	
	@RequestMapping(value = "/login", method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
	String login(@RequestBody LoginInput loginInput) {
		try {
			LOGGER.info("entering LoginController login - " + loginInput.getUserName());
			String login = userAuthenticationService.login(loginInput);
			LOGGER.info("leaving LoginController login - " + loginInput.getUserName());
			return login;
		}
		catch (Exception e) {
			LOGGER.error("Error logging in", e);
			return "{\"error\":\"" + e.getMessage() + "\"}";
		}
    }
	
	@RequestMapping(value = "/login", method = RequestMethod.GET, produces = "application/json")
	String loginGet(HttpServletRequest request) {
		LOGGER.error("Received invalid GET request for /login", new RuntimeException());
		LOGGER.info("Method: " + request.getMethod());

		Enumeration<String> headerNames = request.getHeaderNames();
		while(headerNames.hasMoreElements()) {
		  String headerName = headerNames.nextElement();
		  LOGGER.info("Header Name - " + headerName + ", Value - " + request.getHeader(headerName));
		}

		Enumeration<String> params = request.getParameterNames(); 
		while(params.hasMoreElements()){
		 String paramName = params.nextElement();
		 LOGGER.info("Parameter Name - "+paramName+", Value - "+request.getParameter(paramName));
		}
		
		return "{\"error\":\"Invalid login\"}";
    }
}
