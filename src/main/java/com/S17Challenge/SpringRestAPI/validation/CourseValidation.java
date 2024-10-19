package com.S17Challenge.SpringRestAPI.validation;

import com.S17Challenge.SpringRestAPI.exceptions.ApiExceptions;
import org.springframework.http.HttpStatus;

// static kullanarak sınıfın bir örneğine ihtiya. duymadan doğrudan erişmemizi sağlar. örnek "CourseValidation.checkName("Mathematics");"
public class CourseValidation {

    public static void checkName(String name) {
        if (name == null || name.isEmpty()) {
            throw new ApiExceptions("Name cannot be null or empty", HttpStatus.BAD_REQUEST);
        }
    }

    public static void checkCredit(Integer credit){
        if (credit == null || credit < 0 || credit > 4){
            throw new ApiExceptions("Credit must be between 0 and 4!",HttpStatus.BAD_REQUEST);
        }
    }

    public static void checkId(Integer id){
        if (id == null || id < 0){
            throw new ApiExceptions("ID cannot be null or less than zero ID = "+id,HttpStatus.BAD_REQUEST);
        }
    }

}
