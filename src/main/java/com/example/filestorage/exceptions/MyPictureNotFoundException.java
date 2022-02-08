package com.example.filestorage.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class MyPictureNotFoundException extends RuntimeException{
    public MyPictureNotFoundException(String message) {
        super(message);
    }

    public MyPictureNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
