package ru.doccloud.common.exception;

/**
 * @author Andrey Kadnikov
 */
public class TypeNotFoundException extends RuntimeException {

    public TypeNotFoundException(String message) {
        super(message);
    }
}
