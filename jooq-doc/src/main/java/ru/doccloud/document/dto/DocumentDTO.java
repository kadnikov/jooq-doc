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
    
    private String createdBy;

    @Length(max = 500)
    private String description;

    @JsonDeserialize(using = CustomLocalDateTimeDeserializer.class)
    @JsonSerialize(using = CustomLocalDateTimeSerializer.class)
    private LocalDateTime modificationTime;

    private String modifiedBy;
    
    @NotEmpty
    @Length(max = 100)
    private String title;
    
    private String type;
    
    private String filePath;
    
    @JsonDeserialize(using = CustomJsonDataDeserializer.class)
    @JsonSerialize(using = CustomJsonDataSerializer.class)
    private JsonNode data;

    public DocumentDTO() {

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
	

    public String getCreatedBy() {
		return createdBy;
	}

	public String getModifiedBy() {
		return modifiedBy;
	}

	public JsonNode getData() {
		return data;
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

	public void setModifiedBy(String modifiedBy) {
		this.modifiedBy = modifiedBy;
	}
	
	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}
	
    public void setData(JsonNode data) {
		this.data = data;
	}

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", id)
                .append("creationTime", creationTime)
                .append("description", description)
                .append("modificationTime", modificationTime)
                .append("title", title)
                .append("data", data)
                .build();

    }

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

}
