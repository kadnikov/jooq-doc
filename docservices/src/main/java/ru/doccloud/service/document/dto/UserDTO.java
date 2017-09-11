package ru.doccloud.service.document.dto;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class UserDTO {
    private String userId;
    private String password;
    private String fullName;
    private String email;
    private String[] groups;
    private List<UserRoleDTO> userRoleList;

    public UserDTO(String userId, String password, String fullName, String email, String[] groups, List<UserRoleDTO> userRoleList) {
        this.userId = userId;
        this.password = password;
        this.fullName = fullName;
        this.email = email;
        this.groups = groups;
        this.userRoleList = userRoleList;
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

    public String getEmail() {
        return email;
    }

    public String[] getGroups() {
		return groups;
	}
    
    public List<UserRoleDTO> getUserRoleList() {
        return userRoleList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserDTO userDTO = (UserDTO) o;

        if (!userId.equals(userDTO.userId)) return false;
        return password.equals(userDTO.password);

    }

    @Override
    public int hashCode() {
        int result = userId.hashCode();
        result = 31 * result + password.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "UserDTO{" +
                "userId='" + userId + '\'' +
                ", password lenght='" +  (!StringUtils.isBlank(password) ? password.length() : 0) + '\'' +
                ", fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                ", groups='" + groups + '\'' +
                ", userRoleList=" + userRoleList +
                '}';
    }
}
