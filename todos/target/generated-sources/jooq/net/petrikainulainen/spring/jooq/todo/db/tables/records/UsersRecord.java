/**
 * This class is generated by jOOQ
 */
package net.petrikainulainen.spring.jooq.todo.db.tables.records;


import javax.annotation.Generated;

import net.petrikainulainen.spring.jooq.todo.db.tables.Users;

import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record12;
import org.jooq.Row;
import org.jooq.Row12;
import org.jooq.impl.UpdatableRecordImpl;


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
public class UsersRecord extends UpdatableRecordImpl<UsersRecord> implements Record12<String, String, String[], String, String, String, Long, Boolean, String, Integer, String, Integer> {

	private static final long serialVersionUID = -665353238;

	/**
	 * Setter for <code>public.users.userid</code>.
	 */
	public void setUserid(String value) {
		setValue(0, value);
	}

	/**
	 * Getter for <code>public.users.userid</code>.
	 */
	public String getUserid() {
		return (String) getValue(0);
	}

	/**
	 * Setter for <code>public.users.password</code>.
	 */
	public void setPassword(String value) {
		setValue(1, value);
	}

	/**
	 * Getter for <code>public.users.password</code>.
	 */
	public String getPassword() {
		return (String) getValue(1);
	}

	/**
	 * Setter for <code>public.users.groups</code>.
	 */
	public void setGroups(String[] value) {
		setValue(2, value);
	}

	/**
	 * Getter for <code>public.users.groups</code>.
	 */
	public String[] getGroups() {
		return (String[]) getValue(2);
	}

	/**
	 * Setter for <code>public.users.fullname</code>.
	 */
	public void setFullname(String value) {
		setValue(3, value);
	}

	/**
	 * Getter for <code>public.users.fullname</code>.
	 */
	public String getFullname() {
		return (String) getValue(3);
	}

	/**
	 * Setter for <code>public.users.avatar</code>.
	 */
	public void setAvatar(String value) {
		setValue(4, value);
	}

	/**
	 * Getter for <code>public.users.avatar</code>.
	 */
	public String getAvatar() {
		return (String) getValue(4);
	}

	/**
	 * Setter for <code>public.users.email</code>.
	 */
	public void setEmail(String value) {
		setValue(5, value);
	}

	/**
	 * Getter for <code>public.users.email</code>.
	 */
	public String getEmail() {
		return (String) getValue(5);
	}

	/**
	 * Setter for <code>public.users.created</code>.
	 */
	public void setCreated(Long value) {
		setValue(6, value);
	}

	/**
	 * Getter for <code>public.users.created</code>.
	 */
	public Long getCreated() {
		return (Long) getValue(6);
	}

	/**
	 * Setter for <code>public.users.validated</code>.
	 */
	public void setValidated(Boolean value) {
		setValue(7, value);
	}

	/**
	 * Getter for <code>public.users.validated</code>.
	 */
	public Boolean getValidated() {
		return (Boolean) getValue(7);
	}

	/**
	 * Setter for <code>public.users.validationcode</code>.
	 */
	public void setValidationcode(String value) {
		setValue(8, value);
	}

	/**
	 * Getter for <code>public.users.validationcode</code>.
	 */
	public String getValidationcode() {
		return (String) getValue(8);
	}

	/**
	 * Setter for <code>public.users.category</code>.
	 */
	public void setCategory(Integer value) {
		setValue(9, value);
	}

	/**
	 * Getter for <code>public.users.category</code>.
	 */
	public Integer getCategory() {
		return (Integer) getValue(9);
	}

	/**
	 * Setter for <code>public.users.details</code>.
	 */
	public void setDetails(String value) {
		setValue(10, value);
	}

	/**
	 * Getter for <code>public.users.details</code>.
	 */
	public String getDetails() {
		return (String) getValue(10);
	}

