package com.baseplus.modules.registros.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;

import com.baseplus.core.exception.BusinessException;
import com.baseplus.shared.dto.ApiResponse;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(assignableTypes = RegistroPublicoController.class)
public class RegistroPublicoExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(RegistroPublicoExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException exception) {
        log.warn("Public registros business exception: {}", exception.getMessage());
        ApiResponse<Void> response = ApiResponse.failure(exception.getMessage(), exception.getErrors());
        return ResponseEntity.status(exception.getStatus()).body(response);
    }

    @ExceptionHandler({MultipartException.class, MaxUploadSizeExceededException.class})
    public ResponseEntity<ApiResponse<Void>> handleMultipartException(Exception exception) {
        log.warn("Public registros multipart exception: {}", exception.getMessage());
        ApiResponse<Void> response = ApiResponse.failure(
                "Arquivo invalido.",
                "O upload nao pode ser processado ou excede o tamanho maximo permitido."
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
}
