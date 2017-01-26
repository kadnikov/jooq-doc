package ru.doccloud.document.repository;


import static net.petrikainulainen.spring.jooq.todo.db.tables.Documents.DOCUMENTS;
import static net.petrikainulainen.spring.jooq.todo.db.tables.Links.LINKS;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.SelectField;
import org.jooq.SortField;
import org.jooq.TableField;
import org.jooq.impl.DSL;
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
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import net.petrikainulainen.spring.jooq.todo.db.tables.Documents;
import net.petrikainulainen.spring.jooq.todo.db.tables.Links;
import net.petrikainulainen.spring.jooq.todo.db.tables.records.DocumentsRecord;
import net.petrikainulainen.spring.jooq.todo.db.tables.records.LinksRecord;
import ru.doccloud.common.service.DateTimeService;
import ru.doccloud.document.exception.DocumentNotFoundException;
import ru.doccloud.document.model.Document;
import ru.doccloud.document.model.Link;
import ru.doccloud.document.model.filterBean;
import ru.doccloud.document.model.queryParam;
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
        String[] readers = {documentEntry.getAuthor(), "admins"};
        DocumentsRecord persisted = jooq.insertInto(DOCUMENTS,DOCUMENTS.SYS_DESC,DOCUMENTS.SYS_TITLE,DOCUMENTS.SYS_TYPE,DOCUMENTS.SYS_AUTHOR,DOCUMENTS.SYS_READERS,DOCUMENTS.DATA)
                .values(documentEntry.getDescription(),documentEntry.getTitle(),documentEntry.getType(),documentEntry.getAuthor(),readers,documentEntry.getData())
                .returning()
                .fetchOne();

        Document returned = convertQueryResultToModelObject(persisted);

        LOGGER.info("Added {} todo entry", returned);

        return returned;
    }
    
    @Transactional
    @Override
    public Link addLink(Long headId, Long tailId) {
        LOGGER.info("Adding new Link: ");

        LinksRecord persisted = jooq.insertInto(LINKS,LINKS.HEAD_ID,LINKS.TAIL_ID)
                .values(headId.intValue(),tailId.intValue())
                .returning()
                .fetchOne();

        Link returned = new Link(persisted.getHeadId().longValue(),persisted.getTailId().longValue());

        LOGGER.info("Added {} link entry", returned);

        return returned;
    }
    
    @Transactional
    @Override
    public Link deleteLink(Long headId, Long tailId) {
        LOGGER.info("Delete Link: ");

        int deleted = jooq.delete(LINKS)
                .where(LINKS.HEAD_ID.equal(headId.intValue()).and(LINKS.TAIL_ID.equal(tailId.intValue())))  
                .execute();

        Link returned = new Link(headId,tailId);

        LOGGER.info("{} link entry deleted", deleted);

        return returned;
    }

    private DocumentsRecord createRecord(Document todoEntry) {
        Timestamp currentTime = dateTimeService.getCurrentTimestamp();
        LOGGER.debug("The current time is: {}", currentTime);

        DocumentsRecord record = new DocumentsRecord();

        record.setSysDateCr(currentTime);
        record.setSysDesc(todoEntry.getDescription());
        record.setSysDateMod(currentTime);
        record.setSysTitle(todoEntry.getTitle());
        record.setSysType(todoEntry.getType());

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
    public Page<Document> findAll(Pageable pageable) {
        LOGGER.info("Finding {} Document entries for page {} by using search term: {}",
                pageable.getPageSize(),
                pageable.getPageNumber()
        );

        List<DocumentsRecord> queryResults = jooq.selectFrom(DOCUMENTS)
                .orderBy(getSortFields(pageable.getSort()))
                .limit(pageable.getPageSize()).offset(pageable.getOffset())
                .fetchInto(DocumentsRecord.class);

        List<Document> documentEntries = convertQueryResultsToModelObjects(queryResults);

        LOGGER.info("Found {} document entries for page: {}",
        		documentEntries.size(),
                pageable.getPageNumber()
        );

        long totalCount = findTotalCount();

        LOGGER.info("{} document entries matches with the like expression: {}",
                totalCount
        );

        return new PageImpl<>(documentEntries, pageable, totalCount);
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
    
    private long findTotalCount() {
        LOGGER.debug("Finding search result count by using like expression: {}");

        long resultCount = jooq.fetchCount(
                jooq.selectFrom(DOCUMENTS)
        );

        LOGGER.debug("Found search result count: {}", resultCount);

        return resultCount;
    }
    
    private long findTotalCountByType(Condition cond) {
        LOGGER.debug("Finding search result count by using like expression: {}");

        long resultCount = jooq.fetchCount(
                jooq.selectFrom(DOCUMENTS)
                .where(cond)
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

            Field<String> tableField = getTableField(sortFieldName);
            SortField<?> querySortField = convertTableFieldToSortField(tableField, sortDirection);
            querySortFields.add(querySortField);
        }

        return querySortFields;
    }

    private Field<String> getTableField(String sortFieldName) {
        Field<String> sortField = null;
        try {
            java.lang.reflect.Field tableField = DOCUMENTS.getClass().getField(sortFieldName.toUpperCase());
            sortField = (TableField) tableField.get(DOCUMENTS);
            LOGGER.info("sortField - "+sortField);
        } catch (NoSuchFieldException | IllegalAccessException ex) {
        	LOGGER.info("Could not find table field: {}, Try to search in JSON data", sortFieldName);
        	sortField = jsonText(DOCUMENTS.DATA, sortFieldName);
            
        }

        return sortField;
    }

    private SortField<?> convertTableFieldToSortField(Field<String> tableField, Sort.Direction sortDirection) {
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
                    .description(queryResult.getSysDesc())
                    .type(queryResult.getSysType())
                    .id(queryResult.getId().longValue())
                    .creationTime(queryResult.getSysDateCr())
                    .modificationTime(queryResult.getSysDateMod())
                    .author(queryResult.getSysAuthor())
                    .modifier(queryResult.getSysModifier())
                    .filePath(queryResult.getSysFilePath())
                    .fileMimeType(queryResult.getSysFileMimeType())
                    .fileLength(queryResult.getSysFileLength())
                    .build();
        	documentEntries.add(documentEntry);
        }

        return documentEntries;
    }

    private List<Document> convertQueryResults(List<Record> queryResults, String[] fields) {
        List<Document> documentEntries = new ArrayList<>();

        for (Record queryResult : queryResults) {
        	
        	ObjectNode data = JsonNodeFactory.instance.objectNode();
        	ObjectMapper mapper = new ObjectMapper();
        	if (fields!=null){
        	for (String field : fields) {
        		if (queryResult.getValue(field)!=null){
            	try {
					data.put(field,mapper.readTree(queryResult.getValue(field).toString()));
				} catch (JsonProcessingException e) {
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
        		}
    		}
        	}
			Document documentEntry = Document.getBuilder(queryResult.getValue(DOCUMENTS.SYS_TITLE))
                    .description(queryResult.getValue(DOCUMENTS.SYS_DESC))
                    .type(queryResult.getValue(DOCUMENTS.SYS_TYPE))
                    .id(queryResult.getValue(DOCUMENTS.ID).longValue())
                    .creationTime(queryResult.getValue(DOCUMENTS.SYS_DATE_CR))
                    .modificationTime(queryResult.getValue(DOCUMENTS.SYS_DATE_MOD))
                    .author(queryResult.getValue(DOCUMENTS.SYS_AUTHOR))
                    .modifier(queryResult.getValue(DOCUMENTS.SYS_MODIFIER))
                    .data(data)
                    .build();
        	documentEntries.add(documentEntry);
        }

        return documentEntries;
    }
    private Document convertQueryResultToModelObject(DocumentsRecord queryResult) {
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
                .set(DOCUMENTS.SYS_MODIFIER, documentEntry.getModifier())
                .set(DOCUMENTS.DATA, documentEntry.getData())
                .where(DOCUMENTS.ID.equal(documentEntry.getId().intValue()))
                .execute();

        LOGGER.debug("Updated {} document entry.", updatedRecordCount);

        //If you are using Firebird or PostgreSQL databases, you can use the RETURNING
        //clause in the update statement (and avoid the extra select clause):
        //http://www.jooq.org/doc/3.2/manual/sql-building/sql-statements/update-statement/#N11102

        return findById(documentEntry.getId());
    }

    @Transactional
    @Override
    public Document updateFileInfo(Document documentEntry) { 
        LOGGER.info("Updating file info for document: {}", documentEntry);

        Timestamp currentTime = dateTimeService.getCurrentTimestamp();

        int updatedRecordCount = jooq.update(DOCUMENTS)
                .set(DOCUMENTS.SYS_FILE_PATH, documentEntry.getFilePath())
                .set(DOCUMENTS.SYS_DATE_MOD, currentTime)
                .set(DOCUMENTS.SYS_MODIFIER, documentEntry.getModifier())
                .set(DOCUMENTS.SYS_FILE_LENGTH, documentEntry.getFileLength())
                .set(DOCUMENTS.SYS_FILE_MIME_TYPE, documentEntry.getFileMimeType())
                .where(DOCUMENTS.ID.equal(documentEntry.getId().intValue()))
                .execute();

        LOGGER.debug("Updated {} document entry.", updatedRecordCount);

        //If you are using Firebird or PostgreSQL databases, you can use the RETURNING
        //clause in the update statement (and avoid the extra select clause):
        //http://www.jooq.org/doc/3.2/manual/sql-building/sql-statements/update-statement/#N11102

        return findById(documentEntry.getId());
    } 
    
    
    private static Field<Object> jsonObject(Field<?> field, String name) {
        return DSL.field("{0}->{1}", Object.class, field, DSL.inline(name));
    }

    private static Field<String> jsonText(Field<?> field, String name) {
        return DSL.field("{0}->>{1}", String.class, field, DSL.inline(name));
    }
    
	@Override
	public Page<Document> findAllByType(String type, String[] fields, Pageable pageable, String query) { 
        LOGGER.info("Finding all Documents by type.");

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
        if (fields!=null){
        for (String field : fields) {
        	selectedFields.add(jsonObject(DOCUMENTS.DATA, field).as(field));
		}
        }
        filterBean filter = null;
        List<queryParam> queryParams = null;
        LOGGER.info("Query for search - "+query);
        ObjectMapper mapper = new ObjectMapper();
        if (query!=null){
        	try {
        		filter = mapper.readValue(query, new TypeReference<filterBean>(){});
        		queryParams = filter.getMrules();
				LOGGER.info("List of params - {} {}",queryParams.toString(),queryParams.size());
			} catch (IOException e) {
				LOGGER.error("Error parsing JSON "+ e.getLocalizedMessage());
				e.printStackTrace();
			}
        }
        Condition cond = DOCUMENTS.SYS_TYPE.equal(type);
        if (queryParams!=null)
        for (queryParam param : queryParams) {
        	LOGGER.info("Param {} {} {} ",param.getField(),param.getOperand(),param.getValue());
        	if (param.getOperand()!=null){
        		if ("eq".equals(param.getOperand().toLowerCase()))
	        		cond = cond.and(getTableField(param.getField()).equal(param.getValue()));
	        	if ("cn".equals(param.getOperand().toLowerCase()))
	        		cond = cond.and(getTableField(param.getField()).like("%"+param.getValue()+"%"));
	        	if ("ge".equals(param.getOperand().toLowerCase()))
	        		cond = cond.and(getTableField(param.getField()).greaterOrEqual(param.getValue()));
	        	if ("le".equals(param.getOperand().toLowerCase()))
	        		cond = cond.and(getTableField(param.getField()).lessThan(param.getValue()));
        	}
        }
        List<Record> queryResults = jooq.select(selectedFields).from(DOCUMENTS)
        		.where(cond)
        		.orderBy(getSortFields(pageable.getSort()))
                .limit(pageable.getPageSize()).offset(pageable.getOffset())
        		.fetch();//Into(DocumentsRecord.class);

        List<Document> documentEntries = convertQueryResults(queryResults, fields);

        long totalCount = findTotalCountByType(cond);

        LOGGER.info("{} document entries matches with the like expression: {}",
                totalCount
        );

        return new PageImpl<>(documentEntries, pageable, totalCount);

	}

	@Override
	public List<Document> findAllByParent(Long parent) {
		LOGGER.info("Finding all Documents by parent.");

		Documents d = DOCUMENTS.as("d");
		Links l = LINKS.as("l");
		Documents t = DOCUMENTS.as("t");
		
        List<DocumentsRecord> queryResults = jooq.select(d.ID, d.SYS_TITLE, d.SYS_AUTHOR, d.SYS_DATE_CR, d.SYS_DATE_MOD, d.SYS_DESC, d.SYS_MODIFIER, d.SYS_FILE_PATH, d.SYS_TYPE)
        		.from(d
        		.join(l
        				.join(t)
        				.on(t.ID.equal(l.HEAD_ID)))
        		.on(d.ID.equal(l.TAIL_ID)))
        		.where(t.ID.equal(parent.intValue()))
        		.fetchInto(DocumentsRecord.class);

        List<Document> documentEntries = convertQueryResultsToModelObjects(queryResults);

        LOGGER.info("Found {} Document entries", documentEntries.size());

        return documentEntries;
	}
	
	@Override
	public List<Document> findParents(Long docId) {
		LOGGER.info("Finding parent for doc.");

		Documents d = DOCUMENTS.as("d");
		Links l = LINKS.as("l");
		Documents t = DOCUMENTS.as("t");
		
        List<DocumentsRecord> queryResults = jooq.select(d.ID, d.SYS_TITLE, d.SYS_AUTHOR, d.SYS_DATE_CR, d.SYS_DATE_MOD, d.SYS_DESC, d.SYS_MODIFIER, d.SYS_FILE_PATH, d.SYS_TYPE)
        		.from(d
        		.join(l
        				.join(t)
        				.on(t.ID.equal(l.TAIL_ID)))
        		.on(d.ID.equal(l.HEAD_ID)))
        		.where(t.ID.equal(docId.intValue()))
        		.fetchInto(DocumentsRecord.class);

        List<Document> documentEntries = convertQueryResultsToModelObjects(queryResults);

        LOGGER.info("Found {} Document entries", documentEntries.size());

        return documentEntries;
	}

	@Override
	public void setUser() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        LOGGER.info("Current Remote User - "+request.getRemoteUser());
        jooq.execute("SET my.username = '"+request.getRemoteUser()+"'");
		
	}
	
	@Override
	public void setUser(String userName) {
		LOGGER.info("Current User - "+userName);
        jooq.execute("SET my.username = '"+userName+"'");
        
        //jooq.execute("SELECT current_setting('my.username') FROM documents LIMIT 1;");
		
		
	}
}
