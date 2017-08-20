/**
 * This class is generated by jOOQ
 */
package ru.doccloud.document.jooq.db;


import javax.annotation.Generated;

import ru.doccloud.document.jooq.db.tables.Documents;
import ru.doccloud.document.jooq.db.tables.Groups;
import ru.doccloud.document.jooq.db.tables.Links;
import ru.doccloud.document.jooq.db.tables.Roles;
import ru.doccloud.document.jooq.db.tables.System;
import ru.doccloud.document.jooq.db.tables.Todos;
import ru.doccloud.document.jooq.db.tables.UserRoles;
import ru.doccloud.document.jooq.db.tables.Users;


/**
 * Convenience access to all tables in public
 */
@Generated(
	value = {
		"http://www.jooq.org",
		"jOOQ version:3.6.1"
	},
	comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Tables {

	/**
	 * The table public.documents
	 */
	public static final Documents DOCUMENTS = ru.doccloud.document.jooq.db.tables.Documents.DOCUMENTS;

	/**
	 * The table public.groups
	 */
	public static final Groups GROUPS = ru.doccloud.document.jooq.db.tables.Groups.GROUPS;

	/**
	 * The table public.links
	 */
	public static final Links LINKS = ru.doccloud.document.jooq.db.tables.Links.LINKS;

	/**
	 * The table public.roles
	 */
	public static final Roles ROLES = ru.doccloud.document.jooq.db.tables.Roles.ROLES;

	/**
	 * The table public.system
	 */
	public static final System SYSTEM = ru.doccloud.document.jooq.db.tables.System.SYSTEM;

	/**
	 * The table public.todos
	 */
	public static final Todos TODOS = ru.doccloud.document.jooq.db.tables.Todos.TODOS;

	/**
	 * The table public.user_roles
	 */
	public static final UserRoles USER_ROLES = ru.doccloud.document.jooq.db.tables.UserRoles.USER_ROLES;

	/**
	 * The table public.users
	 */
	public static final Users USERS = ru.doccloud.document.jooq.db.tables.Users.USERS;
}