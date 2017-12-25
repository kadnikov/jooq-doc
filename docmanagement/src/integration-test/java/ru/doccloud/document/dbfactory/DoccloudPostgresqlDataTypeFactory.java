package ru.doccloud.document.dbfactory;

import org.dbunit.dataset.datatype.DataType;
import org.dbunit.dataset.datatype.DataTypeException;
import org.dbunit.ext.postgresql.PostgresqlDataTypeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.doccloud.document.dbfactory.datatypes.JsonType;

import java.sql.Types;


public class DoccloudPostgresqlDataTypeFactory extends PostgresqlDataTypeFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(DoccloudPostgresqlDataTypeFactory.class);

    @Override
    public DataType createDataType(int sqlType, String sqlTypeName) throws DataTypeException {
        LOGGER.info("entering createDataType(sqlType={}, sqlTypeName={})", sqlType, sqlTypeName);

        if(sqlType == Types.OTHER)
            if ("jsonb".equals(sqlTypeName))
                return new JsonType();
        return super.createDataType(sqlType, sqlTypeName);
    }
}
