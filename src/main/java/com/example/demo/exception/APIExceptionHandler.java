package com.example.demo.exception;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
@ControllerAdvice
public class APIExceptionHandler {
	
	@ExceptionHandler(value= {APIException.class})
	ResponseEntity<Object> handleApiRequestException (APIException e){
		ExceptionPayload exception = new  ExceptionPayload(e.getMessage(),
				 HttpStatus.BAD_REQUEST, ZonedDateTime.now(ZoneId.of("UTC")));
		return new ResponseEntity<>(exception,HttpStatus.BAD_REQUEST) ;
		
	}

}
