package ru.doccloud.service.document.dto;

import org.jtransfo.DomainClass;

/**
 * @author Andrey Kadnikov
 */
@DomainClass("ru.doccloud.document.model.Document")
public class DocumentDTO extends AbstractDocumentDTO{

    private String fileStorage;
    private String baseType;

    public DocumentDTO() {
        super();
    }

    public DocumentDTO(String title, String type, String author) {
        super(title, type, author);
    }

    public String getFileStorage() {
        return fileStorage;
    }

    public void setFileStorage(String fileStorage) {
        this.fileStorage = fileStorage;
    }

    public String getBaseType() {
		return baseType;
	}

	public void setBaseType(String baseType) {
		this.baseType = baseType;
	}

	@Override
    public String toString() {
        return "DocumentDTO{" +
                "fileStorage='" + fileStorage + '\'' + super.toString() +
                '}';
    }
}
