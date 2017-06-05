package ru.doccloud.repository.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultDataType;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import ru.doccloud.common.DateHelper;
import ru.doccloud.document.model.FilterBean;
import ru.doccloud.document.model.QueryParam;
import ru.doccloud.repository.impl.SystemRepositoryImpl;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static ru.doccloud.document.jooq.db.tables.System.SYSTEM;

public class DataQueryHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(SystemRepositoryImpl.class);




    public static Field<Object> getFieldValue(QueryParam param) {
        Field<Object> result = null;
        DataType<Object> JSONB = new DefaultDataType<Object>(SQLDialect.POSTGRES, SQLDataType.OTHER, "jsonb");
        DataType<Object> intType = new DefaultDataType<Object>(SQLDialect.POSTGRES, SQLDataType.OTHER, "int");
        DataType<Object> timeType = new DefaultDataType<Object>(SQLDialect.POSTGRES, SQLDataType.OTHER, "timestamp");

        try {
            java.lang.reflect.Field tableField = SYSTEM.getClass().getField(param.getField().toUpperCase());
            Field<Object> sortField = (TableField) tableField.get(SYSTEM);
            LOGGER.trace("getFieldValue(): Field {}, type {}", sortField, sortField.getDataType());

            if (sortField.getDataType().isNumeric()){
                LOGGER.trace("getFieldValue(): integer");
                result = DSL.val(param.getValue()).cast(intType);
            }else if(sortField.getDataType().isDateTime()){
                LOGGER.trace("getFieldValue(): Timestamp");
                result = DSL.val(param.getValue()).cast(timeType);
            }else{
                result = DSL.val(param.getValue());
            }

        } catch (NoSuchFieldException | IllegalAccessException ex) {
            LOGGER.trace("getFieldValue(): Could not find table field: {}, Cast to JSONB", param);
            try {
                int intval = Integer.parseInt(param.getValue());
                result =  DSL.val(param.getValue()).cast(JSONB);
            }catch (NumberFormatException exN){
                try{
                    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    dateFormat.setLenient(false);
                    java.util.Date dateval = DateHelper.parseFully(dateFormat,param.getValue());
                    result =  DSL.val(param.getValue()).cast(JSONB);
                }catch (ParseException exP){
                    result =  DSL.val(param.getValue());
                }
            }

        }
        LOGGER.trace("getFieldValue(): Result {}", result);
        return result;
    }


    public static SortField<?> convertTableFieldToSortField(Field<Object> tableField, Sort.Direction sortDirection) {
        if (sortDirection == Sort.Direction.ASC) {
            return tableField.asc();
        }
        else {
            return tableField.desc();
        }
    }


    public static Collection<SortField<?>> getSortFields(Sort sortSpecification, TableImpl<?> table, Field<?> field) {
        LOGGER.trace("entering getSortFields(sortSpecification={})", sortSpecification);
        Collection<SortField<?>> querySortFields = new ArrayList<>();

        if (sortSpecification == null) {
            LOGGER.trace("getSortFields(): No sort specification found. Returning empty collection -> no sorting is done.");
            return querySortFields;
        }

        for (Sort.Order specifiedField : sortSpecification) {
            String sortFieldName = specifiedField.getProperty();
            Sort.Direction sortDirection = specifiedField.getDirection();
            LOGGER.trace("getSortFields(): Getting sort field with name: {} and direction: {}", sortFieldName, sortDirection);

            Field<Object> tableField = getTableField(sortFieldName, table, field);
            SortField<?> querySortField = DataQueryHelper.convertTableFieldToSortField(tableField, sortDirection);

            LOGGER.trace("getSortFields(): tableField: {} and querySortField: {}", tableField, querySortField);
            querySortFields.add(querySortField);
        }

        LOGGER.trace("leaving getSortFields(): querySortFields {}", querySortFields);

        return querySortFields;
    }

    public static Condition createWhereConditions(String likeExpression, Field<?> fieldDesc, Field<?> fieldTitle) {
        return fieldDesc.likeIgnoreCase(likeExpression)
                .or(fieldTitle.likeIgnoreCase(likeExpression));
    }

    private static Field<Object> getTableField(String sortFieldName, TableImpl<?> table, Field<?> field) {
        LOGGER.trace("entering getTableField(sortFieldName={})", sortFieldName);
        Field<Object> sortField = null;
        try {
            java.lang.reflect.Field tableField = table.getClass().getField(sortFieldName.toUpperCase());
            sortField = (TableField) tableField.get(table);
            LOGGER.trace("getTableField(): sortField - {}", sortField);
        } catch (NoSuchFieldException | IllegalAccessException ex) {
            LOGGER.trace("getTableField(): Could not find table field: {}, Try to search in JSON data", sortFieldName);
            sortField = jsonObject(field, sortFieldName);
            LOGGER.trace("getTableField(): sort field in  JSON data", sortField);
        }

        LOGGER.trace("leaving getTableField()", sortField);
        return sortField;
    }

    private static Field<Object> getFilterField(QueryParam param, TableImpl<?> table, Field<?> field) {
        Field<Object> sortField = null;
        try {
            java.lang.reflect.Field tableField = table.getClass().getField(param.getField().toUpperCase());
            sortField = (TableField) tableField.get(table);
            LOGGER.trace("getFilterField(): sortField - {}", sortField);
        } catch (NoSuchFieldException | IllegalAccessException ex) {
            try {
                int intval = Integer.parseInt(param.getValue());
                sortField = jsonObject(field, param.getField());
            }catch (NumberFormatException exN){
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                dateFormat.setLenient(false);
                sortField = jsonObject(field, param.getField());
            }
            LOGGER.trace("getFilterField(): sort field in  JSON data", sortField);
        }

        LOGGER.trace("leaving getTableField()", sortField);
        return sortField;
    }

    public static Field<Object> jsonObject(Field<?> field, String name) {
        return DSL.field("{0}->{1}", Object.class, field, DSL.inline(name));
    }

    private static Field<Object> jsonText(Field<?> field, String name) {
        return DSL.field("{0}->>{1}", Object.class, field, DSL.inline(name));
    }

    public static Condition extendConditions(Condition cond, List<QueryParam> queryParams, TableImpl<?> table, Field<?> field) {
        if (queryParams !=null)
            for (QueryParam param : queryParams) {
                LOGGER.trace("extendConditions: Param {} {} {} ",param.getField(),param.getOperand(),param.getValue());
                if (param.getOperand()!=null){

//        	    // ['eq','ne','lt','le','gt','ge','bw','bn','in','ni','ew','en','cn','nc']
//                    todo rewrite using enum implementation
                    final String operand = param.getOperand().toLowerCase();

                    LOGGER.trace("extendConditions: operand ",operand);
                    switch (operand)
                    {
                        case "eq":
                            cond = cond.and(getFilterField(param, table, field).equal(getFieldValue(param)));
                            break;
                        case "ne":
                            cond = cond.and(getFilterField(param, table, field).notEqual(getFieldValue(param)));
                            break;
                        case "lt":
                            cond = cond.and(getFilterField(param, table, field).lessThan(getFieldValue(param)));
                            break;
                        case "le":
                            cond = cond.and(getFilterField(param, table, field).lessOrEqual(getFieldValue(param)));
                            break;
                        case "gt":
                            cond = cond.and(getFilterField(param, table, field).greaterThan(getFieldValue(param)));
                            break;
                        case "ge":
                            cond = cond.and(getFilterField(param, table, field).greaterOrEqual(getFieldValue(param)));
                            break;
                        case "bw":
                            cond = cond.and(getFilterField(param, table, field).like(param.getValue()+"%"));
                            break;
                        case "bn":
                            cond = cond.and(getFilterField(param, table, field).notLike(param.getValue()+"%"));
                            break;
                        case "in":
                            cond = cond.and(getFilterField(param, table, field).in(getFieldValue(param)));
                            break;
                        case "ni":
                            cond = cond.and(getFilterField(param, table, field).notIn(getFieldValue(param)));
                            break;
                        case "ew":
                            cond = cond.and(getFilterField(param, table, field).like("%"+param.getValue()));
                            break;
                        case "en":
                            cond = cond.and(getFilterField(param, table, field).notLike("%"+param.getValue()));
                            break;
                        case "cn":
                            cond = cond.and(getFilterField(param, table, field).like("%"+param.getValue()+"%"));
                            break;
                        case "nc":
                            cond = cond.and(getFilterField(param, table, field).notLike("%"+param.getValue()+"%"));
                            break;
                    }
                }
            }
        return cond;
    }



    public static List<QueryParam> getQueryParams(String query) {
        FilterBean filter = null;
        List<QueryParam> queryParams = null;
        LOGGER.trace("Query for search - {}", query);
        ObjectMapper mapper = new ObjectMapper();
        if (query!=null){
            try {
                filter = mapper.readValue(query, new TypeReference<FilterBean>(){});
                queryParams = filter.getMrules();
                LOGGER.trace("findAllByType(): List of params - {} {}", queryParams.toString(), queryParams.size());
            } catch (IOException e) {
                LOGGER.error("Error parsing JSON {}",e.getLocalizedMessage());
                e.printStackTrace();
            }
        }
        return queryParams;
    }
}
