package ru.doccloud.document.model;

import com.fasterxml.jackson.databind.JsonNode;
import org.joda.time.LocalDateTime;

import java.util.UUID;

/**
 * Created by ilya on 5/27/17.
 */
public abstract class AbstractDocument {
    protected  Long id;

    protected  LocalDateTime creationTime;

    protected  String author;

    protected  LocalDateTime modificationTime;

    protected  String modifier;

    protected  String title;

    protected  String description;

    protected  String filePath;

    protected  String fileName;

    protected  String fileMimeType;

    protected  Long fileLength;

    protected  String type;
    
    protected  String parent;

    protected  String docVersion;

    protected  JsonNode data;

    protected  UUID uuid;


    public Long getId() {
        return id;
    }

    public LocalDateTime getCreationTime() {
        return creationTime;
    }

    public String getAuthor() {
        return author;
    }

    public LocalDateTime getModificationTime() {
        return modificationTime;
    }

    public String getModifier() {
        return modifier;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFileMimeType() {
        return fileMimeType;
    }

    public Long getFileLength() {
        return fileLength;
    }

    public String getType() {
        return type;
    }

    public String getParent() {
		return parent;
	}

	public void setParent(String parent) {
		this.parent = parent;
	}

	public String getDocVersion() {
        return docVersion;
    }

    public JsonNode getData() {
        return data;
    }

    public UUID getUuid() {
        return uuid;
    }



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AbstractDocument)) return false;

        AbstractDocument that = (AbstractDocument) o;

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
        return "AbstractDocument{" +
                "id=" + id +
                ", creationTime=" + creationTime +
                ", author='" + author + '\'' +
                ", modificationTime=" + modificationTime +
                ", modifier='" + modifier + '\'' +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", filePath='" + filePath + '\'' +
                ", fileName='" + fileName + '\'' +
                ", fileMimeType='" + fileMimeType + '\'' +
                ", fileLength=" + fileLength +
                ", type='" + type + '\'' +
                ", docVersion='" + docVersion + '\'' +
                ", data=" + data +
                ", uuid=" + uuid +
                '}';
    }
}
