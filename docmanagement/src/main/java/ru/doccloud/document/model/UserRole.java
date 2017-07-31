package ru.doccloud.document.model;


import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;

public class UserRole implements Serializable {
    private String role;
    private String userId;

    public UserRole(Builder builder) {
        this.userId = builder.userId;
        this.role = builder.role;
    }


    public String getRole() {
        return role;
    }

    public String getUserId() {
        return userId;
    }


    public static class Builder {

        private String userId;

        private String role;


        public Builder(String userId) {
            this.userId = userId;
        }

        public UserRole.Builder role(String role) {
            this.role = role;
            return this;
        }


        public static UserRole.Builder getBuilder(String userId) {
            return new UserRole.Builder(userId);
        }

        public UserRole build() {
            UserRole created = new UserRole(this);

            String userId = created.getUserId();

            if (StringUtils.isBlank(userId)) {
                throw new IllegalStateException("title cannot be null or empty");
            }

            return created;
        }

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserRole userRole = (UserRole) o;

        if (!role.equals(userRole.role)) return false;
        return userId.equals(userRole.userId);

    }

    @Override
    public int hashCode() {
        int result = role.hashCode();
        result = 31 * result + userId.hashCode();
        return result;
    }

    public static UserRole.Builder getBuilder(String userId) {
        return new UserRole.Builder(userId);
    }

    @Override
    public String toString() {
        return "UserRole{" +
                "role='" + role + '\'' +
                ", userId='" + userId + '\'' +
                '}';
    }
}
