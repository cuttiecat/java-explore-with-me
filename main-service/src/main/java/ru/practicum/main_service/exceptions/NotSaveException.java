package ru.practicum.main_service.exceptions;

public class NotSaveException extends RuntimeException {

    public NotSaveException(String message) {
        super(message);
    }

}