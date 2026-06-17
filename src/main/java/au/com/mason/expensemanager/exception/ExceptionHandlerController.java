package au.com.mason.expensemanager.exception;

import jakarta.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.NoHandlerFoundException;

import au.com.mason.expensemanager.dto.ErrorDto;

@ControllerAdvice
public class ExceptionHandlerController {

	private static Logger LOGGER = LogManager.getLogger(ExceptionHandlerController.class);

	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	public ResponseEntity<ErrorDto> methodNotSupportedHandler(HttpServletRequest request,
		HttpRequestMethodNotSupportedException e) {
		LOGGER.debug("Method not supported - {} {}", e.getMethod(), request.getServletPath());
		return new ResponseEntity<>(new ErrorDto(e.getMessage(), null), HttpStatus.METHOD_NOT_ALLOWED);
	}

	@ExceptionHandler(NoHandlerFoundException.class)
	public ResponseEntity<ErrorDto> notFoundHandler(HttpServletRequest request, NoHandlerFoundException e) {
		LOGGER.debug("No handler found - {}", request.getServletPath());
		return new ResponseEntity<>(new ErrorDto(e.getMessage(), null), HttpStatus.NOT_FOUND);
	}

	@ExceptionHandler(value = {Exception.class, RuntimeException.class})
	public ResponseEntity<ErrorDto> defaultErrorHandler(HttpServletRequest request, Exception e) {

		if (!e.getMessage().contains("No static resource")) {
			LOGGER.error("Unhandled Exception - " + request.getServletPath(), e);
		}

		return new ResponseEntity<>(
			new ErrorDto(ExceptionUtils.getRootCauseMessage(e), ExceptionUtils.getStackTrace(e)),
			HttpStatus.INTERNAL_SERVER_ERROR);
	}
}
