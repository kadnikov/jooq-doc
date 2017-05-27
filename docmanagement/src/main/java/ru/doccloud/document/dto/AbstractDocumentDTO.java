package ru.doccloud.document.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;
import org.joda.time.LocalDateTime;
import ru.doccloud.common.json.CustomLocalDateTimeDeserializer;
import ru.doccloud.common.json.CustomLocalDateTimeSerializer;

import java.util.UUID;

/**
 * Created by ilya on 5/27/17.
 */
public abstract class AbstractDocumentDTO {

    protected Long id;

    @JsonDeserialize(using = CustomLocalDateTimeDeserializer.class)
    @JsonSerialize(using = CustomLocalDateTimeSerializer.class)
    protected LocalDateTime creationTime;

    protected String author;

    @Length(max = 500)
    protected String description;

    @JsonDeserialize(using = CustomLocalDateTimeDeserializer.class)
    @JsonSerialize(using = CustomLocalDateTimeSerializer.class)
    protected LocalDateTime modificationTime;

    protected String modifier;

    @NotEmpty
    @Length(max = 100)
    protected String title;

    protected String docVersion;

    protected String type;

    protected String filePath;

    protected String fileMimeType;

    protected String fileName;

    protected Long fileLength;

    protected JsonNode data;

    protected UUID uuid;

    public AbstractDocumentDTO() {
    }

    public AbstractDocumentDTO(String title, String type, String author) {
        this.author = author;
        this.title = title;
        this.type = type;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(LocalDateTime creationTime) {
        this.creationTime = creationTime;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getModificationTime() {
        return modificationTime;
    }

    public void setModificationTime(LocalDateTime modificationTime) {
        this.modificationTime = modificationTime;
    }

    public String getModifier() {
        return modifier;
    }

    public void setModifier(String modifier) {
        this.modifier = modifier;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDocVersion() {
        return docVersion;
    }

    public void setDocVersion(String docVersion) {
        this.docVersion = docVersion;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    public String getFileName() {
        return fileName;
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

    public JsonNode getData() {
        return data;
    }

    public void setData(JsonNode data) {
        this.data = data;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AbstractDocumentDTO)) return false;

        AbstractDocumentDTO that = (AbstractDocumentDTO) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        return uuid != null ? uuid.equals(that.uuid) : that.uuid == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (uuid != null ? uuid.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "AbstractDocumentDTO{" +
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
                ", uuid=" + uuid +
                '}';
    }
}
