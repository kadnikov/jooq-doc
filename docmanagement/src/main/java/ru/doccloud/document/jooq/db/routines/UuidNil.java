/**
 * This class is generated by jOOQ
 */
package ru.doccloud.document.jooq.db.routines;


import java.util.UUID;

import javax.annotation.Generated;

import org.jooq.Parameter;
import org.jooq.impl.AbstractRoutine;

import ru.doccloud.document.jooq.db.Public;


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
public class UuidNil extends AbstractRoutine<UUID> {

	private static final long serialVersionUID = 1246712422;

	/**
	 * The parameter <code>public.uuid_nil.RETURN_VALUE</code>.
	 */
	public static final Parameter<UUID> RETURN_VALUE = createParameter("RETURN_VALUE", org.jooq.impl.SQLDataType.UUID, false);

	/**
	 * Create a new routine call instance
	 */
	public UuidNil() {
		super("uuid_nil", Public.PUBLIC, org.jooq.impl.SQLDataType.UUID);

		setReturnParameter(RETURN_VALUE);
	}
}
