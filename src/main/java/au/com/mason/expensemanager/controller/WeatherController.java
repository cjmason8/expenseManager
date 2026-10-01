package au.com.mason.expensemanager.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import au.com.mason.expensemanager.dto.WeatherForecastDto;
import au.com.mason.expensemanager.service.WeatherService;

@RestController
@CrossOrigin
public class WeatherController {

	private static final Logger LOGGER = LogManager.getLogger(WeatherController.class);

	@Autowired
	private WeatherService weatherService;

	@RequestMapping(value = "/weather/forecast", method = RequestMethod.GET, produces = "application/json")
	WeatherForecastDto getForecast() throws Exception {
		LOGGER.info("entering WeatherController getForecast");
		WeatherForecastDto forecast = weatherService.getForecast();
		LOGGER.info("leaving WeatherController getForecast");

		return forecast;
	}

}
