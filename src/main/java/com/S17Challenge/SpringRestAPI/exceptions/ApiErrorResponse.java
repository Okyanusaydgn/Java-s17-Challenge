package com.S17Challenge.SpringRestAPI.exceptions;

import lombok.AllArgsConstructor;
import lombok.Data;

// Bu sınıf hata durumlarında dönecek yanıtın nasıl olacağını söylüyor.

@Data
@AllArgsConstructor
public class ApiErrorResponse {

    private Integer status; // HTTP hata kodu
    private String message; // Hata mesajı
    private Long timestamp; // Hata oluştuğu zamanın zamanı

}
