package ru.doccloud.docs.dto;

import org.joda.time.LocalDateTime;
import ru.doccloud.docs.common.TestDateUtil;
import ru.doccloud.service.document.dto.DocumentDTO;

/**
 */
public class DocsDTOBuilder {

    private Long id;

    private LocalDateTime creationTime;

    private String description;

    private LocalDateTime modificationTime;

    private String title;

    public DocsDTOBuilder() {

    }

    public DocsDTOBuilder creationTime(String creationTime) {
        this.creationTime = TestDateUtil.parseLocalDateTime(creationTime);
        return this;
    }

    public DocsDTOBuilder description(String description) {
        this.description = description;
        return this;
    }

    public DocsDTOBuilder id(Long id) {
        this.id = id;
        return this;
    }

    public DocsDTOBuilder modificationTime(String modificationTime) {
        this.modificationTime = TestDateUtil.parseLocalDateTime(modificationTime);
        return this;
    }

    public DocsDTOBuilder title(String title) {
        this.title = title;
        return this;
    }

    public DocumentDTO build() {
    	DocumentDTO dto = new DocumentDTO();

        dto.setId(id);
        dto.setCreationTime(creationTime);
        dto.setDescription(description);
        dto.setModificationTime(modificationTime);
        dto.setTitle(title);

        return dto;
    }
}
