/**
 * This class is generated by jOOQ
 */
package net.petrikainulainen.spring.jooq.todo.db.tables;


import com.fasterxml.jackson.databind.JsonNode;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Generated;

import net.petrikainulainen.spring.jooq.todo.db.Keys;
import net.petrikainulainen.spring.jooq.todo.db.Public;
import net.petrikainulainen.spring.jooq.todo.db.tables.records.DocumentsRecord;

import org.jooq.Field;
import org.jooq.Identity;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.UniqueKey;
import org.jooq.impl.TableImpl;

import ru.doccloud.common.jooq.PostgresJSONJacksonJsonNodeBinding;


/**
 * This class is generated by jOOQ.
 */
@Generated(
	value = {
		"http://www.jooq.org",
		"jOOQ version:3.6.1"
	},
	comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Documents extends TableImpl<DocumentsRecord> {

	private static final long serialVersionUID = 744441772;

	/**
	 * The reference instance of <code>public.documents</code>
	 */
	public static final Documents DOCUMENTS = new Documents();

	/**
	 * The class holding records for this type
	 */
	@Override
	public Class<DocumentsRecord> getRecordType() {
		return DocumentsRecord.class;
	}

	/**
	 * The column <code>public.documents.id</code>.
	 */
	public final TableField<DocumentsRecord, Integer> ID = createField("id", org.jooq.impl.SQLDataType.INTEGER.nullable(false).defaulted(true), this, "");

	/**
	 * The column <code>public.documents.sys_date_cr</code>.
	 */
	public final TableField<DocumentsRecord, Timestamp> SYS_DATE_CR = createField("sys_date_cr", org.jooq.impl.SQLDataType.TIMESTAMP.nullable(false).defaulted(true), this, "");

	/**
	 * The column <code>public.documents.sys_desc</code>.
	 */
	public final TableField<DocumentsRecord, String> SYS_DESC = createField("sys_desc", org.jooq.impl.SQLDataType.CLOB, this, "");

	/**
	 * The column <code>public.documents.sys_date_mod</code>.
	 */
	public final TableField<DocumentsRecord, Timestamp> SYS_DATE_MOD = createField("sys_date_mod", org.jooq.impl.SQLDataType.TIMESTAMP.nullable(false).defaulted(true), this, "");

	/**
	 * The column <code>public.documents.sys_title</code>.
	 */
	public final TableField<DocumentsRecord, String> SYS_TITLE = createField("sys_title", org.jooq.impl.SQLDataType.CLOB, this, "");

	/**
	 * The column <code>public.documents.sys_author</code>.
	 */
	public final TableField<DocumentsRecord, String> SYS_AUTHOR = createField("sys_author", org.jooq.impl.SQLDataType.CLOB, this, "");

	/**
	 * The column <code>public.documents.sys_modifier</code>.
	 */
	public final TableField<DocumentsRecord, String> SYS_MODIFIER = createField("sys_modifier", org.jooq.impl.SQLDataType.CLOB, this, "");

	/**
	 * The column <code>public.documents.sys_readers</code>.
	 */
	public final TableField<DocumentsRecord, String[]> SYS_READERS = createField("sys_readers", org.jooq.impl.SQLDataType.CLOB.getArrayDataType(), this, "");

	/**
	 * The column <code>public.documents.sys_editors</code>.
	 */
	public final TableField<DocumentsRecord, String[]> SYS_EDITORS = createField("sys_editors", org.jooq.impl.SQLDataType.CLOB.getArrayDataType(), this, "");

	/**
	 * The column <code>public.documents.sys_folders</code>.
	 */
	public final TableField<DocumentsRecord, String[]> SYS_FOLDERS = createField("sys_folders", org.jooq.impl.SQLDataType.CLOB.getArrayDataType(), this, "");

	/**
	 * The column <code>public.documents.sys_type</code>.
	 */
	public final TableField<DocumentsRecord, String> SYS_TYPE = createField("sys_type", org.jooq.impl.SQLDataType.CLOB, this, "");

	/**
	 * The column <code>public.documents.sys_version</code>.
	 */
	public final TableField<DocumentsRecord, String> SYS_VERSION = createField("sys_version", org.jooq.impl.SQLDataType.CLOB, this, "");

	/**
	 * The column <code>public.documents.sys_parent</code>.
	 */
	public final TableField<DocumentsRecord, String> SYS_PARENT = createField("sys_parent", org.jooq.impl.SQLDataType.CLOB, this, "");

	/**
	 * The column <code>public.documents.sys_file_path</code>.
	 */
	public final TableField<DocumentsRecord, String> SYS_FILE_PATH = createField("sys_file_path", org.jooq.impl.SQLDataType.CLOB, this, "");

	/**
	 * The column <code>public.documents.sys_file_mime_type</code>.
	 */
	public final TableField<DocumentsRecord, String> SYS_FILE_MIME_TYPE = createField("sys_file_mime_type", org.jooq.impl.SQLDataType.CLOB, this, "");

	/**
	 * The column <code>public.documents.sys_file_length</code>.
	 */
	public final TableField<DocumentsRecord, Long> SYS_FILE_LENGTH = createField("sys_file_length", org.jooq.impl.SQLDataType.BIGINT, this, "");

	/**
	 * The column <code>public.documents.data</code>.
	 */
	public final TableField<DocumentsRecord, JsonNode> DATA = createField("data", org.jooq.impl.DefaultDataType.getDefaultDataType("jsonb"), this, "", new PostgresJSONJacksonJsonNodeBinding());

	/**
	 * Create a <code>public.documents</code> table reference
	 */
	public Documents() {
		this("documents", null);
	}

	/**
	 * Create an aliased <code>public.documents</code> table reference
	 */
	public Documents(String alias) {
		this(alias, DOCUMENTS);
	}

	private Documents(String alias, Table<DocumentsRecord> aliased) {
		this(alias, aliased, null);
	}

	private Documents(String alias, Table<DocumentsRecord> aliased, Field<?>[] parameters) {
		super(alias, Public.PUBLIC, aliased, parameters, "");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Identity<DocumentsRecord, Integer> getIdentity() {
		return Keys.IDENTITY_DOCUMENTS;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public UniqueKey<DocumentsRecord> getPrimaryKey() {
		return Keys.DOCUMENTS_PKEY;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<UniqueKey<DocumentsRecord>> getKeys() {
		return Arrays.<UniqueKey<DocumentsRecord>>asList(Keys.DOCUMENTS_PKEY);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Documents as(String alias) {
		return new Documents(alias, this);
	}

	/**
	 * Rename this table
	 */
	public Documents rename(String name) {
		return new Documents(name, null);
	}
}