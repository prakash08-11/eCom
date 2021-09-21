package com.example.demo.exception;

import java.time.ZonedDateTime;

import org.springframework.http.HttpStatus;


public class ExceptionPayload {
	
	public String getMessage() {
		return message;
	}

	public HttpStatus getHttpStatus() {
		return httpStatus;
	}
	public ZonedDateTime getTimeStamp() {
		return timeStamp;
	}
	public ExceptionPayload(String message, HttpStatus httpStatus, ZonedDateTime timeStamp) {
		super();
		this.message = message;
		this.httpStatus = httpStatus;
		this.timeStamp = timeStamp;
	}
	private final String message;
	private final HttpStatus httpStatus;
	private final  ZonedDateTime timeStamp;

	

}
