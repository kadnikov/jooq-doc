package ru.doccloud.document.model;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;

import java.io.Serializable;
import java.util.List;

public class User implements Serializable{

    private String userId;
    private String password;
    private String fullName;
    private String email;
    private JsonNode details;
    private String[] groups;

    private List<UserRole> userRoleList;

    public User(Builder builder) {
        this.userId = builder.userId;
        this.password = builder.password;
        this.fullName = builder.fullName;
        this.email = builder.email;
        this.groups = builder.groups;
        this.details = builder.details;
    }

    public String getUserId() {
        return userId;
    }

    public String getPassword() {
        return password;
    }

    public String getFullName() {
        return fullName;
    }
    
    public JsonNode getDetails() {
        return details;
    }
    
    public String getEmail() {
        return email;
    }

    public String[] getGroups() {
		return groups;
	}

	public List<UserRole> getUserRoleList() {
        return userRoleList;
    }

    public void setUserRoleList(List<UserRole> userRoleList) {
        this.userRoleList = userRoleList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        if (!userId.equals(user.userId)) return false;
        return password.equals(user.password);

    }

    @Override
    public int hashCode() {
        int result = userId.hashCode();
        result = 31 * result + password.hashCode();
        return result;
    }

    public static User.Builder getBuilder(String userId) {
        return new User.Builder(userId);
    }

    @Override
    public String toString() {
        return "User{" +
                "userId='" + userId + '\'' +
                ", password lenght='" + (!StringUtils.isBlank(password) ? password.length() : 0) + '\'' +
                ", fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                ", details='" + details + '\'' +
                ", groups='" + groups + '\'' +
                '}';
    }


    public static class Builder {

        private String userId;

        private String password;

        private String fullName;

        private String email;

        private JsonNode details;
        
        private String[] groups;


        public Builder(String userId) {
            this.userId = userId;
        }

        public User.Builder password(String password) {
            this.password = password;
            return this;
        }

        public User.Builder fullName(String fullName) {
            this.fullName = fullName;
            return this;
        }

        public User.Builder email(String email) {
            this.email = email;
            return this;
        }
        
        public User.Builder details(JsonNode details) {
            this.details = details;
            return this;
        }
        
        public User.Builder groups(String[] groups) {
            this.groups = groups;
            return this;
        }

        public static User.Builder getBuilder(String userId) {
            return new User.Builder(userId);
        }

        public User build() {
            User created = new User(this);

            String userId = created.getUserId();

            if (StringUtils.isBlank(userId)) {
                throw new IllegalStateException("title cannot be null or empty");
            }

            return created;
        }

    }
}
