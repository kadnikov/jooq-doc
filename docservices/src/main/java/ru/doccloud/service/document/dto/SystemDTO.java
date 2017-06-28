package ru.doccloud.service.document.dto;

import org.jtransfo.DomainClass;

/**
 * @author Ilya Ushakov
 */
@DomainClass("ru.doccloud.document.model.SystemDocument")
public class SystemDTO extends AbstractDocumentDTO{



    private String symbolicName;
    
    public String getSymbolicName() {
		return symbolicName;
	}

	public void setSymbolicName(String symbolicName) {
		this.symbolicName = symbolicName;
	}

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
