package it.lacksfer.application.exception;

public class FileStorageException extends RuntimeException{
    public FileStorageException(String message, Throwable cause){
        super(message, cause);
    }
}
