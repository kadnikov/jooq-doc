package ru.doccloud.service;


import java.util.List;

import ru.doccloud.service.document.dto.GroupDTO;
import ru.doccloud.service.document.dto.UserDTO;

/**
 * Created by Illia_Ushakov on 6/28/2017.
 */
public interface UserService {
    public UserDTO getUserDto(final String login, final String password);

    public List<GroupDTO> getGroups();
}
