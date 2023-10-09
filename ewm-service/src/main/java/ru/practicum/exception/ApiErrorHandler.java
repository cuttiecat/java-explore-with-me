package ru.practicum.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import javax.validation.ValidationException;
import java.time.LocalDateTime;


@Slf4j
@RestControllerAdvice
public class ApiErrorHandler {


    @ExceptionHandler({MethodArgumentNotValidException.class,
            MethodArgumentTypeMismatchException.class,
            ValidationException.class,
            MissingServletRequestParameterException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError badRequestException(final RuntimeException e) {
        log.info(HttpStatus.BAD_REQUEST + " {}", e.getMessage());
        return ApiError.builder()
                .status(HttpStatus.BAD_REQUEST)
                .reason("Incorrectly made request.")
                .message(e.getLocalizedMessage())
                .errorTimestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleCategoryIsNotEmptyException(CategoryIsNotEmptyException e) {
        return ApiError.builder()
                .status(HttpStatus.CONFLICT)
                .reason("Категория не пустая")
                .message(e.getMessage())
                .errorTimestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleAlreadyExistsException(AlreadyExistsException e) {
        return ApiError.builder()
                .status(HttpStatus.FORBIDDEN)
                .reason("Уже существует.")
                .message(e.getMessage())
                .errorTimestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFoundException(NotFoundException e) {
        return ApiError.builder()
                .status(HttpStatus.NOT_FOUND)
                .reason("Нужный объект не найден.")
                .message(e.getMessage())
                .errorTimestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleAccessDeniedException(AccessDeniedException e) {
        return ApiError.builder()
                .status(HttpStatus.FORBIDDEN)
                .reason("Для запрошенной операции условия не выполнены.")
                .message(e.getMessage())
                .errorTimestamp(LocalDateTime.now())
                .build();
    }


    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleThrowable(Exception e) {
        return ApiError.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .reason("Internal server error.")
                .message(e.getClass() + " - " + e.getMessage())
                .errors(e.getStackTrace())
                .errorTimestamp(LocalDateTime.now())
                .build();
    }

}
