package ru.doccloud.document.model;

import com.fasterxml.jackson.databind.JsonNode;
import org.joda.time.LocalDateTime;

import java.sql.Timestamp;
import java.util.Objects;
import java.util.UUID;

/**
 * @author IlyaUshakov
 */
public class System extends AbstractDocument{

    private String symbolicName;

    private System(Builder builder) {
        this.id = builder.id;

        LocalDateTime creationTime = null;
        if (builder.creationTime != null) {
            creationTime = new LocalDateTime(builder.creationTime);
        }
        this.creationTime = creationTime;

        this.description = builder.description;

        LocalDateTime modificationTime = null;
        if (builder.modificationTime != null) {
            modificationTime = new LocalDateTime(builder.modificationTime);
        }
        this.modificationTime = modificationTime;

        this.title = builder.title;
        
        this.filePath = builder.filePath;
        
        this.fileMimeType = builder.fileMimeType;
        
        this.fileLength = builder.fileLength;

        this.fileName = builder.fileName;
        
        this.author = builder.author;
        
        this.modifier = builder.modifier;
        
        this.type = builder.type;
        
        this.data = builder.data;
        this.docVersion = builder.docVersion;
        this.uuid = builder.uuid;
    }

    public static Builder getBuilder(String title) {
        return new Builder(title);
    }


    public String getSymbolicName() {
        return symbolicName;
    }

    public void setSymbolicName(String symbolicName) {
        this.symbolicName = symbolicName;
    }

    public static class Builder {

		private Long id;

        private Timestamp creationTime;

        private String description;

        private Timestamp modificationTime;

		private String title;

		private String filePath;

		private Long fileLength;

		private String fileName;

		private String fileMimeType;

        private String type;

        private String author;

        private String modifier;

        private JsonNode data;

        private String docVersion;

        private UUID uuid;

        private String symbolicName;

        public Builder(String title) {
            this.title = title;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }


        public Builder type(String type) {
            this.type = type;
            return this;
        }

        public Builder modifier(String modifiedBy) {
            this.modifier = modifiedBy;
            return this;
        }

        public Builder filePath(String filePath) {
            this.filePath = filePath;
            return this;
        }

        public Builder fileName(String fileName){
            this.fileName = fileName;
            return this;
        }

        public Builder author(String author) {
            this.author = author;
            return this;
        }

        public Builder fileMimeType(String fileMimeType) {
            this.fileMimeType = fileMimeType;
            return this;
        }

        public Builder symbolicName(String symbolicName) {
            this.symbolicName = symbolicName;
            return this;
        }

        public Builder fileLength(Long fileLength) {
            this.fileLength = fileLength;
            return this;
        }



        public Builder creationTime(Timestamp creationTime) {
            this.creationTime = creationTime;
            return this;
        }

        public Builder id(Long integer) {
            this.id = integer;
            return this;
        }

        public Builder data(JsonNode data) {
            this.data = data;
            return this;
        }

        public Builder modificationTime(Timestamp modificationTime) {
            this.modificationTime = modificationTime;
            return this;
        }

        public Builder docVersion(String docVersion){
            this.docVersion = docVersion;
            return this;
        }

        public Builder uuid(UUID uuid){
            this.uuid = uuid;
            return this;
        }

        public System build() {
            System created = new System(this);

            String title = created.getTitle();

            if (title == null || title.length() == 0) {
                throw new IllegalStateException("title cannot be null or empty");
            }

            return created;
        }

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof System)) return false;
        if (!super.equals(o)) return false;
        System that = (System) o;
        return Objects.equals(symbolicName, that.symbolicName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), symbolicName);
    }

    @Override
    public String toString() {
        return "System{" +
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
                ", symbolicName='" + symbolicName + '\'' +
                '}';
    }
}
