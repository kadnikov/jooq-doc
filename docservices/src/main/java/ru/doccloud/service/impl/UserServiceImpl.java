package ru.doccloud.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ru.doccloud.document.model.Group;
import ru.doccloud.document.model.User;
import ru.doccloud.document.model.UserRole;
import ru.doccloud.repository.UserRepository;
import ru.doccloud.service.UserService;
import ru.doccloud.service.document.dto.GroupDTO;
import ru.doccloud.service.document.dto.UserDTO;
import ru.doccloud.service.document.dto.UserRoleDTO;

@Service
public class UserServiceImpl implements UserService {
    private static final Logger LOGGER = LoggerFactory.getLogger(SystemDocumentCrudService.class);

    private UserRepository userRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public List<GroupDTO> getGroups(){
    	final List<Group> groups = userRepository.getGoups();
    	if(groups == null)
            return null;
        List<GroupDTO> groupsDTOList = new ArrayList<>();
        for (Group group: groups){
        	groupsDTOList.add(new GroupDTO(group.getTitle(),group.getId()));
        }
        return groupsDTOList;
    }
    @Override
    public UserDTO getUserDto(final String login, final String password) {
        LOGGER.debug("entering getUserDto(login={}, passw0rdlenght = {})", login, !StringUtils.isBlank(password) ? password.length() : 0);
        final User user =  userRepository.getUser(login, password);

        LOGGER.debug("getUserDto(): found user {}", user);
        UserDTO userDTO =  toUserDto(user);

        LOGGER.debug("leaving getUserDto(): userDto {}", userDTO);
        return userDTO;
    }

    private UserDTO toUserDto(User user) {
        return new UserDTO(user.getUserId(), user.getPassword(), user.getFullName(), user.getEmail(), convertUserRolesToDTOist(user.getUserRoleList()));
    }

    private List<UserRoleDTO> convertUserRolesToDTOist(List<UserRole> userRoles) {
        if(userRoles == null)
            return null;
        List<UserRoleDTO> userRoleDTOList = new ArrayList<>();
        for (UserRole userRole: userRoles){
            userRoleDTOList.add(new UserRoleDTO(userRole.getRole(), userRole.getUserId()));
        }
        return userRoleDTOList;
    }
}
