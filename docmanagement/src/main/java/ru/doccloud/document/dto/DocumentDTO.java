package ru.doccloud.document.dto;

import org.jtransfo.DomainClass;

/**
 * @author Andrey Kadnikov
 */
@DomainClass("ru.doccloud.document.model.Document")
public class DocumentDTO extends AbstractDocumentDTO{

    public DocumentDTO() {

    }

    public DocumentDTO(String title, String type, String author) {
        super(title, type, author);
    }

    @Override
    public String toString() {
        return "DocumentDTO " + super.toString();
    }
}
