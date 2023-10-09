package ru.practicum.main_service.exception;

public class NotSaveException extends RuntimeException {

    public NotSaveException(String message) {
        super(message);
    }

}