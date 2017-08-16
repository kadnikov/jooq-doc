package ru.doccloud.repository;

import java.util.List;

import ru.doccloud.document.model.Group;
import ru.doccloud.document.model.User;

public interface UserRepository {
    public User getUser(final String login, final String password);

    public List<Group> getGoups();
}
