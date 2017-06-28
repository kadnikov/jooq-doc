package ru.doccloud.service.document.dto;

/**
 * Created by Illia_Ushakov on 6/28/2017.
 */
public class UserRoleDTO {
    private String role;
    private String userId;

    public UserRoleDTO(String role, String userId) {
        this.role = role;
        this.userId = userId;
    }

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

        UserRoleDTO that = (UserRoleDTO) o;

        if (!role.equals(that.role)) return false;
        return userId.equals(that.userId);

    }

    @Override
    public int hashCode() {
        int result = role.hashCode();
        result = 31 * result + userId.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "UserRoleDTO{" +
                "role='" + role + '\'' +
                ", userId='" + userId + '\'' +
                '}';
    }
}
