package com.example.filestorage.exceptions;


public class PictureStorageException extends RuntimeException{
    public PictureStorageException(String message){
        super(message);
    }
    public PictureStorageException(String message, Throwable cause){
        super(message, cause);

    }
}
