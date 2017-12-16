package ru.doccloud.repository.impl;


import org.apache.commons.lang3.StringUtils;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.doccloud.common.exception.DocumentNotFoundException;
import ru.doccloud.common.service.DateTimeService;
import ru.doccloud.common.util.JsonNodeParser;
import ru.doccloud.document.jooq.db.tables.Documents;
import ru.doccloud.document.jooq.db.tables.Links;
import ru.doccloud.document.jooq.db.tables.records.DocumentsRecord;
import ru.doccloud.document.jooq.db.tables.records.LinksRecord;
import ru.doccloud.document.model.Document;
import ru.doccloud.document.model.Link;
import ru.doccloud.document.model.QueryParam;
import ru.doccloud.repository.DocumentRepository;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static ru.doccloud.document.jooq.db.tables.Documents.DOCUMENTS;
import static ru.doccloud.document.jooq.db.tables.Links.LINKS;
import static ru.doccloud.repository.util.DataQueryHelper.*;

/**
 * @author Andrey Kadnikov
 */
@Repository
public class JOOQDocumentRepository extends AbstractJooqRepository implements DocumentRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(JOOQDocumentRepository.class);


    @Autowired
    public JOOQDocumentRepository(DateTimeService dateTimeService, DSLContext jooq) {
        super(jooq, dateTimeService);

    }

    @Transactional
    @Override
    public Document add(Document documentEntry) {
        LOGGER.trace("entering add(documentEntry= {})", documentEntry);
        DocumentsRecord persisted = jooq.insertInto(
                DOCUMENTS, DOCUMENTS.SYS_DESC, DOCUMENTS.SYS_TITLE, DOCUMENTS.SYS_BASE_TYPE, DOCUMENTS.SYS_TYPE, DOCUMENTS.SYS_AUTHOR,
                DOCUMENTS.SYS_READERS, DOCUMENTS.DATA, DOCUMENTS.SYS_FILE_LENGTH, DOCUMENTS.SYS_FILE_MIME_TYPE,
                DOCUMENTS.SYS_FILE_NAME, DOCUMENTS.SYS_FILE_PATH, DOCUMENTS.SYS_VERSION, DOCUMENTS.SYS_FILE_STORAGE)
                .values(
                        documentEntry.getDescription(), documentEntry.getTitle(), documentEntry.getBaseType(), documentEntry.getType(), documentEntry.getAuthor(),
                        documentEntry.getReaders(), documentEntry.getData(), documentEntry.getFileLength(), documentEntry.getFileMimeType(),
                        documentEntry.getFileName(), documentEntry.getFilePath(), documentEntry.getDocVersion(), documentEntry.getFileStorage())
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


    @Transactional
    @Override
    public Document delete(Long id) {
        LOGGER.trace("entering delete(id={})", id);

        DocumentsRecord deleted = findDocById(id);

        LOGGER.trace("delete(): Document was found in database {}", deleted);

        if(deleted == null)
            throw new DocumentNotFoundException("The document with id was not found in database");

        int deletedRecordCount = jooq.delete(DOCUMENTS)
                .where(DOCUMENTS.ID.equal(id.intValue()))
                .execute();

        LOGGER.trace("delete(): {} document entries deleted", deletedRecordCount);

        LOGGER.trace("leaving delete(): Returning deleted Document entry: {}", deleted);

        return DocumentConverter.convertQueryResultToModelObject(deleted);
    }

    @Transactional(readOnly = true)
    @Override
    public List<Document> findAll() {
        LOGGER.debug("entering findAll()");

        List<DocumentsRecord> queryResults = jooq.selectFrom(DOCUMENTS).fetchInto(DocumentsRecord.class);

        LOGGER.debug("findAll(): Found {} Document entries, they are going to convert to model objects", queryResults);

        List<Document> documentEntries = DocumentConverter.convertQueryResultsToModelObjects(queryResults);

        LOGGER.debug("leaving findAll(): Found {} Document entries", documentEntries);

        return documentEntries;
    }

    @Transactional(readOnly = true)
    @Override
    public Page<Document> findAll(Pageable pageable, String query) {

        LOGGER.debug("entering findAll(pageSize = {}, pageNumber = {})", pageable.getPageSize(), pageable.getPageNumber());
        List<QueryParam> queryParams = getQueryParams(query);
        Condition cond = null;
        if (queryParams !=null){
            cond = DOCUMENTS.SYS_TYPE.isNotNull();
            cond = extendConditions(cond, queryParams, DOCUMENTS, DOCUMENTS.DATA);
        }
        List<DocumentsRecord> queryResults = jooq.selectFrom(DOCUMENTS)
                .where(cond)
                .orderBy(getSortFields(pageable.getSort(), DOCUMENTS, DOCUMENTS.DATA))
                .limit(pageable.getPageSize()).offset(pageable.getOffset())
                .fetchInto(DocumentsRecord.class);

        LOGGER.debug("findAll(): Found {} Document entries, they are going to convert to model objects", queryResults);

        List<Document> documentEntries = DocumentConverter.convertQueryResultsToModelObjects(queryResults);

        LOGGER.debug("findAll(): {} document entries for page: {} ",
                documentEntries.size(),
                pageable.getPageNumber()
        );
        long totalCount = 0;
        if (queryParams !=null){
            totalCount = findTotalCountByType(cond, DOCUMENTS);
        }else{
            totalCount = findTotalCount(DOCUMENTS);
        }

        LOGGER.trace("findAll(): {} document entries matches with the like expression: {}", totalCount);

        LOGGER.trace("leaving findAll(): Found {} Document entries", documentEntries);

        return new PageImpl<>(documentEntries, pageable, totalCount);
    }


    @Override
    @Cacheable(value = "docsByType", cacheManager = "springCM")
    public Page<Document> findAllByType(String type, String[] fields, Pageable pageable, String query, String userName) {
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
        selectedFields.add(DOCUMENTS.SYS_BASE_TYPE);
        selectedFields.add(DOCUMENTS.SYS_FILE_NAME);
        selectedFields.add(DOCUMENTS.SYS_VERSION);
        selectedFields.add(DOCUMENTS.SYS_UUID);
        selectedFields.add(DOCUMENTS.SYS_PARENT);
        selectedFields.add(DOCUMENTS.SYS_FILE_STORAGE);
        selectedFields.add(DOCUMENTS.SYS_READERS);
        if (fields!=null){
            if (fields[0].equals("all")){
                selectedFields.add(DOCUMENTS.DATA);
            }else{
                for (String field : fields) {
                    selectedFields.add(jsonObject(DOCUMENTS.DATA, field).as(field));
                }
            }
        }
        LOGGER.trace("findAllByType(): selectedFields: {}", selectedFields);

        List<QueryParam> queryParams = getQueryParams(query);
        Condition cond = DOCUMENTS.SYS_TYPE.equal(type);
        cond = extendConditions(cond, queryParams, DOCUMENTS, DOCUMENTS.DATA);
        List<Record> queryResults = jooq.select(selectedFields).from(DOCUMENTS)
                .where(cond)
                .orderBy(getSortFields(pageable.getSort(), DOCUMENTS, DOCUMENTS.DATA))
                .limit(pageable.getPageSize()).offset(pageable.getOffset())
                .fetch();//Into(DocumentsRecord.class);

        LOGGER.trace("findAllByType(): Found {} Document entries, they are going to convert to model objects", queryResults);

        List<Document> documentEntries = DocumentConverter.convertQueryResults(queryResults, fields);

        long totalCount = findTotalCountByType(cond, DOCUMENTS);

        LOGGER.trace("findAllByType(): {} document entries matches with the like expression: {}", totalCount);

        LOGGER.trace("leaving findAllByType(): Found {} Documents", documentEntries);
        return new PageImpl<>(documentEntries, pageable, totalCount);

    }

    @Transactional(readOnly = true)
    @Override
    public Document findById(Long id) {
        LOGGER.trace("entering findById(id = {})", id);

        DocumentsRecord queryResult = findDocById(id);

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
    public Page<Document> findBySearchTerm(String searchTerm, Pageable pageable) {
        LOGGER.trace("entering findBySearchTerm(searchTerm={}, pageable={})", searchTerm, pageable);

        String likeExpression = "%" + searchTerm + "%";

        List<DocumentsRecord> queryResults = jooq.selectFrom(DOCUMENTS)
                .where(createWhereConditions(likeExpression, DOCUMENTS.SYS_DESC, DOCUMENTS.SYS_TITLE))
                .orderBy(getSortFields(pageable.getSort(), DOCUMENTS, DOCUMENTS.DATA))
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

        UpdateSetMoreStep<DocumentsRecord> s = jooq.update(DOCUMENTS)
                .set(DOCUMENTS.SYS_DATE_MOD, currentTime)
                .set(DOCUMENTS.SYS_MODIFIER, documentEntry.getModifier())
                .set(DOCUMENTS.SYS_VERSION, documentEntry.getDocVersion());

        if (documentEntry.getTitle() != null) s.set(DOCUMENTS.SYS_TITLE, documentEntry.getTitle());
        if (documentEntry.getDescription() != null) s.set(DOCUMENTS.SYS_DESC, documentEntry.getDescription());
        if (documentEntry.getData() != null) s.set(DOCUMENTS.DATA, documentEntry.getData());

        int updatedRecordCount = s.where(DOCUMENTS.ID.equal(documentEntry.getId().intValue())).execute();

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
//todo check that such document exists in database
        Timestamp currentTime = dateTimeService.getCurrentTimestamp();

        int updatedRecordCount = jooq.update(DOCUMENTS)
                .set(DOCUMENTS.SYS_FILE_PATH, documentEntry.getFilePath())
                .set(DOCUMENTS.SYS_DATE_MOD, currentTime)
                .set(DOCUMENTS.SYS_MODIFIER, documentEntry.getModifier())
                .set(DOCUMENTS.SYS_FILE_LENGTH, documentEntry.getFileLength())
                .set(DOCUMENTS.SYS_FILE_MIME_TYPE, documentEntry.getFileMimeType())
                .set(DOCUMENTS.SYS_FILE_NAME, documentEntry.getFileName())
                .set(DOCUMENTS.SYS_FILE_STORAGE, documentEntry.getFileStorage())
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
        //todo check that such document exists in database
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
    public List<Document> findAllByParent(Long parent)  {
        LOGGER.trace("entering findAllByParent(parent = {})", parent);

        if(parent== null)
            throw new DocumentNotFoundException("parentIs is null");

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
        if(parent == null)
            throw new DocumentNotFoundException("parent id is null");

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
        if(docId == null)
            throw new DocumentNotFoundException("documentId is null");

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



    private long findCountByLikeExpression(String likeExpression) {
        LOGGER.trace("entering findCountByLikeExpression(likeExpression={})", likeExpression);

        long resultCount = jooq.fetchCount(
                jooq.select()
                        .from(DOCUMENTS)
                        .where(createWhereConditions(likeExpression, DOCUMENTS.SYS_DESC, DOCUMENTS.SYS_TITLE))
        );

        LOGGER.trace("leaving findCountByLikeExpression(): Found search result count: {}", resultCount);

        return resultCount;
    }


    private static class DocumentConverter{
        private static Document convertQueryResultToModelObject(Record queryResult, String[] fields) {
            return  Document.getBuilder(queryResult.getValue(DOCUMENTS.SYS_TITLE))
                    .description(queryResult.getValue(DOCUMENTS.SYS_DESC))
                    .baseType(queryResult.getValue(DOCUMENTS.SYS_BASE_TYPE))
                    .type(queryResult.getValue(DOCUMENTS.SYS_TYPE))
                    .id(queryResult.getValue(DOCUMENTS.ID).longValue())
                    .creationTime(queryResult.getValue(DOCUMENTS.SYS_DATE_CR))
                    .modificationTime(queryResult.getValue(DOCUMENTS.SYS_DATE_MOD))
                    .author(queryResult.getValue(DOCUMENTS.SYS_AUTHOR))
                    .modifier(queryResult.getValue(DOCUMENTS.SYS_MODIFIER))
                    .filePath(queryResult.getValue(DOCUMENTS.SYS_FILE_PATH))
                    .fileName(queryResult.getValue(DOCUMENTS.SYS_FILE_NAME))
                    .fileStorage(queryResult.getValue(DOCUMENTS.SYS_FILE_STORAGE))
                    .uuid(queryResult.getValue(DOCUMENTS.SYS_UUID))
                    .parent(queryResult.getValue(DOCUMENTS.SYS_PARENT))
                    .readers(queryResult.getValue(DOCUMENTS.SYS_READERS))
                    .data(JsonNodeParser.buildObjectNode(queryResult, fields))
                    .build();
        }


        private static Document convertQueryResultToModelObject(DocumentsRecord queryResult) {
            return Document.getBuilder(queryResult.getSysTitle())
                    .creationTime(queryResult.getSysDateCr())
                    .description(queryResult.getSysDesc())
                    .baseType(queryResult.getSysBaseType())
                    .type(queryResult.getSysType())
                    .data(queryResult.getData())
                    .id(queryResult.getId().longValue())
                    .modificationTime(queryResult.getSysDateMod())
                    .author(queryResult.getSysAuthor())
                    .modifier(queryResult.getSysModifier())
                    .filePath(queryResult.getSysFilePath())
                    .fileMimeType(queryResult.getSysFileMimeType())
                    .fileLength(queryResult.getSysFileLength())
                    .fileStorage(queryResult.getSysFileStorage())
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

    @Override
    public Page<Document> findAllByParentAndType(Long parentid, String type, Pageable pageable) {

        LOGGER.trace("entering findAllByParentAndType(parent = {}, type = {})", parentid , type);

        if(parentid == null)
            throw new DocumentNotFoundException("parentId is null");

        if(StringUtils.isBlank(type))
            throw new DocumentNotFoundException("document type is null");

        Condition cond = DOCUMENTS.SYS_TYPE.equal(type);
        cond = cond.and(DOCUMENTS.SYS_PARENT.equal(parentid.toString()));
        List<DocumentsRecord>  queryResults = jooq.selectFrom(DOCUMENTS)
                .where(cond)
                .orderBy(getSortFields(pageable.getSort(), DOCUMENTS, DOCUMENTS.DATA))
                .limit(pageable.getPageSize()).offset(pageable.getOffset())
                .fetchInto(DocumentsRecord.class);


        LOGGER.trace("findAllByParentAndType(): Found {} Document entries, they are going to convert to model objects", queryResults);

        List<Document> documentEntries = DocumentConverter.convertQueryResultsToModelObjects(queryResults);

        LOGGER.trace("leaving findAllByParentAndType(): Found {}", documentEntries);
        long totalCount = findTotalCountByType(cond, DOCUMENTS);

        return new PageImpl<>(documentEntries, pageable, totalCount);
    }

    private DocumentsRecord findDocById(Long id){
        return  jooq.selectFrom(DOCUMENTS)
                .where(DOCUMENTS.ID.equal(id.intValue()))
                .fetchOne();
    }

}