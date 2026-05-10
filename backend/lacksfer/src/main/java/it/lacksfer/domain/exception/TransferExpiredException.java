package it.lacksfer.domain.exception;

public class TransferExpiredException extends RuntimeException {
    public TransferExpiredException(String message){
        super(message);
    }
}
