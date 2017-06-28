package ru.doccloud.document.model;


public class UserRole {
    private String role;
    private String userId;

    public String getRole() {
        return role;
    }

    public String getUserId() {
        return userId;
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

    @Override
    public String toString() {
        return "UserRole{" +
                "role='" + role + '\'' +
                ", userId='" + userId + '\'' +
                '}';
    }
}
