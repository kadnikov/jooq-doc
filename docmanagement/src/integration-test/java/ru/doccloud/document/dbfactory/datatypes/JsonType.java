package ru.doccloud.document.dbfactory.datatypes;

import org.dbunit.dataset.datatype.AbstractDataType;
import org.dbunit.dataset.datatype.TypeCastException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;


public class JsonType extends AbstractDataType {
    private static final Logger LOGGER = LoggerFactory.getLogger(JsonType.class);

    public JsonType() {
        super("jsonb", Types.OTHER, String.class, false);
    }

    @Override
    public Object typeCast(Object value) throws TypeCastException {
        LOGGER.info("typeCast(object={})", value);
        return value.toString();
    }

    @Override
    public Object getSqlValue(int column, ResultSet resultSet) throws SQLException {
        LOGGER.trace("getSqlValue({}, {})", column, resultSet);
        if (resultSet.wasNull()) {
            return null;
        }
        return resultSet.getString(column);
    }

    @Override
    public void setSqlValue(Object value, int column, PreparedStatement statement) throws SQLException {
        if (value == null) {
            statement.setNull(column, Types.OTHER);
        }
        statement.setObject(column, value.toString(), Types.OTHER);
    }
}