	/**
	 * Setter for <code>public.users.status</code>.
	 */
	public void setStatus(Integer value) {
		setValue(11, value);
	}

	/**
	 * Getter for <code>public.users.status</code>.
	 */
	public Integer getStatus() {
		return (Integer) getValue(11);
	}

	// -------------------------------------------------------------------------
	// Primary key information
	// -------------------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Record1<String> key() {
		return (Record1) super.key();
	}

	// -------------------------------------------------------------------------
	// Record12 type implementation
	// -------------------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Row12<String, String, String[], String, String, String, Long, Boolean, String, Integer, String, Integer> fieldsRow() {
		return (Row12) super.fieldsRow();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Row12<String, String, String[], String, String, String, Long, Boolean, String, Integer, String, Integer> valuesRow() {
		return (Row12) super.valuesRow();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Field<String> field1() {
		return Users.USERS.USERID;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Field<String> field2() {
		return Users.USERS.PASSWORD;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Field<String[]> field3() {
		return Users.USERS.GROUPS;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Field<String> field4() {
		return Users.USERS.FULLNAME;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Field<String> field5() {
		return Users.USERS.AVATAR;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Field<String> field6() {
		return Users.USERS.EMAIL;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Field<Long> field7() {
		return Users.USERS.CREATED;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Field<Boolean> field8() {
		return Users.USERS.VALIDATED;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Field<String> field9() {
		return Users.USERS.VALIDATIONCODE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Field<Integer> field10() {
		return Users.USERS.CATEGORY;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Field<String> field11() {
		return Users.USERS.DETAILS;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Field<Integer> field12() {
		return Users.USERS.STATUS;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String value1() {
		return getUserid();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String value2() {
		return getPassword();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String[] value3() {
		return getGroups();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String value4() {
		return getFullname();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String value5() {
		return getAvatar();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String value6() {
		return getEmail();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Long value7() {
		return getCreated();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Boolean value8() {
		return getValidated();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String value9() {
		return getValidationcode();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Integer value10() {
		return getCategory();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String value11() {
		return getDetails();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Integer value12() {
		return getStatus();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public UsersRecord value1(String value) {
		setUserid(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public UsersRecord value2(String value) {
		setPassword(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public UsersRecord value3(String[] value) {
		setGroups(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public UsersRecord value4(String value) {
		setFullname(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public UsersRecord value5(String value) {
		setAvatar(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public UsersRecord value6(String value) {
		setEmail(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public UsersRecord value7(Long value) {
		setCreated(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public UsersRecord value8(Boolean value) {
		setValidated(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public UsersRecord value9(String value) {
		setValidationcode(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public UsersRecord value10(Integer value) {
		setCategory(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public UsersRecord value11(String value) {
		setDetails(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public UsersRecord value12(Integer value) {
		setStatus(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public UsersRecord values(String value1, String value2, String[] value3, String value4, String value5, String value6, Long value7, Boolean value8, String value9, Integer value10, String value11, Integer value12) {
		value1(value1);
		value2(value2);
		value3(value3);
		value4(value4);
		value5(value5);
		value6(value6);
		value7(value7);
		value8(value8);
		value9(value9);
		value10(value10);
		value11(value11);
		value12(value12);
		return this;
	}

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * Create a detached UsersRecord
	 */
	public UsersRecord() {
		super(Users.USERS);
	}

	/**
	 * Create a detached, initialised UsersRecord
	 */
	public UsersRecord(String userid, String password, String[] groups, String fullname, String avatar, String email, Long created, Boolean validated, String validationcode, Integer category, String details, Integer status) {
		super(Users.USERS);

		setValue(0, userid);
		setValue(1, password);
		setValue(2, groups);
		setValue(3, fullname);
		setValue(4, avatar);
		setValue(5, email);
		setValue(6, created);
		setValue(7, validated);
		setValue(8, validationcode);
		setValue(9, category);
		setValue(10, details);
		setValue(11, status);
	}
}