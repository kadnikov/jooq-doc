package ru.doccloud.document.repository;


import static ru.doccloud.document.jooq.db.tables.Documents.DOCUMENTS;
import static ru.doccloud.document.jooq.db.tables.Links.LINKS;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.DataType;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.SQLDialect;
import org.jooq.SelectField;
import org.jooq.SortField;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultDataType;
import org.jooq.impl.SQLDataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import ru.doccloud.common.exception.DocumentNotFoundException;
import ru.doccloud.common.service.DateTimeService;
import ru.doccloud.document.jooq.db.tables.Documents;
import ru.doccloud.document.jooq.db.tables.Links;
import ru.doccloud.document.jooq.db.tables.records.DocumentsRecord;
import ru.doccloud.document.jooq.db.tables.records.LinksRecord;
import ru.doccloud.document.model.Document;
import ru.doccloud.document.model.FilterBean;
import ru.doccloud.document.model.Link;
import ru.doccloud.document.model.QueryParam;

/**
 * @author Andrey Kadnikov
 */
@Repository
public class JOOQDocumentRepository implements DocumentRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(JOOQDocumentRepository.class);


    private final DateTimeService dateTimeService;

    private final DSLContext jooq;

    @Autowired
    public JOOQDocumentRepository(DateTimeService dateTimeService, DSLContext jooq) {
        this.dateTimeService = dateTimeService;
        this.jooq = jooq;
    }

    @Transactional
    @Override
    public Document add(Document documentEntry) {
        LOGGER.trace("entering add(documentEntry= {})", documentEntry);
        String[] readers = {documentEntry.getAuthor(), "admins"};
        LOGGER.trace("add(): readers {}", readers);
        DocumentsRecord persisted = jooq.insertInto(
                DOCUMENTS, DOCUMENTS.SYS_DESC, DOCUMENTS.SYS_TITLE, DOCUMENTS.SYS_TYPE, DOCUMENTS.SYS_AUTHOR,
                DOCUMENTS.SYS_READERS, DOCUMENTS.DATA, DOCUMENTS.SYS_FILE_LENGTH, DOCUMENTS.SYS_FILE_MIME_TYPE,
                DOCUMENTS.SYS_FILE_NAME, DOCUMENTS.SYS_FILE_PATH, DOCUMENTS.SYS_VERSION)
                .values(
                        documentEntry.getDescription(), documentEntry.getTitle(), documentEntry.getType(), documentEntry.getAuthor(),
                        readers, documentEntry.getData(), documentEntry.getFileLength(), documentEntry.getFileMimeType(),
                        documentEntry.getFileName(), documentEntry.getFilePath(), documentEntry.getDocVersion())
                .returning()
                .fetchOne();
        Document returned = DocumentConverter.convertQueryResultToModelObject(persisted);

        LOGGER.trace("leaving add():  added document {}", returned);

        return returned;
    }

    @Transactional
    @Override
    public Link addLink(Long headId, Long tailId) {
        LOGGER.trace("entering addLink(headId = {}, tailId={})", headId, tailId);

        LinksRecord persisted = jooq.insertInto(LINKS,LINKS.HEAD_ID,LINKS.TAIL_ID)
                .values(headId.intValue(),tailId.intValue())
                .returning()
                .fetchOne();

        Link returned = new Link(persisted.getHeadId().longValue(),persisted.getTailId().longValue());

        LOGGER.trace("leaving addLink():  added link {}", returned);

        return returned;
    }

    @Transactional
    @Override
    public Link deleteLink(Long headId, Long tailId) {
        LOGGER.trace("entering deleteLink(headId = {}, tailId={})", headId, tailId);

        int deleted = jooq.delete(LINKS)
                .where(LINKS.HEAD_ID.equal(headId.intValue()).and(LINKS.TAIL_ID.equal(tailId.intValue())))
                .execute();

        LOGGER.trace("deleteLink(): {} link entry deleted", deleted);
        Link returned = new Link(headId,tailId);

        LOGGER.trace("leaving deleteLink():  deleted link {}", returned);
        return returned;
    }

