/**
 * This class is generated by jOOQ
 */
package ru.doccloud.document.jooq.db;


import javax.annotation.Generated;

import org.jooq.ForeignKey;
import org.jooq.Identity;
import org.jooq.UniqueKey;
import org.jooq.impl.AbstractKeys;

import ru.doccloud.document.jooq.db.tables.Documents;
import ru.doccloud.document.jooq.db.tables.Groups;
import ru.doccloud.document.jooq.db.tables.Links;
import ru.doccloud.document.jooq.db.tables.Roles;
import ru.doccloud.document.jooq.db.tables.System;
import ru.doccloud.document.jooq.db.tables.Todos;
import ru.doccloud.document.jooq.db.tables.UserRoles;
import ru.doccloud.document.jooq.db.tables.Users;
import ru.doccloud.document.jooq.db.tables.records.DocumentsRecord;
import ru.doccloud.document.jooq.db.tables.records.GroupsRecord;
import ru.doccloud.document.jooq.db.tables.records.LinksRecord;
import ru.doccloud.document.jooq.db.tables.records.RolesRecord;
import ru.doccloud.document.jooq.db.tables.records.SystemRecord;
import ru.doccloud.document.jooq.db.tables.records.TodosRecord;
import ru.doccloud.document.jooq.db.tables.records.UserRolesRecord;
import ru.doccloud.document.jooq.db.tables.records.UsersRecord;


/**
 * A class modelling foreign key relationships between tables of the <code>public</code> 
 * schema
 */
@Generated(
	value = {
		"http://www.jooq.org",
		"jOOQ version:3.6.1"
	},
	comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Keys {

	// -------------------------------------------------------------------------
	// IDENTITY definitions
	// -------------------------------------------------------------------------

	public static final Identity<DocumentsRecord, Integer> IDENTITY_DOCUMENTS = Identities0.IDENTITY_DOCUMENTS;
	public static final Identity<SystemRecord, Integer> IDENTITY_SYSTEM = Identities0.IDENTITY_SYSTEM;
	public static final Identity<TodosRecord, Integer> IDENTITY_TODOS = Identities0.IDENTITY_TODOS;

	// -------------------------------------------------------------------------
	// UNIQUE and PRIMARY KEY definitions
	// -------------------------------------------------------------------------

	public static final UniqueKey<DocumentsRecord> DOCUMENTS_PKEY = UniqueKeys0.DOCUMENTS_PKEY;
	public static final UniqueKey<GroupsRecord> GROUPS_PKEY = UniqueKeys0.GROUPS_PKEY;
	public static final UniqueKey<LinksRecord> LINKS_PKEY = UniqueKeys0.LINKS_PKEY;
	public static final UniqueKey<RolesRecord> ROLES_PKEY = UniqueKeys0.ROLES_PKEY;
	public static final UniqueKey<SystemRecord> SYSTEM_PKEY = UniqueKeys0.SYSTEM_PKEY;
	public static final UniqueKey<TodosRecord> TODOS_PKEY = UniqueKeys0.TODOS_PKEY;
	public static final UniqueKey<UserRolesRecord> USER_ROLES_PKEY = UniqueKeys0.USER_ROLES_PKEY;
	public static final UniqueKey<UsersRecord> USERS_PKEY = UniqueKeys0.USERS_PKEY;

	// -------------------------------------------------------------------------
	// FOREIGN KEY definitions
	// -------------------------------------------------------------------------

	public static final ForeignKey<LinksRecord, DocumentsRecord> LINKS__LINKS_HEAD_ID_FKEY = ForeignKeys0.LINKS__LINKS_HEAD_ID_FKEY;
	public static final ForeignKey<LinksRecord, DocumentsRecord> LINKS__LINKS_TAIL_ID_FKEY = ForeignKeys0.LINKS__LINKS_TAIL_ID_FKEY;
	public static final ForeignKey<UserRolesRecord, RolesRecord> USER_ROLES__USER_ROLES_ROLE_FKEY = ForeignKeys0.USER_ROLES__USER_ROLES_ROLE_FKEY;
	public static final ForeignKey<UserRolesRecord, UsersRecord> USER_ROLES__USER_ROLES_USERID_FKEY = ForeignKeys0.USER_ROLES__USER_ROLES_USERID_FKEY;

	// -------------------------------------------------------------------------
	// [#1459] distribute members to avoid static initialisers > 64kb
	// -------------------------------------------------------------------------

	private static class Identities0 extends AbstractKeys {
		public static Identity<DocumentsRecord, Integer> IDENTITY_DOCUMENTS = createIdentity(Documents.DOCUMENTS, Documents.DOCUMENTS.ID);
		public static Identity<SystemRecord, Integer> IDENTITY_SYSTEM = createIdentity(System.SYSTEM, System.SYSTEM.ID);
		public static Identity<TodosRecord, Integer> IDENTITY_TODOS = createIdentity(Todos.TODOS, Todos.TODOS.ID);
	}

	private static class UniqueKeys0 extends AbstractKeys {
		public static final UniqueKey<DocumentsRecord> DOCUMENTS_PKEY = createUniqueKey(Documents.DOCUMENTS, Documents.DOCUMENTS.ID);
		public static final UniqueKey<GroupsRecord> GROUPS_PKEY = createUniqueKey(Groups.GROUPS, Groups.GROUPS.ID);
		public static final UniqueKey<LinksRecord> LINKS_PKEY = createUniqueKey(Links.LINKS, Links.LINKS.HEAD_ID, Links.LINKS.TAIL_ID);
		public static final UniqueKey<RolesRecord> ROLES_PKEY = createUniqueKey(Roles.ROLES, Roles.ROLES.ROLE);
		public static final UniqueKey<SystemRecord> SYSTEM_PKEY = createUniqueKey(System.SYSTEM, System.SYSTEM.ID);
		public static final UniqueKey<TodosRecord> TODOS_PKEY = createUniqueKey(Todos.TODOS, Todos.TODOS.ID);
		public static final UniqueKey<UserRolesRecord> USER_ROLES_PKEY = createUniqueKey(UserRoles.USER_ROLES, UserRoles.USER_ROLES.USERID, UserRoles.USER_ROLES.ROLE);
		public static final UniqueKey<UsersRecord> USERS_PKEY = createUniqueKey(Users.USERS, Users.USERS.USERID);
	}

	private static class ForeignKeys0 extends AbstractKeys {
		public static final ForeignKey<LinksRecord, DocumentsRecord> LINKS__LINKS_HEAD_ID_FKEY = createForeignKey(ru.doccloud.document.jooq.db.Keys.DOCUMENTS_PKEY, Links.LINKS, Links.LINKS.HEAD_ID);
		public static final ForeignKey<LinksRecord, DocumentsRecord> LINKS__LINKS_TAIL_ID_FKEY = createForeignKey(ru.doccloud.document.jooq.db.Keys.DOCUMENTS_PKEY, Links.LINKS, Links.LINKS.TAIL_ID);
		public static final ForeignKey<UserRolesRecord, RolesRecord> USER_ROLES__USER_ROLES_ROLE_FKEY = createForeignKey(ru.doccloud.document.jooq.db.Keys.ROLES_PKEY, UserRoles.USER_ROLES, UserRoles.USER_ROLES.ROLE);
		public static final ForeignKey<UserRolesRecord, UsersRecord> USER_ROLES__USER_ROLES_USERID_FKEY = createForeignKey(ru.doccloud.document.jooq.db.Keys.USERS_PKEY, UserRoles.USER_ROLES, UserRoles.USER_ROLES.USERID);
	}
}