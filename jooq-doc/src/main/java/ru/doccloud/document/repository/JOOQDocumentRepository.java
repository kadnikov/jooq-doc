package ru.doccloud.document.repository;


import static net.petrikainulainen.spring.jooq.todo.db.tables.Documents.DOCUMENTS;

import java.io.IOException;
import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.SortField;
import org.jooq.TableField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import net.petrikainulainen.spring.jooq.todo.db.tables.records.DocumentsRecord;
import ru.doccloud.common.service.DateTimeService;
import ru.doccloud.document.exception.DocumentNotFoundException;
import ru.doccloud.document.model.Document;
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
        LOGGER.info("Adding new Document entry with information: {}", documentEntry);

        DocumentsRecord persisted = jooq.insertInto(DOCUMENTS,DOCUMENTS.SYS_DESC,DOCUMENTS.SYS_TITLE,DOCUMENTS.DATA)
                .values(documentEntry.getDescription(),documentEntry.getTitle(),documentEntry.getData())
                .returning()
                .fetchOne();

        Document returned = convertQueryResultToModelObject(persisted);

        LOGGER.info("Added {} todo entry", returned);

        return returned;
    }

    private DocumentsRecord createRecord(Document todoEntry) {
        Timestamp currentTime = dateTimeService.getCurrentTimestamp();
        LOGGER.debug("The current time is: {}", currentTime);

        DocumentsRecord record = new DocumentsRecord();

        record.setSysDateCr(currentTime);
        record.setSysFilePath(todoEntry.getDescription());
        record.setSysDateMod(currentTime);
        record.setSysTitle(todoEntry.getTitle());

        return record;
    }

    @Transactional
    @Override
    public Document delete(Long id) {
        LOGGER.info("Deleting Document entry by id: {}", id);

        Document deleted = findById(id);

        int deletedRecordCount = jooq.delete(DOCUMENTS)
                .where(DOCUMENTS.ID.equal(id.intValue()))
                .execute();

        LOGGER.debug("Deleted {} Document entries", deletedRecordCount);
        LOGGER.info("Returning deleted Document entry: {}", deleted);

        return deleted;
    }

    @Transactional(readOnly = true)
    @Override
    public List<Document> findAll() {
        LOGGER.info("Finding all Document entries.");

        List<DocumentsRecord> queryResults = jooq.selectFrom(DOCUMENTS).fetchInto(DocumentsRecord.class);

        List<Document> documentEntries = convertQueryResultsToModelObjects(queryResults);

        LOGGER.info("Found {} Document entries", documentEntries.size());

        return documentEntries;
    }

    @Transactional(readOnly = true)
    @Override
    public Document findById(Long id) {
        LOGGER.info("Finding Document entry by id: {}", id);

        DocumentsRecord queryResult = jooq.selectFrom(DOCUMENTS)
                .where(DOCUMENTS.ID.equal(id.intValue()))
                .fetchOne();

        LOGGER.debug("Got result: {}", queryResult);

        if (queryResult == null) {
            throw new DocumentNotFoundException("No Document entry found with id: " + id);
        }

        return convertQueryResultToModelObject(queryResult);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<Document> findBySearchTerm(String searchTerm, Pageable pageable) {
        LOGGER.info("Finding {} Document entries for page {} by using search term: {}",
                pageable.getPageSize(),
                pageable.getPageNumber(),
                searchTerm
        );

        String likeExpression = "%" + searchTerm + "%";

        List<DocumentsRecord> queryResults = jooq.selectFrom(DOCUMENTS)
                .where(createWhereConditions(likeExpression))
                .orderBy(getSortFields(pageable.getSort()))
                .limit(pageable.getPageSize()).offset(pageable.getOffset())
                .fetchInto(DocumentsRecord.class);

        List<Document> documentEntries = convertQueryResultsToModelObjects(queryResults);

        LOGGER.info("Found {} document entries for page: {}",
        		documentEntries.size(),
                pageable.getPageNumber()
        );

        long totalCount = findCountByLikeExpression(likeExpression);

        LOGGER.info("{} document entries matches with the like expression: {}",
                totalCount,
                likeExpression
        );

        return new PageImpl<>(documentEntries, pageable, totalCount);
    }

    private long findCountByLikeExpression(String likeExpression) {
        LOGGER.debug("Finding search result count by using like expression: {}", likeExpression);

        long resultCount = jooq.fetchCount(
                jooq.select()
                        .from(DOCUMENTS)
                        .where(createWhereConditions(likeExpression))
        );

        LOGGER.debug("Found search result count: {}", resultCount);

        return resultCount;
    }

    private Condition createWhereConditions(String likeExpression) {
        return DOCUMENTS.SYS_DESC.likeIgnoreCase(likeExpression)
                .or(DOCUMENTS.SYS_TITLE.likeIgnoreCase(likeExpression));
    }

    private Collection<SortField<?>> getSortFields(Sort sortSpecification) {
        LOGGER.debug("Getting sort fields from sort specification: {}", sortSpecification);
        Collection<SortField<?>> querySortFields = new ArrayList<>();

        if (sortSpecification == null) {
            LOGGER.debug("No sort specification found. Returning empty collection -> no sorting is done.");
            return querySortFields;
        }

        Iterator<Sort.Order> specifiedFields = sortSpecification.iterator();

        while (specifiedFields.hasNext()) {
            Sort.Order specifiedField = specifiedFields.next();

            String sortFieldName = specifiedField.getProperty();
            Sort.Direction sortDirection = specifiedField.getDirection();
            LOGGER.debug("Getting sort field with name: {} and direction: {}", sortFieldName, sortDirection);

            TableField tableField = getTableField(sortFieldName);
            SortField<?> querySortField = convertTableFieldToSortField(tableField, sortDirection);
            querySortFields.add(querySortField);
        }

        return querySortFields;
    }

    private TableField getTableField(String sortFieldName) {
        TableField sortField = null;
        try {
            Field tableField = DOCUMENTS.getClass().getField(sortFieldName);
            sortField = (TableField) tableField.get(DOCUMENTS);
        } catch (NoSuchFieldException | IllegalAccessException ex) {
            String errorMessage = String.format("Could not find table field: {}", sortFieldName);
            throw new InvalidDataAccessApiUsageException(errorMessage, ex);
        }

        return sortField;
    }

    private SortField<?> convertTableFieldToSortField(TableField tableField, Sort.Direction sortDirection) {
        if (sortDirection == Sort.Direction.ASC) {
            return tableField.asc();
        }
        else {
            return tableField.desc();
        }
    }

    private List<Document> convertQueryResultsToModelObjects(List<DocumentsRecord> queryResults) {
        List<Document> documentEntries = new ArrayList<>();

        for (DocumentsRecord queryResult : queryResults) {
        	Document documentEntry = Document.getBuilder(queryResult.getSysTitle())
                    .creationTime(queryResult.getSysDateCr())
                    .description(queryResult.getSysFilePath())
                    .id(queryResult.getId().longValue())
                    .modificationTime(queryResult.getSysDateMod())
                    .build();
        	documentEntries.add(documentEntry);
        }

        return documentEntries;
    }

    private Document convertQueryResultToModelObject(DocumentsRecord queryResult) {
        return Document.getBuilder(queryResult.getSysTitle())
                .creationTime(queryResult.getSysDateCr())
                .description(queryResult.getSysFilePath())
                .data(queryResult.getData())
                .id(queryResult.getId().longValue())
                .modificationTime(queryResult.getSysDateMod())
                .build();
    }

    @Transactional
    @Override
    public Document update(Document documentEntry) {
        LOGGER.info("Updating document: {}", documentEntry);

        Timestamp currentTime = dateTimeService.getCurrentTimestamp();
        LOGGER.debug("The current time is: {}", currentTime);

        int updatedRecordCount = jooq.update(DOCUMENTS)
                .set(DOCUMENTS.SYS_DESC, documentEntry.getDescription())
                .set(DOCUMENTS.SYS_DATE_MOD, currentTime)
                .set(DOCUMENTS.SYS_TITLE, documentEntry.getTitle())
                .set(DOCUMENTS.DATA, documentEntry.getData())
                .where(DOCUMENTS.ID.equal(documentEntry.getId().intValue()))
                .execute();

        LOGGER.debug("Updated {} document entry.", updatedRecordCount);

        //If you are using Firebird or PostgreSQL databases, you can use the RETURNING
        //clause in the update statement (and avoid the extra select clause):
        //http://www.jooq.org/doc/3.2/manual/sql-building/sql-statements/update-statement/#N11102

        return findById(documentEntry.getId());
    }
}