//    private DocumentsRecord createRecord(Document documentEntry) {
//        LOGGER.trace("entering createRecord(documentEntry = {}", documentEntry);
//        Timestamp currentTime = dateTimeService.getCurrentTimestamp();
//        LOGGER.trace("createRecord(): The current time is: {}", currentTime);
//
//        DocumentsRecord record = new DocumentsRecord();
//
//        record.setSysDateCr(currentTime);
//        record.setSysDesc(documentEntry.getDescription());
//        record.setSysDateMod(currentTime);
//        record.setSysTitle(documentEntry.getTitle());
//        record.setSysType(documentEntry.getType());
//
//        LOGGER.trace("leaving createRecord():  created Document {}", record);
//        return record;
//    }

    @Transactional
    @Override
    public Document delete(Long id) {
        LOGGER.trace("entering delete(id={})", id);

        Document deleted = findById(id);

        LOGGER.trace("delete(): Document was found in database {}", deleted);

        if(deleted == null)
            throw new DocumentNotFoundException("The document with id was not found in database");
        int deletedRecordCount = jooq.delete(DOCUMENTS)
                .where(DOCUMENTS.ID.equal(id.intValue()))
                .execute();

        LOGGER.trace("delete(): {} document entries deleted", deletedRecordCount);

        LOGGER.trace("leaving delete(): Returning deleted Document entry: {}", deleted);

        return deleted;
    }

    @Transactional(readOnly = true)
    @Override
    public List<Document> findAll() {
        LOGGER.trace("entering findAll()");

        List<DocumentsRecord> queryResults = jooq.selectFrom(DOCUMENTS).fetchInto(DocumentsRecord.class);

        LOGGER.trace("findAll(): Found {} Document entries, they are going to convert to model objects", queryResults);

        List<Document> documentEntries = DocumentConverter.convertQueryResultsToModelObjects(queryResults);

        LOGGER.trace("leaving findAll(): Found {} Document entries", documentEntries);

        return documentEntries;
    }

    @Transactional(readOnly = true)
    @Override
    public Page<Document> findAll(Pageable pageable, String query) {

        LOGGER.trace("entering findAll(pageSize = {}, pageNumber = {})", pageable.getPageSize(), pageable.getPageNumber());
        List<QueryParam> queryParams = getQueryParams(query);
        Condition cond = null;
        if (queryParams !=null){
	        cond = DOCUMENTS.SYS_TYPE.isNotNull();
	        cond = extendConditions(cond, queryParams);
        }
        List<DocumentsRecord> queryResults = jooq.selectFrom(DOCUMENTS)
        		.where(cond)
                .orderBy(getSortFields(pageable.getSort()))
                .limit(pageable.getPageSize()).offset(pageable.getOffset())
                .fetchInto(DocumentsRecord.class);
        
        LOGGER.trace("findAll(): Found {} Document entries, they are going to convert to model objects", queryResults);

        List<Document> documentEntries = DocumentConverter.convertQueryResultsToModelObjects(queryResults);

        LOGGER.trace("findAll(): {} document entries for page: {} ",
                documentEntries.size(),
                pageable.getPageNumber()
        );
        long totalCount = 0;
        if (queryParams !=null){
        	totalCount = findTotalCountByType(cond);
        }else{
        	totalCount = findTotalCount();
        }

        LOGGER.trace("findAll(): {} document entries matches with the like expression: {}", totalCount);

        LOGGER.trace("leaving findAll(): Found {} Document entries", documentEntries);

        return new PageImpl<>(documentEntries, pageable, totalCount);
    }


    @Override
    public Page<Document> findAllByType(String type, String[] fields, Pageable pageable, String query) {
        LOGGER.trace("entering findAllByType(type={}, fields={}, pageable={}, query={})", type, fields, pageable, query);

        ArrayList<SelectField<?>> selectedFields = new ArrayList<SelectField<?>>();
        selectedFields.add(DOCUMENTS.ID);
        selectedFields.add(DOCUMENTS.SYS_TITLE);
        selectedFields.add(DOCUMENTS.SYS_AUTHOR);
        selectedFields.add(DOCUMENTS.SYS_DATE_CR);
        selectedFields.add(DOCUMENTS.SYS_DATE_MOD);
        selectedFields.add(DOCUMENTS.SYS_DESC);
        selectedFields.add(DOCUMENTS.SYS_MODIFIER);
        selectedFields.add(DOCUMENTS.SYS_FILE_PATH);
        selectedFields.add(DOCUMENTS.SYS_TYPE);
        selectedFields.add(DOCUMENTS.SYS_FILE_NAME);
        selectedFields.add(DOCUMENTS.SYS_VERSION);
        selectedFields.add(DOCUMENTS.SYS_UUID);
        selectedFields.add(DOCUMENTS.SYS_PARENT);
        if (fields!=null){
            for (String field : fields) {
                selectedFields.add(jsonObject(DOCUMENTS.DATA, field).as(field));
            }
        }
        LOGGER.trace("findAllByType(): selectedFields: {}", selectedFields);

        List<QueryParam> queryParams = getQueryParams(query);
        Condition cond = DOCUMENTS.SYS_TYPE.equal(type);
        cond = extendConditions(cond, queryParams);
        List<Record> queryResults = jooq.select(selectedFields).from(DOCUMENTS)
                .where(cond)
                .orderBy(getSortFields(pageable.getSort()))
                .limit(pageable.getPageSize()).offset(pageable.getOffset())
                .fetch();//Into(DocumentsRecord.class);

        LOGGER.trace("findAllByType(): Found {} Document entries, they are going to convert to model objects", queryResults);

        List<Document> documentEntries = DocumentConverter.convertQueryResults(queryResults, fields);

        long totalCount = findTotalCountByType(cond);

        LOGGER.trace("findAllByType(): {} document entries matches with the like expression: {}", totalCount);

        LOGGER.trace("leaving findAllByType(): Found {} Documents", documentEntries);
        return new PageImpl<>(documentEntries, pageable, totalCount);

    }

	@Transactional(readOnly = true)
    @Override
    public Document findById(Long id) {
        LOGGER.trace("entering findById(id = {})", id);

        DocumentsRecord queryResult = jooq.selectFrom(DOCUMENTS)
                .where(DOCUMENTS.ID.equal(id.intValue()))
                .fetchOne();


        if (queryResult == null) {
            throw new DocumentNotFoundException("No Document entry found with id: " + id);
        }
        LOGGER.trace("leaving findById(): Found {}", queryResult);
        return DocumentConverter.convertQueryResultToModelObject(queryResult);
    }

    @Transactional(readOnly = true)
    @Override
    public Document findByUUID(String uuid) {
        LOGGER.debug("entering findByUUID(uuid = {})", uuid);

        DocumentsRecord queryResult = jooq.selectFrom(DOCUMENTS)
                .where(DOCUMENTS.SYS_UUID.equal( UUID.fromString(uuid)))
                .fetchOne();

        if (queryResult == null) {
            throw new DocumentNotFoundException("No Document entry found with uuid: " + uuid);
        }
        LOGGER.trace("leaving findByUUID(): Found {}", queryResult);
        return DocumentConverter.convertQueryResultToModelObject(queryResult);
    }


    @Transactional(readOnly = true)
    @Override
    public Document findSettings() {
        LOGGER.trace("entering findSettings(): try to find storage area settings in cache first");

            LOGGER.trace("storage area settings weren't found in cache. It will get from database");
            DocumentsRecord record = jooq.selectFrom(DOCUMENTS)
                    .where(DOCUMENTS.SYS_TYPE.equal("storage_area"))
                    .fetchOne();
            LOGGER.trace("findSettings(): settings record was found in db {}", record);
        if (record == null) {
            throw new DocumentNotFoundException("No Document entry found with type storageArea");
        }

            LOGGER.trace("findSettings(): storage area settings has been added to cache");


        LOGGER.trace("leaving findSettings(): Got result: {}", record);
        return DocumentConverter.convertQueryResultToModelObject(record);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<Document> findBySearchTerm(String searchTerm, Pageable pageable) {
        LOGGER.trace("entering findBySearchTerm(searchTerm={}, pageable={})", searchTerm, pageable);

        String likeExpression = "%" + searchTerm + "%";

        List<DocumentsRecord> queryResults = jooq.selectFrom(DOCUMENTS)
                .where(createWhereConditions(likeExpression))
                .orderBy(getSortFields(pageable.getSort()))
                .limit(pageable.getPageSize()).offset(pageable.getOffset())
                .fetchInto(DocumentsRecord.class);

        LOGGER.trace("findBySearchTerm(): Found {} Document entries, they are going to convert to model objects", queryResults);

        List<Document> documentEntries = DocumentConverter.convertQueryResultsToModelObjects(queryResults);

        long totalCount = findCountByLikeExpression(likeExpression);
        LOGGER.trace("findBySearchTerm(): {} document entries matches with the like expression: {}", totalCount);
        LOGGER.trace("leaving findBySearchTerm(): Found {}", documentEntries);

        return new PageImpl<>(documentEntries, pageable, totalCount);
    }

    @Transactional
    @Override
    public Document update(Document documentEntry) {
        LOGGER.trace("entering update(documentEntry={})", documentEntry);

        Timestamp currentTime = dateTimeService.getCurrentTimestamp();
        LOGGER.trace("update(): The current time is: {}", currentTime);

        int updatedRecordCount = jooq.update(DOCUMENTS)
                .set(DOCUMENTS.SYS_DESC, documentEntry.getDescription())
                .set(DOCUMENTS.SYS_DATE_MOD, currentTime)
                .set(DOCUMENTS.SYS_TITLE, documentEntry.getTitle())
                .set(DOCUMENTS.SYS_MODIFIER, documentEntry.getModifier())
                .set(DOCUMENTS.DATA, documentEntry.getData())
                .set(DOCUMENTS.SYS_FILE_PATH, documentEntry.getFilePath())
                .set(DOCUMENTS.SYS_FILE_LENGTH, documentEntry.getFileLength())
                .set(DOCUMENTS.SYS_FILE_MIME_TYPE, documentEntry.getFileMimeType())
                .set(DOCUMENTS.SYS_FILE_NAME, documentEntry.getFileName())
                .set(DOCUMENTS.SYS_VERSION, documentEntry.getDocVersion())
                .set(DOCUMENTS.SYS_TYPE, documentEntry.getType())
                .where(DOCUMENTS.ID.equal(documentEntry.getId().intValue()))
                .execute();

        LOGGER.trace("leaving update(): Updated {}", updatedRecordCount);
        //If you are using Firebird or PostgreSQL databases, you can use the RETURNING
        //clause in the update statement (and avoid the extra select clause):
        //http://www.jooq.org/doc/3.2/manual/sql-building/sql-statements/update-statement/#N11102

        return findById(documentEntry.getId());
    }

    @Transactional
    @Override
    public Document updateFileInfo(Document documentEntry) {
        LOGGER.trace("entering updateFileInfo(documentEntry={})", documentEntry);

        Timestamp currentTime = dateTimeService.getCurrentTimestamp();

        int updatedRecordCount = jooq.update(DOCUMENTS)
                .set(DOCUMENTS.SYS_FILE_PATH, documentEntry.getFilePath())
                .set(DOCUMENTS.SYS_DATE_MOD, currentTime)
                .set(DOCUMENTS.SYS_MODIFIER, documentEntry.getModifier())
                .set(DOCUMENTS.SYS_FILE_LENGTH, documentEntry.getFileLength())
                .set(DOCUMENTS.SYS_FILE_MIME_TYPE, documentEntry.getFileMimeType())
                .set(DOCUMENTS.SYS_FILE_NAME, documentEntry.getFileName())
                .where(DOCUMENTS.ID.equal(documentEntry.getId().intValue()))
                .execute();

        LOGGER.trace("leaving updateFileInfo(): Updated {} document entry", updatedRecordCount);
        //If you are using Firebird or PostgreSQL databases, you can use the RETURNING
        //clause in the update statement (and avoid the extra select clause):
        //http://www.jooq.org/doc/3.2/manual/sql-building/sql-statements/update-statement/#N11102

        return findById(documentEntry.getId());
    }
    
    @Transactional
    @Override
    public Document setParent(Document documentEntry) {
        LOGGER.trace("entering updateFileInfo(documentEntry={})", documentEntry);

        Timestamp currentTime = dateTimeService.getCurrentTimestamp();

        int updatedRecordCount = jooq.update(DOCUMENTS)
                .set(DOCUMENTS.SYS_PARENT, documentEntry.getParent())
                .where(DOCUMENTS.ID.equal(documentEntry.getId().intValue()))
                .execute();

        LOGGER.trace("leaving updateFileInfo(): Updated {} document entry", updatedRecordCount);
        //If you are using Firebird or PostgreSQL databases, you can use the RETURNING
        //clause in the update statement (and avoid the extra select clause):
        //http://www.jooq.org/doc/3.2/manual/sql-building/sql-statements/update-statement/#N11102

        return findById(documentEntry.getId());
    }


    private static Field<Object> jsonObject(Field<?> field, String name) {
        return DSL.field("{0}->{1}", Object.class, field, DSL.inline(name));
    }

    private static Field<Object> jsonText(Field<?> field, String name) {
        return DSL.field("{0}->>{1}", Object.class, field, DSL.inline(name));
    }

    @Override
    public List<Document> findAllByParent(Long parent) {

        LOGGER.trace("entering findAllByParent(parent = {})", parent);
        
    	List<DocumentsRecord>  queryResults = jooq.selectFrom(DOCUMENTS)
                .where(DOCUMENTS.SYS_PARENT.equal(parent.toString()))
                .fetchInto(DocumentsRecord.class);


        LOGGER.trace("findAllByParent(): Found {} Document entries, they are going to convert to model objects", queryResults);

        List<Document> documentEntries = DocumentConverter.convertQueryResultsToModelObjects(queryResults);

        LOGGER.trace("leaving findAllByParent(): Found {}", documentEntries);

        return documentEntries;
    }

    @Override
    public List<Document> findAllByLinkParent(Long parent) {
        LOGGER.trace("entering findAllByParent(parent = {})", parent);

        Documents d = DOCUMENTS.as("d");
        Links l = LINKS.as("l");
        Documents t = DOCUMENTS.as("t");

        List<DocumentsRecord> queryResults = jooq.select(d.ID, d.SYS_TITLE, d.SYS_AUTHOR, d.SYS_DATE_CR, d.SYS_DATE_MOD, d.SYS_DESC, d.SYS_MODIFIER, d.SYS_FILE_PATH, d.SYS_TYPE, d.SYS_FILE_NAME, d.SYS_UUID)
                .from(d
                        .join(l
                                .join(t)
                                .on(t.ID.equal(l.HEAD_ID)))
                        .on(d.ID.equal(l.TAIL_ID)))
                .where(t.ID.equal(parent.intValue()))
                .fetchInto(DocumentsRecord.class);

        LOGGER.trace("findAllByParent(): Found {} Document entries, they are going to convert to model objects", queryResults);

        List<Document> documentEntries = DocumentConverter.convertQueryResultsToModelObjects(queryResults);

        LOGGER.trace("leaving findAllByParent(): Found {}", documentEntries);

        return documentEntries;
    }

    @Override
    public List<Document> findParents(Long docId) {
        LOGGER.trace("entering findParents(docId = {})", docId);

        Documents d = DOCUMENTS.as("d");
        Links l = LINKS.as("l");
        Documents t = DOCUMENTS.as("t");

        List<DocumentsRecord> queryResults = jooq.select(d.ID, d.SYS_TITLE, d.SYS_AUTHOR, d.SYS_DATE_CR, d.SYS_DATE_MOD, d.SYS_DESC, d.SYS_MODIFIER, d.SYS_FILE_PATH, d.SYS_TYPE, d.SYS_FILE_NAME, d.SYS_UUID)
                .from(d
                        .join(l
                                .join(t)
                                .on(t.ID.equal(l.TAIL_ID)))
                        .on(d.ID.equal(l.HEAD_ID)))
                .where(t.ID.equal(docId.intValue()))
                .fetchInto(DocumentsRecord.class);

        LOGGER.trace("findParents(): Found {} Document entries, they are going to convert to model objects", queryResults);

        List<Document> documentEntries = DocumentConverter.convertQueryResultsToModelObjects(queryResults);

        LOGGER.debug("leaving findParents(): Found: {}", documentEntries);

        return documentEntries;
    }

    @Transactional
    @Override
    public void setUser() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        LOGGER.trace("Current Remote User - ",request.getRemoteUser());
        jooq.execute("SET my.username = '"+request.getRemoteUser()+"'");

    }

    @Transactional
    @Override
    public void setUser(String userName) {
        LOGGER.trace("Current User - {}",userName);
        jooq.execute("SET my.username = '"+userName+"'");

        //jooq.execute("SELECT current_setting('my.username') FROM documents LIMIT 1;");
    }

    private long findCountByLikeExpression(String likeExpression) {
        LOGGER.trace("entering findCountByLikeExpression(likeExpression={})", likeExpression);

        long resultCount = jooq.fetchCount(
                jooq.select()
                        .from(DOCUMENTS)
                        .where(createWhereConditions(likeExpression))
        );

        LOGGER.trace("leaving findCountByLikeExpression(): Found search result count: {}", resultCount);

        return resultCount;
    }

    private long findTotalCount() {
        LOGGER.trace("entering findTotalCount()");

        long resultCount = jooq.selectCount()
        		   .from(DOCUMENTS)
        		   .fetchOne(0, long.class);		

        LOGGER.trace("leaving findTotalCount(): Found search result count: {}", resultCount);

        return resultCount;
    }

    private long findTotalCountByType(Condition cond) {
        LOGGER.trace("entering findTotalCountByType(cond={})", cond);

        long resultCount = jooq.fetchCount(
                jooq.selectFrom(DOCUMENTS)
                        .where(cond)
        );

        LOGGER.trace("leaving findTotalCountByType(): Found search result count: {}", resultCount);

        return resultCount;
    }


    private Condition createWhereConditions(String likeExpression) {
        return DOCUMENTS.SYS_DESC.likeIgnoreCase(likeExpression)
                .or(DOCUMENTS.SYS_TITLE.likeIgnoreCase(likeExpression));
    }

    private List<QueryParam> getQueryParams(String query) {
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

	private Condition extendConditions(Condition cond, List<QueryParam> queryParams) {
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
                            cond = cond.and(getFilterField(param).equal(getFieldValue(param)));
                            break;
                        case "ne":
                            cond = cond.and(getFilterField(param).notEqual(getFieldValue(param)));
                            break;
                        case "lt":
                            cond = cond.and(getFilterField(param).lessThan(getFieldValue(param)));
                            break;
                        case "le":
                            cond = cond.and(getFilterField(param).lessOrEqual(getFieldValue(param)));
                            break;
                        case "gt":
                            cond = cond.and(getFilterField(param).greaterThan(getFieldValue(param)));
                            break;
                        case "ge":
                            cond = cond.and(getFilterField(param).greaterOrEqual(getFieldValue(param)));
                            break;
                        case "bw":
                            cond = cond.and(getFilterField(param).like(param.getValue()+"%"));
                            break;
                        case "bn":
                            cond = cond.and(getFilterField(param).notLike(param.getValue()+"%"));
                            break;
                        case "in":
                            cond = cond.and(getFilterField(param).in(getFieldValue(param)));
                            break;
                        case "ni":
                            cond = cond.and(getFilterField(param).notIn(getFieldValue(param)));
                            break;
                        case "ew":
                            cond = cond.and(getFilterField(param).like("%"+param.getValue()));
                            break;
                        case "en":
                            cond = cond.and(getFilterField(param).notLike("%"+param.getValue()));
                            break;
                        case "cn":
                            cond = cond.and(getFilterField(param).like("%"+param.getValue()+"%"));
                            break;
                        case "nc":
                            cond = cond.and(getFilterField(param).notLike("%"+param.getValue()+"%"));
                            break;
                    }
                }
            }
        return cond;
	}


    private Collection<SortField<?>> getSortFields(Sort sortSpecification) {
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

            Field<Object> tableField = getTableField(sortFieldName);
            SortField<?> querySortField = convertTableFieldToSortField(tableField, sortDirection);

            LOGGER.trace("getSortFields(): tableField: {} and querySortField: {}", tableField, querySortField);
            querySortFields.add(querySortField);
        }

        LOGGER.trace("leaving getSortFields(): querySortFields {}", querySortFields);

        return querySortFields;
    }

    private Field<Object> getTableField(String sortFieldName) {
        LOGGER.trace("entering getTableField(sortFieldName={})", sortFieldName);
        Field<Object> sortField = null;
        try {
            java.lang.reflect.Field tableField = DOCUMENTS.getClass().getField(sortFieldName.toUpperCase());
            sortField = (TableField) tableField.get(DOCUMENTS);
            LOGGER.trace("getTableField(): sortField - {}", sortField);
        } catch (NoSuchFieldException | IllegalAccessException ex) {
            LOGGER.trace("getTableField(): Could not find table field: {}, Try to search in JSON data", sortFieldName);
            sortField = jsonObject(DOCUMENTS.DATA, sortFieldName);
            LOGGER.trace("getTableField(): sort field in  JSON data", sortField);
        }

        LOGGER.trace("leaving getTableField()", sortField);
        return sortField;
    }
    
    private Field<Object> getFilterField(QueryParam param) {
        Field<Object> sortField = null;
        try {
            java.lang.reflect.Field tableField = DOCUMENTS.getClass().getField(param.getField().toUpperCase());
            sortField = (TableField) tableField.get(DOCUMENTS);
            LOGGER.trace("getFilterField(): sortField - {}", sortField);
        } catch (NoSuchFieldException | IllegalAccessException ex) {
        	try {
        		int intval = Integer.parseInt(param.getValue());
        		sortField = jsonObject(DOCUMENTS.DATA, param.getField());
        	}catch (NumberFormatException exN){
        		try{
        			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        			 dateFormat.setLenient(false);
        			 java.util.Date dateval = parseFully(dateFormat,param.getValue());
	        		sortField = jsonObject(DOCUMENTS.DATA, param.getField());
        		}catch (ParseException exP){
        			sortField = jsonText(DOCUMENTS.DATA, param.getField());
        		}
        	}
            LOGGER.trace("getFilterField(): sort field in  JSON data", sortField);
        }

        LOGGER.trace("leaving getTableField()", sortField);
        return sortField;
    }
    
    private static java.util.Date parseFully(DateFormat format, String text) 
            throws ParseException {
          ParsePosition position = new ParsePosition(0);
          java.util.Date date = format.parse(text, position);
          if (position.getIndex() == text.length()) {
              return date;
          }
          if (date == null) {
              throw new ParseException("Date could not be parsed: " + text,
                                       position.getErrorIndex());
          }
          throw new ParseException("Date was parsed incompletely: " + text,
                                   position.getIndex());
      }
    
    private Field<Object> getFieldValue(QueryParam param) {
    	Field<Object> result = null;
        DataType<Object> JSONB = new DefaultDataType<Object>(SQLDialect.POSTGRES, SQLDataType.OTHER, "jsonb");
        DataType<Object> intType = new DefaultDataType<Object>(SQLDialect.POSTGRES, SQLDataType.OTHER, "int");
        DataType<Object> timeType = new DefaultDataType<Object>(SQLDialect.POSTGRES, SQLDataType.OTHER, "timestamp");
        
        try {
            java.lang.reflect.Field tableField = DOCUMENTS.getClass().getField(param.getField().toUpperCase());
            Field<Object> sortField = (TableField) tableField.get(DOCUMENTS);
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
        			 java.util.Date dateval = parseFully(dateFormat,param.getValue());
        			 result =  DSL.val(param.getValue()).cast(JSONB);
        		}catch (ParseException exP){
        			result =  DSL.val(param.getValue());
        		}
        	}
            
        }
        LOGGER.trace("getFieldValue(): Result {}", result);
        return result;
    }
    

    private SortField<?> convertTableFieldToSortField(Field<Object> tableField, Sort.Direction sortDirection) {
        if (sortDirection == Sort.Direction.ASC) {
            return tableField.asc();
        }
        else {
            return tableField.desc();
        }
    }

    private static class DocumentConverter{
        private static Document convertQueryResultToModelObject(Record queryResult, String[] fields) {
            ObjectNode data = JsonNodeFactory.instance.objectNode();
            ObjectMapper mapper = new ObjectMapper();
            if (fields!=null){
                for (String field : fields) {
                    if (queryResult.getValue(field)!=null){
                        try {
                            data.put(field,mapper.readTree(queryResult.getValue(field).toString()));
                        } catch (IllegalArgumentException | IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            return  Document.getBuilder(queryResult.getValue(DOCUMENTS.SYS_TITLE))
                    .description(queryResult.getValue(DOCUMENTS.SYS_DESC))
                    .type(queryResult.getValue(DOCUMENTS.SYS_TYPE))
                    .id(queryResult.getValue(DOCUMENTS.ID).longValue())
                    .creationTime(queryResult.getValue(DOCUMENTS.SYS_DATE_CR))
                    .modificationTime(queryResult.getValue(DOCUMENTS.SYS_DATE_MOD))
                    .author(queryResult.getValue(DOCUMENTS.SYS_AUTHOR))
                    .modifier(queryResult.getValue(DOCUMENTS.SYS_MODIFIER))
                    .filePath(queryResult.getValue(DOCUMENTS.SYS_FILE_PATH))
                    .fileName(queryResult.getValue(DOCUMENTS.SYS_FILE_NAME))
                    .uuid(queryResult.getValue(DOCUMENTS.SYS_UUID))
                    .parent(queryResult.getValue(DOCUMENTS.SYS_PARENT))
                    .data(data)
                    .build();
        }


        private static Document convertQueryResultToModelObject(DocumentsRecord queryResult) {
            return Document.getBuilder(queryResult.getSysTitle())
                    .creationTime(queryResult.getSysDateCr())
                    .description(queryResult.getSysDesc())
                    .type(queryResult.getSysType())
                    .data(queryResult.getData())
                    .id(queryResult.getId().longValue())
                    .modificationTime(queryResult.getSysDateMod())
                    .author(queryResult.getSysAuthor())
                    .modifier(queryResult.getSysModifier())
                    .filePath(queryResult.getSysFilePath())
                    .fileMimeType(queryResult.getSysFileMimeType())
                    .fileLength(queryResult.getSysFileLength())
                    .fileName(queryResult.getSysFileName())
                    .docVersion(queryResult.getSysVersion())
                    .parent(queryResult.getSysParent())
                    .uuid(queryResult.getSysUuid())
                    .build();
        }

        private static List<Document> convertQueryResultsToModelObjects(List<DocumentsRecord> queryResults) {
            List<Document> documentEntries = new ArrayList<>();

            for (DocumentsRecord queryResult : queryResults) {
                Document documentEntry = DocumentConverter.convertQueryResultToModelObject(queryResult);
                documentEntries.add(documentEntry);
            }

            return documentEntries;
        }

        private static List<Document> convertQueryResults(List<Record> queryResults, String[] fields) {
            List<Document> documentEntries = new ArrayList<>();

            for (Record queryResult : queryResults) {
                documentEntries.add(DocumentConverter.convertQueryResultToModelObject(queryResult, fields));
            }

            return documentEntries;
        }
    }

}