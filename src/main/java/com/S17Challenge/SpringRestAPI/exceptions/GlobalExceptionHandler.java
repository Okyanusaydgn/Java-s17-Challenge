package com.S17Challenge.SpringRestAPI.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.logging.ErrorManager;

@Slf4j // lombok kütüphanesi tarafından sağlanıyor. log nesnesi eklememizi sağlar. logları dinamik olarak kontrol edebilmemizi sağlar.
@ControllerAdvice // bu anatasyon sayesinde sınıfın global hata yöneticisi olarak işlev görmesini sağlar.
public class GlobalExceptionHandler {


    @ExceptionHandler // bu anatasyon bu metodun yalnızca apiException türündeki istisnaları yakalayacağını belirtir.
    public ResponseEntity<ApiErrorResponse> handleApiException(ApiExceptions apiExceptions) {

        log.error("API exception occured! Exception details: ", apiExceptions.getMessage());
        ApiErrorResponse apiErrorResponse = new ApiErrorResponse(
                apiExceptions.getHttpStatus().value(),
                apiExceptions.getMessage(),
                System.currentTimeMillis()
        );
        return new ResponseEntity<>(apiErrorResponse,apiExceptions.getHttpStatus());
    }

    @ExceptionHandler
    public ResponseEntity<ApiErrorResponse> handleAllExceptions(Exception exception){
        log.error("Exception occured! Exception details: ", exception.getMessage());
        ApiErrorResponse apiErrorResponse = new ApiErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                exception.getMessage(),
                System.currentTimeMillis()
        );
        return new ResponseEntity<>(apiErrorResponse,HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
