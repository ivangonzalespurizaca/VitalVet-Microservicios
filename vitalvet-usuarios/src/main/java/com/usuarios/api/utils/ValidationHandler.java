package com.usuarios.api.utils;

import org.springframework.security.access.AccessDeniedException;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

@RestControllerAdvice
public class ValidationHandler {

	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiResponse<?>> handleValidationErrors(MethodArgumentNotValidException e){
		Map<String, String> errores=new HashMap<>();
		e.getBindingResult().getFieldErrors().forEach(error -> {
            errores.put(error.getField(), error.getDefaultMessage());
        });
		ApiResponse<Map<String, String>> response =
                new ApiResponse<>(false, "Errores de validación", errores);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
	}
	@ExceptionHandler(ModeloNotFoundException.class)
	public ResponseEntity<ApiResponse<?>> manejarModeloNotFoundException(ModeloNotFoundException ex, WebRequest request) {
		ApiResponse<String> response =
                new ApiResponse<>(false, ex.getMessage(),null);
		return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
	}
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<?>> handleBadCredentialsException(BadCredentialsException ex) {
        ApiResponse<String> response = new ApiResponse<>(false, "Error de autenticación", ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }
	//ERROR 500 internal server error
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse<?>> handleGenericException(Exception ex) {
		ApiResponse<String> response =
                new ApiResponse<>(false, "Error interno del servidor", ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
	//Manejo de errores de negocio 409
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<?>> handleBusinessException(BusinessException ex) {
    	ApiResponse<String> response = new ApiResponse<>(false, ex.getMessage(), null);
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);// 409
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<?>> handleAccessDeniedException(AccessDeniedException ex) {
        ApiResponse<String> response = new ApiResponse<>(
                false,
                "No tienes permisos suficientes para acceder a este recurso.",
                "Access Denied"
        );
        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN); // Código 403
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<?>> handleIllegalArgumentException(IllegalArgumentException ex) {
        ApiResponse<String> response = new ApiResponse<>(false, ex.getMessage(), null);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST); // Código 400
    }
	
}

