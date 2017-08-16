package ru.doccloud.document.model;

import org.apache.commons.lang3.StringUtils;

public class Group {
	private String title;
    private String id;
    
    public Group(Builder builder) {
        this.title = builder.title;
        this.id = builder.id;
    }
	public String getTitle() {
		return title;
	}
	public String getId() {
		return id;
	}

    public static Group.Builder getBuilder(String groupId) {
        return new Group.Builder(groupId);
    }

    public static class Builder {

        private String id;

        private String title;


        public Builder(String id) {
            this.id = id;
        }

        public Group.Builder id(String id) {
            this.id = id;
            return this;
        }

        public Group.Builder title(String title) {
            this.title = title;
            return this;
        }

        public static Group.Builder getBuilder(String id) {
            return new Group.Builder(id);
        }

        public Group build() {
        	Group created = new Group(this);

            String groupId = created.getId();

            if (StringUtils.isBlank(groupId)) {
                throw new IllegalStateException("id cannot be null or empty");
            }

            return created;
        }

    }
    
}
