package ru.doccloud.common.exception;

/**
 * @author Petri Kainulainen
 */
public class TodoNotFoundException extends RuntimeException {

    public TodoNotFoundException(String message) {
        super(message);
    }
}
