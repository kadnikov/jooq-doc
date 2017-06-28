package ru.doccloud.repository.impl;

import org.apache.commons.lang3.StringUtils;
import org.jooq.DSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.doccloud.document.jooq.db.tables.records.UsersRecord;
import ru.doccloud.document.model.User;
import ru.doccloud.repository.UserRepository;

import static ru.doccloud.document.jooq.db.tables.Users.USERS;

@Repository
public class UserRepositoryImpl implements UserRepository {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserRepositoryImpl.class);

    private  final DSLContext jooq;

    @Autowired
    public UserRepositoryImpl(DSLContext jooq) {
        this.jooq = jooq;
    }

    @Transactional(readOnly = true)
    @Override
    public User getUser(final String login, final String password) {
        boolean isPasswordEmpty = StringUtils.isBlank(password);
        LOGGER.trace("entering getUser(login = {}, passwordLenght = {})", login, !isPasswordEmpty ? password.length() : 0);

        if(isPasswordEmpty)
            throw new IllegalStateException("getUser(): password is empty");

        final UsersRecord queryResult = jooq.selectFrom(USERS)
                .where(USERS.USERID.equal(login))
                .fetchOne();

        LOGGER.trace("getUser(): found result {}", queryResult);

        final User user = convertQueryResultToModelObject(queryResult);
//        todo add userRoles List to user Object
        LOGGER.trace("getUser(): found user {}", user);
        return user;
    }

    private static User convertQueryResultToModelObject(UsersRecord queryResult) {
        return User.getBuilder(queryResult.getUserid())
                .password(queryResult.getPassword())
                .fullName(queryResult.getFullname())
                .email(queryResult.getEmail())
                .build();
    }
}
