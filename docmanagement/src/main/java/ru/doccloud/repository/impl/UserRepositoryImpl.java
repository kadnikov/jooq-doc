package ru.doccloud.repository.impl;

import static ru.doccloud.document.jooq.db.tables.UserRoles.USER_ROLES;
import static ru.doccloud.document.jooq.db.tables.Users.USERS;
import static ru.doccloud.document.jooq.db.tables.Groups.GROUPS;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.jooq.DSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import ru.doccloud.document.jooq.db.tables.records.GroupsRecord;
import ru.doccloud.document.jooq.db.tables.records.UserRolesRecord;
import ru.doccloud.document.jooq.db.tables.records.UsersRecord;
import ru.doccloud.document.model.Group;
import ru.doccloud.document.model.User;
import ru.doccloud.document.model.UserRole;
import ru.doccloud.repository.UserRepository;

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
    public List<Group> getGoups(){ 
    	final List<GroupsRecord> queryResult = jooq.selectFrom(GROUPS).fetchInto(GroupsRecord.class);
    	final List<Group> groups = convertGroupsQueryResultToModelObj(queryResult);
    	return groups;
	}
    @Transactional(readOnly = true)
    @Override
    @Cacheable(value = "userByLoginAndPwd", cacheManager = "springCM")
    public User getUser(final String login, final String password) {
        boolean isPasswordEmpty = StringUtils.isBlank(password);
        LOGGER.trace("entering getUser(login = {}, passwordLenght = {})", login, !isPasswordEmpty ? password.length() : 0);

        if(isPasswordEmpty)
            throw new IllegalStateException("getUser(): password is empty");

        final UsersRecord queryResult = jooq.selectFrom(USERS)
                .where(USERS.USERID.equal(login))
                .fetchOne();

        LOGGER.trace("getUser(): found user {}", queryResult);

//        todo rewrite using join
        final List<UserRolesRecord> userRolesQueryResult = jooq.selectFrom(USER_ROLES).where(USER_ROLES.USERID.equal(login)).fetchInto(UserRolesRecord.class);

        LOGGER.trace("getUser(): found roles {}", userRolesQueryResult);


        final User user = convertQueryResultToModelObject(queryResult, userRolesQueryResult);
//        todo add userRoles List to user Object
        LOGGER.trace("getUser(): found user {}", user);
        return user;
    }

    private static User convertQueryResultToModelObject(UsersRecord queryResult,  List<UserRolesRecord> userRolesQueryResult) {
        User user=  User.getBuilder(queryResult.getUserid())
                .password(queryResult.getPassword())
                .fullName(queryResult.getFullname())
                .email(queryResult.getEmail())
                .build();
        user.setUserRoleList(convertUserRolesQueryResultToModelObj(userRolesQueryResult));
        return user;
    }

    private static List<UserRole> convertUserRolesQueryResultToModelObj(List<UserRolesRecord> userRolesQueryResult){
        if(userRolesQueryResult == null)
            return null;
        List<UserRole> userRoles = new ArrayList<>();

        for (UserRolesRecord queryResult : userRolesQueryResult) {
            UserRole userRole = UserRole.getBuilder(queryResult.getUserid()).role(queryResult.getRole()).build();
            userRoles.add(userRole);
        }

        return userRoles;
    }
    
    private static List<Group> convertGroupsQueryResultToModelObj(List<GroupsRecord> groupsQueryResult){
        if(groupsQueryResult == null)
            return null;
        List<Group> groups = new ArrayList<>();

        for (GroupsRecord queryResult : groupsQueryResult) {
        	Group group = Group.getBuilder(queryResult.getId()).title(queryResult.getTitle()).build();
        	groups.add(group);
        }

        return groups;
    }

}
