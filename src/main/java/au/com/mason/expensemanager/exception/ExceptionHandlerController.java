package au.com.mason.expensemanager.exception;

import jakarta.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import au.com.mason.expensemanager.dto.ErrorDto;

@ControllerAdvice
public class ExceptionHandlerController {

	private static Logger LOGGER = LogManager.getLogger(ExceptionHandlerController.class);

    @ExceptionHandler(value = {Exception.class, RuntimeException.class})
    public ResponseEntity<ErrorDto> defaultErrorHandler(HttpServletRequest request, Exception e) {
    	
    	LOGGER.error("Unhandled Exception - " + request.getServletPath(), e);
    	
    	return new ResponseEntity<>(new ErrorDto(ExceptionUtils.getRootCauseMessage(e), ExceptionUtils.getStackTrace(e)), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
