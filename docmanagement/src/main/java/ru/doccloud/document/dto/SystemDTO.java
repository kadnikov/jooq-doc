package ru.doccloud.document.dto;

import org.jtransfo.DomainClass;

/**
 * @author Ilya Ushakov
 */
@DomainClass("ru.doccloud.document.model.SystemEntity")
public class SystemDTO extends AbstractDocumentDTO{



    private String symbolicName;
    public SystemDTO() {

    }

    public SystemDTO(String title, String type, String author) {
        super(title, type, author);
    }



    @Override
    public String toString() {
        return "SystemDTO{" +
                "symbolicName='" + symbolicName + '\'' +
                '}';
    }
}