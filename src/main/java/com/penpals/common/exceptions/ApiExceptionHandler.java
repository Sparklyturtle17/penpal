package com.penpals.common.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

	@ExceptionHandler(NotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	ApiError notFound(NotFoundException e) { return new ApiError(e.getMessage()); }

	@ExceptionHandler(IllegalArgumentException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	ApiError badRequest(IllegalArgumentException e) { return new ApiError(e.getMessage()); }

	@ExceptionHandler(MethodArgumentNotValidException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	ApiError invalidBody(MethodArgumentNotValidException e) {
		var f = e.getBindingResult().getFieldError();
		return new ApiError(f != null ? f.getField() + ": " + f.getDefaultMessage() : "Invalid request");
	}

	public record ApiError(String message) {}
}