package ru.mishazx.systemotpjava.exceptions;

public class IllegalOperationException extends RuntimeException {
    public IllegalOperationException(String message) {
        super(message);
    }
}