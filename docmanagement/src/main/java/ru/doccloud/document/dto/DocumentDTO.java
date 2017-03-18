package ru.doccloud.document.dto;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;
import org.joda.time.LocalDateTime;
import org.jtransfo.DomainClass;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import ru.doccloud.common.json.CustomJsonDataDeserializer;
import ru.doccloud.common.json.CustomJsonDataSerializer;
import ru.doccloud.common.json.CustomLocalDateTimeDeserializer;
import ru.doccloud.common.json.CustomLocalDateTimeSerializer;

/**
 * @author Andrey Kadnikov
 */
@DomainClass("ru.doccloud.document.model.Document")
public class DocumentDTO {

    private Long id;

    @JsonDeserialize(using = CustomLocalDateTimeDeserializer.class)
    @JsonSerialize(using = CustomLocalDateTimeSerializer.class)
    private LocalDateTime creationTime;
    
    private String author;

    @Length(max = 500)
    private String description;

    @JsonDeserialize(using = CustomLocalDateTimeDeserializer.class)
    @JsonSerialize(using = CustomLocalDateTimeSerializer.class)
    private LocalDateTime modificationTime;

    private String modifier;
    
    @NotEmpty
    @Length(max = 100)
    private String title;

    private String docVersion;

    private String type;
    
    private String filePath;
    
    private String fileMimeType;

    private String fileName;
    
    private Long fileLength;

    private JsonNode data;

    public DocumentDTO() {

    }

    public DocumentDTO(String title, String type, String author) {
        this.author = author;
        this.title = title;
        this.type = type;
    }

    public Long getId() {
        return id;
    }

    public LocalDateTime getCreationTime() {
        return creationTime;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getModificationTime() {
        return modificationTime;
    }

    public String getTitle() {
        return title;
    }
    
	public String getType() {
		return type;
	}

    public String getFileName() {
        return fileName;
    }

    public JsonNode getData() {
		return data;
	}


    public String getDocVersion() {
        return docVersion;
    }

    public void setDocVersion(String docVersion) {
        this.docVersion = docVersion;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setCreationTime(LocalDateTime creationTime) {
        this.creationTime = creationTime;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setModificationTime(LocalDateTime modificationTime) {
        this.modificationTime = modificationTime;
    }

    public void setTitle(String title) {
        this.title = title;
    }
    
    public void setType(String type) {
        this.type = type;
    }

	
    public void setData(JsonNode data) {
		this.data = data;
	}



    public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public String getFileMimeType() {
		return fileMimeType;
	}

	public void setFileMimeType(String fileMimeType) {
		this.fileMimeType = fileMimeType;
	}

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Long getFileLength() {
		return fileLength;
	}

	public void setFileLength(Long fileLength) {
		this.fileLength = fileLength;
	}

	public String getModifier() {
		return modifier;
	}

	public void setModifier(String modifier) {
		this.modifier = modifier;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

    @Override
    public String toString() {
        return "DocumentDTO{" +
                "id=" + id +
                ", creationTime=" + creationTime +
                ", author='" + author + '\'' +
                ", description='" + description + '\'' +
                ", modificationTime=" + modificationTime +
                ", modifier='" + modifier + '\'' +
                ", title='" + title + '\'' +
                ", docVersion='" + docVersion + '\'' +
                ", type='" + type + '\'' +
                ", filePath='" + filePath + '\'' +
                ", fileMimeType='" + fileMimeType + '\'' +
                ", fileName='" + fileName + '\'' +
                ", fileLength=" + fileLength +
                ", data=" + data +
                '}';
    }
}
