package com.favorsoft.mplatform.exception;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.Principal;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.favorsoft.mplatform.cdn.domain.ErrorHistory;
import com.favorsoft.mplatform.cdn.repository.jpa.ErrorHistoryRepository;

@RestControllerAdvice
public class ExceptionHandlerCustom {

	@Autowired
	private ErrorHistoryRepository errorHistoryRepository;
	
	// 최상위 Exception handler
	@ExceptionHandler({ Throwable.class, Exception.class, RuntimeException.class})
	@ResponseBody
	public ErrorResponse handleException(Throwable source, HttpServletRequest request, HttpServletResponse response, Principal principal) {
		// Convert StackTrace to String
		String stackTraceStr = ConvertStackTraceToString(source); 
		
		// Create & Save ErrorHistory
		createErrorHistory(MplatformErrorCode.INTERNAL_SERVER_ERROR.getErrorCode(), source.getMessage(), stackTraceStr, "ADMIN");
				
		// Build Error Response
		return buildError(source, MplatformErrorCode.INTERNAL_SERVER_ERROR, 
        		source.getMessage(), stackTraceStr, request, response);
    }
	
	// Mplatform Custom Exception Handler
	@ExceptionHandler(value = MplatformRuntimeException.class)
	@ResponseBody
	public ErrorResponse mplatformRuntimeExceptionHandler(MplatformRuntimeException source, HttpServletRequest request, HttpServletResponse response, Principal principal) {
		
		// Convert StackTrace to String
		String stackTraceStr = ConvertStackTraceToString(source);
		
		// Create & Save ErrorHistory
		createErrorHistory(source.getErrorCode(), source.getMessage(), stackTraceStr, principal.getName());

		// Build Error Response
		return buildError(source, source.getMplatformErrorCode(), source.getMessage(), stackTraceStr, request, response);
	}
	
	private void createErrorHistory(String errorCode, String errorMsg, String stackTraceStr, String principal) {
		// Create ErrorHistory
		ErrorHistory errorHis = new ErrorHistory(errorCode, errorMsg, stackTraceStr, principal);

		// Save ErrorHistory
		errorHistoryRepository.save(errorHis);
	}
	private String ConvertStackTraceToString(Throwable e) {
		StringWriter sw = new StringWriter(); 
		e.printStackTrace(new PrintWriter(sw)); 
		return sw.toString(); 
	}
	
	private ErrorResponse buildError(Throwable source, MplatformErrorCode errorCode, String errorMsg, String stackTrace,
			HttpServletRequest request, HttpServletResponse response) {
		
		if (response != null) {
			response.setStatus(errorCode.getHttpStatusCode());
		}
		ErrorResponse errorResponse = new ErrorResponse(errorCode.getErrorCode(), errorMsg);
		return errorResponse;
	}
}
