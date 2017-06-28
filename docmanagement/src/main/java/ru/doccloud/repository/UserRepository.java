package ru.doccloud.repository;

import ru.doccloud.document.model.User;

public interface UserRepository {
    public User getUser(final String login, final String password);
}
