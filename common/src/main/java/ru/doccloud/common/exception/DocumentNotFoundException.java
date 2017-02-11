package ru.doccloud.common.exception;

/**
 * @author Andrey Kadnikov
 */
public class DocumentNotFoundException extends RuntimeException {

    public DocumentNotFoundException(String message) {
        super(message);
    }
}
