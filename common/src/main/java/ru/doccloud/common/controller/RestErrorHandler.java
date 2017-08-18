package ru.doccloud.common.controller;

import java.util.List;

import org.everit.json.schema.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import ru.doccloud.common.dto.RestError;
import ru.doccloud.common.exception.DocumentNotFoundException;
import ru.doccloud.common.exception.TypeNotFoundException;

/**
 * @author Andrey Kadnikov
 */
@ControllerAdvice
public class RestErrorHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestErrorHandler.class);

    @ExceptionHandler(DocumentNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public void handleTodoNotFound(DocumentNotFoundException ex) {
        LOGGER.info("Todo entry was not found. Returning HTTP status code 404");
    }
    
    @ExceptionHandler(TypeNotFoundException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public RestError handleTypeNotFoundError(TypeNotFoundException ex) {
        LOGGER.info("Type not found error");

        RestError.Builder error = RestError.getBuilder()
                .status(HttpStatus.BAD_REQUEST)
                .code(HttpStatus.BAD_REQUEST.value())
                .message(ex.getMessage());

       RestError validationError = error.build();

        LOGGER.info("Returning validation error: {}", validationError);

        return validationError;
    }
    
    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public RestError handleJsonDataValidationException(ValidationException ex) {
        LOGGER.info("Document data can't be validated by type schema");

        RestError.Builder error = RestError.getBuilder()
                .status(HttpStatus.BAD_REQUEST)
                .code(HttpStatus.BAD_REQUEST.value())
                .message(ex.toString());
        List<ValidationException> fieldErrors = ex.getCausingExceptions();

        for (ValidationException fieldError: fieldErrors) {
            error.validationError(fieldError.getKeyword(), fieldError.getErrorMessage(), fieldError.getLocalizedMessage());
        }

       RestError validationError = error.build();

        LOGGER.info("Returning validation error: {}", validationError);

        return validationError;
    }
    
    
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public RestError handleValidationError(MethodArgumentNotValidException ex) {
        LOGGER.info("Handling validation error");

        RestError.Builder error = RestError.getBuilder()
                .status(HttpStatus.BAD_REQUEST)
                .code(HttpStatus.BAD_REQUEST.value())
                .message(ex.getMessage());

        List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors();

        for (FieldError fieldError: fieldErrors) {
            error.validationError(fieldError.getField(), fieldError.getCode(), fieldError.getDefaultMessage());
        }

        RestError validationError = error.build();

        LOGGER.info("Returning validation error: {}", validationError);

        return validationError;
    }
}
