package ru.doccloud.document.model;

import java.sql.Timestamp;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.joda.time.LocalDateTime;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * @author Andrey Kadnikov
 */
public class Document {

	private final Long id;

    private final LocalDateTime creationTime;

    private final String author;

    private final LocalDateTime modificationTime;
    
    private final String modifier;

    private final String title;
    
    private final String description;
    
    private final String filePath;
    
    private final String fileMimeType;
    
    private final Long fileLength;
    
    private final String type;
    
    private final JsonNode data;

    private Document(Builder builder) {
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
        
        this.author = builder.author;
        
        this.modifier = builder.modifier;
        
        this.type = builder.type;
        
        this.data = builder.data;
    }

    public static Builder getBuilder(String title) {
        return new Builder(title);
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
    
    public String getFilePath() {
        return filePath;
    }

    public String getType() {
		return type;
	}
    
    public JsonNode getData() {
        return data;
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

    public String getFileMimeType() {
		return fileMimeType;
	}

	public Long getFileLength() {
		return fileLength;
	}

	public String getAuthor() {
		return author;
	}

	public String getModifier() {
		return modifier;
	}

	public static class Builder {

		private Long id;

        private Timestamp creationTime;

        private String description;

        private Timestamp modificationTime;

		private String title;
		
		private String filePath;
		
		private Long fileLength;

		private String fileMimeType;
        
        private String type;
        
        private String author;
        
        private String modifier;
        
        private JsonNode data;

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
        
        public Builder author(String author) {
            this.author = author;
            return this;
        }
        
        public Builder fileMimeType(String fileMimeType) {
            this.fileMimeType = fileMimeType;
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
        
        

        public Document build() {
            Document created = new Document(this);

            String title = created.getTitle();

            if (title == null || title.length() == 0) {
                throw new IllegalStateException("title cannot be null or empty");
            }

            return created;
        }

    }
}
