package com.S17Challenge.SpringRestAPI.exceptions;


import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

// Bu sınıf özel hata fırlatmasını sağlıyor. RuntimeException sınıfını genişletip httpStatus  ile birlikte özel mesajlar gönderiyor.
@Getter
@Setter

public class ApiExceptions extends RuntimeException {

    private HttpStatus httpStatus;


    public ApiExceptions(String message, HttpStatus httpStatus) {
        super(message);  // RunTimeException sınıfnının hata mesajlarını çağrıyor.
        this.httpStatus = httpStatus;
    }

}
