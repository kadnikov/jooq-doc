package ru.doccloud.repository.impl;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.SelectField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.doccloud.common.exception.DocumentNotFoundException;
import ru.doccloud.common.service.DateTimeService;
import ru.doccloud.common.util.JsonNodeParser;
import ru.doccloud.document.jooq.db.tables.records.SystemRecord;
import ru.doccloud.document.model.QueryParam;
import ru.doccloud.document.model.SystemDocument;
import ru.doccloud.repository.SystemRepository;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static ru.doccloud.document.jooq.db.tables.System.SYSTEM;
import static ru.doccloud.repository.util.DataQueryHelper.*;


/**
 * @author Ilya Ushakov
 */
@Repository
public class SystemRepositoryImpl extends AbstractJooqRepository implements SystemRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(SystemRepositoryImpl.class);

    @Autowired
    public SystemRepositoryImpl(DateTimeService dateTimeService, DSLContext jooq) {
        super(jooq, dateTimeService);
    }

    @Transactional
    @Override
    public SystemDocument add(SystemDocument documentEntry) {
        LOGGER.trace("entering add(documentEntry= {})", documentEntry);
        String[] readers = {documentEntry.getAuthor(), "admins"};
        LOGGER.trace("add(): readers {}", readers);
        SystemRecord persisted = jooq.insertInto(
                SYSTEM, SYSTEM.SYS_DESC, SYSTEM.SYS_TITLE, SYSTEM.SYS_TYPE, SYSTEM.SYS_AUTHOR,
                SYSTEM.SYS_READERS, SYSTEM.DATA, SYSTEM.SYS_FILE_LENGTH, SYSTEM.SYS_FILE_MIME_TYPE,
                SYSTEM.SYS_FILE_NAME, SYSTEM.SYS_FILE_PATH, SYSTEM.SYS_VERSION, SYSTEM.SYS_SYMBOLIC_NAME, SYSTEM.SYS_PARENT)
                .values(
                        documentEntry.getDescription(), documentEntry.getTitle(), documentEntry.getType(), documentEntry.getAuthor(),
                        readers, documentEntry.getData(), documentEntry.getFileLength(), documentEntry.getFileMimeType(),
                        documentEntry.getFileName(), documentEntry.getFilePath(), documentEntry.getDocVersion(), documentEntry.getSymbolicName(), documentEntry.getParent())
                .returning()
                .fetchOne();
        SystemDocument returned = SystemConverter.convertQueryResultToModelObject(persisted);

        LOGGER.trace("leaving add():  added document {}", returned);

        return returned;
    }


    @Transactional
    @Override
    public SystemDocument delete(Long id) {
        LOGGER.trace("entering delete(id={})", id);

        SystemDocument deleted = findById(id);

        LOGGER.trace("delete(): Document was found in database {}", deleted);

        if(deleted == null)
            throw new DocumentNotFoundException("The document with id was not found in database");
        int deletedRecordCount = jooq.delete(SYSTEM)
                .where(SYSTEM.ID.equal(id.intValue()))
                .execute();

        LOGGER.trace("delete(): {} document entries deleted", deletedRecordCount);

        LOGGER.trace("leaving delete(): Returning deleted Document entry: {}", deleted);

        return deleted;
    }

    @Transactional(readOnly = true)
    @Override
    public List<SystemDocument> findAll() {
        LOGGER.trace("entering findAll()");

        List<SystemRecord> queryResults = jooq.selectFrom(SYSTEM).fetchInto(SystemRecord.class);

        LOGGER.trace("findAll(): Found {} Document entries, they are going to convert to model objects", queryResults);

        List<SystemDocument> documentEntries = SystemConverter.convertQueryResultsToModelObjects(queryResults);

        LOGGER.trace("leaving findAll(): Found {} Document entries", documentEntries);

        return documentEntries;
    }

    @Transactional(readOnly = true)
    @Override
    public Page<SystemDocument> findAll(Pageable pageable, String query) {

        LOGGER.trace("entering findAll(pageSize = {}, pageNumber = {})", pageable.getPageSize(), pageable.getPageNumber());
        List<QueryParam> queryParams = getQueryParams(query);
        Condition cond = null;
        if (queryParams !=null){
	        cond = SYSTEM.SYS_TYPE.isNotNull();
	        cond = extendConditions(cond, queryParams, SYSTEM, SYSTEM.DATA);
        }
        List<SystemRecord> queryResults = jooq.selectFrom(SYSTEM)
        		.where(cond)
                .orderBy(getSortFields(pageable.getSort(), SYSTEM, SYSTEM.DATA))
                .limit(pageable.getPageSize()).offset(pageable.getOffset())
                .fetchInto(SystemRecord.class);
        
        LOGGER.trace("findAll(): Found {} Document entries, they are going to convert to model objects", queryResults);

        List<SystemDocument> documentEntries = SystemConverter.convertQueryResultsToModelObjects(queryResults);

        LOGGER.trace("findAll(): {} document entries for page: {} ",
                documentEntries.size(),
                pageable.getPageNumber()
        );
        long totalCount = 0;
        if (queryParams !=null){
        	totalCount = findTotalCountByType(cond, SYSTEM);
        }else{
        	totalCount = findTotalCount(SYSTEM);
        }

        LOGGER.trace("findAll(): {} document entries matches with the like expression: {}", totalCount);

        LOGGER.trace("leaving findAll(): Found {} Document entries", documentEntries);

        return new PageImpl<>(documentEntries, pageable, totalCount);
    }


    @Override
    public Page<SystemDocument> findAllByType(String type, String[] fields, Pageable pageable, String query) {
        LOGGER.trace("entering findAllByType(type={}, fields={}, pageable={}, query={})", type, fields, pageable, query);

        ArrayList<SelectField<?>> selectedFields = new ArrayList<SelectField<?>>();
        selectedFields.add(SYSTEM.ID);
        selectedFields.add(SYSTEM.SYS_TITLE);
        selectedFields.add(SYSTEM.SYS_AUTHOR);
        selectedFields.add(SYSTEM.SYS_DATE_CR);
        selectedFields.add(SYSTEM.SYS_DATE_MOD);
        selectedFields.add(SYSTEM.SYS_DESC);
        selectedFields.add(SYSTEM.SYS_MODIFIER);
        selectedFields.add(SYSTEM.SYS_FILE_PATH);
        selectedFields.add(SYSTEM.SYS_TYPE);
        selectedFields.add(SYSTEM.SYS_FILE_NAME);
        selectedFields.add(SYSTEM.SYS_VERSION);
        selectedFields.add(SYSTEM.SYS_UUID);
        selectedFields.add(SYSTEM.SYS_SYMBOLIC_NAME);
        selectedFields.add(SYSTEM.SYS_PARENT);
        if (fields!=null){
            for (String field : fields) {
                selectedFields.add(jsonObject(SYSTEM.DATA, field).as(field));
            }
        }
        LOGGER.trace("findAllByType(): selectedFields: {}", selectedFields);

        List<QueryParam> queryParams = getQueryParams(query);
        Condition cond = SYSTEM.SYS_TYPE.equal(type);
        cond = extendConditions(cond, queryParams, SYSTEM, SYSTEM.DATA);
        List<Record> queryResults = jooq.select(selectedFields).from(SYSTEM)
                .where(cond)
                .orderBy(getSortFields(pageable.getSort(), SYSTEM, SYSTEM.DATA))
                .limit(pageable.getPageSize()).offset(pageable.getOffset())
                .fetch();//Into(SystemRecord.class);

        LOGGER.trace("findAllByType(): Found {} Document entries, they are going to convert to model objects", queryResults);

        List<SystemDocument> documentEntries = SystemConverter.convertQueryResults(queryResults, fields);

        long totalCount = findTotalCountByType(cond, SYSTEM);

        LOGGER.trace("findAllByType(): {} document entries matches with the like expression: {}", totalCount);

        LOGGER.trace("leaving findAllByType(): Found {} SYSTEM", documentEntries);
        return new PageImpl<>(documentEntries, pageable, totalCount);

    }

	@Transactional(readOnly = true)
    @Override
    public SystemDocument findById(Long id) {
        LOGGER.trace("entering findById(id = {})", id);

        SystemRecord queryResult = jooq.selectFrom(SYSTEM)
                .where(SYSTEM.ID.equal(id.intValue()))
                .fetchOne();


        if (queryResult == null) {
            throw new DocumentNotFoundException("No Document entry found with id: " + id);
        }
        LOGGER.trace("leaving findById(): Found {}", queryResult);
        return SystemConverter.convertQueryResultToModelObject(queryResult);
    }

    @Transactional(readOnly = true)
    @Override
    public SystemDocument findByUUID(String uuid) {
        LOGGER.debug("entering findByUUID(uuid = {})", uuid);

        SystemRecord queryResult = jooq.selectFrom(SYSTEM)
                .where(SYSTEM.SYS_UUID.equal( UUID.fromString(uuid)))
                .fetchOne();

        if (queryResult == null) {
            throw new DocumentNotFoundException("No Document entry found with uuid: " + uuid);
        }
        LOGGER.trace("leaving findByUUID(): Found {}", queryResult);
        return SystemConverter.convertQueryResultToModelObject(queryResult);
    }
    
    @Transactional(readOnly = true)
    @Override
    public SystemDocument findBySymbolicName(String symbolic) {
        LOGGER.debug("entering findBySymbolicName(symbolic = {})", symbolic);

        SystemRecord queryResult = jooq.selectFrom(SYSTEM)
                .where(SYSTEM.SYS_SYMBOLIC_NAME.equal(symbolic))
                .fetchOne();

        if (queryResult == null) {
            throw new DocumentNotFoundException("No Document entry found with symbolic name: " + symbolic);
        }
        LOGGER.trace("leaving findBySymbolicName(): Found {}", queryResult);
        return SystemConverter.convertQueryResultToModelObject(queryResult);
    }


    @Transactional(readOnly = true)
    @Override
    public SystemDocument findSettings() {
        LOGGER.trace("entering findSettings(): try to find storage area settings in cache first");


            LOGGER.trace("storage area settings weren't found in cache. It will get from database");
            SystemRecord record = jooq.selectFrom(SYSTEM)
                    .where(SYSTEM.SYS_TYPE.equal("storage_area"))
                    .fetchOne();
            LOGGER.trace("findSettings(): settings record was found in db {}", record);
            if (record == null) {
                throw new DocumentNotFoundException("No Document entry found with type storageArea");
            }

            LOGGER.trace("findSettings(): storage area settings has been added to cache");


        LOGGER.trace("leaving findSettings(): Got result: {}", record);
        return SystemConverter.convertQueryResultToModelObject(record);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<SystemDocument> findBySearchTerm(String searchTerm, Pageable pageable) {
        LOGGER.trace("entering findBySearchTerm(searchTerm={}, pageable={})", searchTerm, pageable);

        String likeExpression = "%" + searchTerm + "%";

        List<SystemRecord> queryResults = jooq.selectFrom(SYSTEM)
                .where(createWhereConditions(likeExpression, SYSTEM.SYS_DESC, SYSTEM.SYS_TITLE))
                .orderBy(getSortFields(pageable.getSort(), SYSTEM, SYSTEM.DATA))
                .limit(pageable.getPageSize()).offset(pageable.getOffset())
                .fetchInto(SystemRecord.class);

        LOGGER.trace("findBySearchTerm(): Found {} Document entries, they are going to convert to model objects", queryResults);

        List<SystemDocument> documentEntries = SystemConverter.convertQueryResultsToModelObjects(queryResults);

        long totalCount = findCountByLikeExpression(likeExpression);
        LOGGER.trace("findBySearchTerm(): {} document entries matches with the like expression: {}", totalCount);
        LOGGER.trace("leaving findBySearchTerm(): Found {}", documentEntries);

        return new PageImpl<>(documentEntries, pageable, totalCount);
    }

    @Transactional
    @Override
    public SystemDocument update(SystemDocument documentEntry) {
        LOGGER.trace("entering update(documentEntry={})", documentEntry);

        Timestamp currentTime = dateTimeService.getCurrentTimestamp();
        LOGGER.trace("update(): The current time is: {}", currentTime);

        int updatedRecordCount = jooq.update(SYSTEM)
                .set(SYSTEM.SYS_DESC, documentEntry.getDescription())
                .set(SYSTEM.SYS_DATE_MOD, currentTime)
                .set(SYSTEM.SYS_TITLE, documentEntry.getTitle())
                .set(SYSTEM.SYS_MODIFIER, documentEntry.getModifier())
                .set(SYSTEM.SYS_SYMBOLIC_NAME, documentEntry.getSymbolicName())
                .set(SYSTEM.SYS_PARENT, documentEntry.getParent())
                .set(SYSTEM.DATA, documentEntry.getData())
                .set(SYSTEM.SYS_FILE_PATH, documentEntry.getFilePath())
                .set(SYSTEM.SYS_FILE_LENGTH, documentEntry.getFileLength())
                .set(SYSTEM.SYS_FILE_MIME_TYPE, documentEntry.getFileMimeType())
                .set(SYSTEM.SYS_FILE_NAME, documentEntry.getFileName())
                .set(SYSTEM.SYS_VERSION, documentEntry.getDocVersion())
                .set(SYSTEM.SYS_TYPE, documentEntry.getType())
                .where(SYSTEM.ID.equal(documentEntry.getId().intValue()))
                .execute();

        LOGGER.trace("leaving update(): Updated {}", updatedRecordCount);
        //If you are using Firebird or PostgreSQL databases, you can use the RETURNING
        //clause in the update statement (and avoid the extra select clause):
        //http://www.jooq.org/doc/3.2/manual/sql-building/sql-statements/update-statement/#N11102

        return findById(documentEntry.getId());
    }

    @Transactional
    @Override
    public SystemDocument updateFileInfo(SystemDocument documentEntry) {
        LOGGER.trace("entering updateFileInfo(documentEntry={})", documentEntry);

        Timestamp currentTime = dateTimeService.getCurrentTimestamp();

        int updatedRecordCount = jooq.update(SYSTEM)
                .set(SYSTEM.SYS_FILE_PATH, documentEntry.getFilePath())
                .set(SYSTEM.SYS_DATE_MOD, currentTime)
                .set(SYSTEM.SYS_MODIFIER, documentEntry.getModifier())
                .set(SYSTEM.SYS_FILE_LENGTH, documentEntry.getFileLength())
                .set(SYSTEM.SYS_FILE_MIME_TYPE, documentEntry.getFileMimeType())
                .set(SYSTEM.SYS_FILE_NAME, documentEntry.getFileName())
                .where(SYSTEM.ID.equal(documentEntry.getId().intValue()))
                .execute();

        LOGGER.trace("leaving updateFileInfo(): Updated {} document entry", updatedRecordCount);
        //If you are using Firebird or PostgreSQL databases, you can use the RETURNING
        //clause in the update statement (and avoid the extra select clause):
        //http://www.jooq.org/doc/3.2/manual/sql-building/sql-statements/update-statement/#N11102

        return findById(documentEntry.getId());
    }


    private long findCountByLikeExpression(String likeExpression) {
        LOGGER.trace("entering findCountByLikeExpression(likeExpression={})", likeExpression);

        long resultCount = jooq.fetchCount(
                jooq.select()
                        .from(SYSTEM)
                        .where(createWhereConditions(likeExpression, SYSTEM.SYS_DESC, SYSTEM.SYS_TITLE))
        );

        LOGGER.trace("leaving findCountByLikeExpression(): Found search result count: {}", resultCount);

        return resultCount;
    }

    private static class SystemConverter {
        private static SystemDocument convertQueryResultToModelObject(Record queryResult, String[] fields) {
            return  SystemDocument.getBuilder(queryResult.getValue(SYSTEM.SYS_TITLE))
                    .description(queryResult.getValue(SYSTEM.SYS_DESC))
                    .type(queryResult.getValue(SYSTEM.SYS_TYPE))
                    .id(queryResult.getValue(SYSTEM.ID).longValue())
                    .creationTime(queryResult.getValue(SYSTEM.SYS_DATE_CR))
                    .modificationTime(queryResult.getValue(SYSTEM.SYS_DATE_MOD))
                    .author(queryResult.getValue(SYSTEM.SYS_AUTHOR))
                    .modifier(queryResult.getValue(SYSTEM.SYS_MODIFIER))
                    .filePath(queryResult.getValue(SYSTEM.SYS_FILE_PATH))
                    .fileName(queryResult.getValue(SYSTEM.SYS_FILE_NAME))
                    .uuid(queryResult.getValue(SYSTEM.SYS_UUID))
                    .symbolicName(queryResult.getValue(SYSTEM.SYS_SYMBOLIC_NAME))
                    .parent(queryResult.getValue(SYSTEM.SYS_PARENT))
                    .data(JsonNodeParser.buildObjectNode(queryResult, fields))
                    .build();
        }


        private static SystemDocument convertQueryResultToModelObject(SystemRecord queryResult) {
            return SystemDocument.getBuilder(queryResult.getSysTitle())
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
                    .uuid(queryResult.getSysUuid())
                    .symbolicName(queryResult.getSysSymbolicName())
                    .parent(queryResult.getSysParent())
                    .build();
        }

        private static List<SystemDocument> convertQueryResultsToModelObjects(List<SystemRecord> queryResults) {
            List<SystemDocument> documentEntries = new ArrayList<>();

            for (SystemRecord queryResult : queryResults) {
                SystemDocument documentEntry = SystemConverter.convertQueryResultToModelObject(queryResult);
                documentEntries.add(documentEntry);
            }

            return documentEntries;
        }

        private static List<SystemDocument> convertQueryResults(List<Record> queryResults, String[] fields) {
            List<SystemDocument> documentEntries = new ArrayList<>();

            for (Record queryResult : queryResults) {
                documentEntries.add(SystemConverter.convertQueryResultToModelObject(queryResult, fields));
            }

            return documentEntries;
        }
    }

}