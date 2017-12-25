package ru.doccloud.document.repository;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DatabaseTearDown;
import com.github.springtestdbunit.annotation.ExpectedDatabase;
import com.github.springtestdbunit.assertion.DatabaseAssertionMode;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import ru.doccloud.common.exception.DocumentNotFoundException;
import ru.doccloud.document.IntegrationTestConstants;
import ru.doccloud.document.PersistenceContext;
import ru.doccloud.document.model.Document;
import ru.doccloud.document.model.Link;
import ru.doccloud.repository.DocumentRepository;

import java.util.List;

import static com.googlecode.catchexception.CatchException.catchException;
import static com.googlecode.catchexception.CatchException.caughtException;
import static org.assertj.core.api.Assertions.assertThat;
import static ru.doccloud.document.PageAssert.assertThatPage;
import static ru.doccloud.document.model.DocumentAssert.assertThatDocument;
import static ru.doccloud.document.model.LinkAssert.assertThatLink;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {PersistenceContext.class})
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
        DirtiesContextTestExecutionListener.class,
        TransactionalTestExecutionListener.class,
        DbUnitTestExecutionListener.class })

public class ITJOOQDocumentRepositoryTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ITJOOQDocumentRepositoryTest.class);

    private static final int FIRST_PAGE = 0;
    private static final int SECOND_PAGE = 1;
    private static final int THIRD_PAGE = 2;
    private static final int PAGE_SIZE_ONE = 1;
    private static final int PAGE_SIZE_TWO = 2;
    private static final int ONE_ELEMENT_ON_PAGE = 1;
    private static final long TWO_ELEMENTS = 2;
    private static final int TWO_PAGES = 2;
    private static final long ZERO_ELEMENTS = 0L;
    private static final int ZERO_ELEMENTS_ON_PAGE = 0;
    private static final int ZERO_PAGES = 0;

    @Autowired
    private DocumentRepository repository;

    @Test
    @DatabaseSetup("/ru/doccloud/document/empty-document-data.xml")
    @ExpectedDatabase(value= "/ru/doccloud/document/document-data-add.xml", assertionMode = DatabaseAssertionMode.NON_STRICT)
    @DatabaseTearDown(value={"/ru/doccloud/document/document-data-add.xml"}, type= DatabaseOperation.DELETE_ALL)
    public void add_ShouldAddNewDocumentEntry() {
        Document newDocumentEntry = Document.getBuilder(IntegrationTestConstants.NEW_TITLE)
                .description(IntegrationTestConstants.NEW_DESCRIPTION)
                .author(IntegrationTestConstants.NEW_AUTHOR)
                .baseType(IntegrationTestConstants.NEW_BASE_TYPE)
                .build();

        Document persistedDocumentEntry = repository.add(newDocumentEntry);

        assertThatDocument(persistedDocumentEntry)
                .hasId()
                .hasDescription(IntegrationTestConstants.NEW_DESCRIPTION)
                .hasTitle(IntegrationTestConstants.NEW_TITLE)
                .hasAuthor(IntegrationTestConstants.NEW_AUTHOR)
                .hasBaseType(IntegrationTestConstants.NEW_BASE_TYPE);

    }

    /**
     * see http://blog.codeleak.pl/2014/04/yet-another-way-to-handle-exceptions-in.html
     */
    @Test
    @DatabaseSetup("/ru/doccloud/document/document-data.xml")
    @ExpectedDatabase(assertionMode=DatabaseAssertionMode.NON_STRICT, value ="/ru/doccloud/document/document-data.xml")
    @DatabaseTearDown(value={"/ru/doccloud/document/document-data.xml"}, type= DatabaseOperation.DELETE_ALL)
    public void delete_DocumentEntryNotFound_ShouldDeleteDocument() {
        LOGGER.info("delete_DocumentEntryNotFound_ShouldDeleteDocument(): trying to throw exception");
        catchException(repository, DocumentNotFoundException.class).delete(IntegrationTestConstants.ID_THIRD_DOCUMENT);
        assertThat( (DocumentNotFoundException)caughtException()).isExactlyInstanceOf(DocumentNotFoundException.class);
    }
//
    @Test
    @DatabaseSetup("/ru/doccloud/document/document-data.xml")
    @ExpectedDatabase(assertionMode=DatabaseAssertionMode.NON_STRICT, value = "/ru/doccloud/document/document-data-deleted.xml")
    @DatabaseTearDown(value={"/ru/doccloud/document/document-data-deleted.xml"}, type= DatabaseOperation.DELETE_ALL)
    public void delete_DocumentEntryFound_ShouldDeleteDocument() {
        Document deletedDocumentEntry = repository.delete(IntegrationTestConstants.ID_FIRST_DOCUMENT);
        assertThatDocument(deletedDocumentEntry)
                .hasId(IntegrationTestConstants.ID_FIRST_DOCUMENT)
                .hasDescription(IntegrationTestConstants.CURRENT_DESCRIPTION)
                .hasTitle(IntegrationTestConstants.CURRENT_TITLE_FIRST_DOCUMENT)
                .hasAuthor(IntegrationTestConstants.CURRENT_AUTHOR_DOCUMENT)
                .hasBaseType(IntegrationTestConstants.CURRENT_BASE_TYPE_DOCUMENT);
    }

////
    @Test
    @DatabaseSetup("/ru/doccloud/document/empty-document-data.xml")
    @ExpectedDatabase(assertionMode=DatabaseAssertionMode.NON_STRICT, value = "/ru/doccloud/document/empty-document-data.xml")
    @DatabaseTearDown(value={"/ru/doccloud/document/empty-document-data.xml"}, type= DatabaseOperation.DELETE_ALL)
    public void findAll_NoDocumentEntriesFound_ShouldReturnEmptyList() {
        List<Document> DocumentEntries = repository.findAll();
        assertThat(DocumentEntries).isEmpty();
    }
//
    @Test
    @DatabaseSetup("/ru/doccloud/document/document-data.xml")
    @ExpectedDatabase(assertionMode=DatabaseAssertionMode.NON_STRICT, value ="/ru/doccloud/document/document-data.xml")
    @DatabaseTearDown(value={"/ru/doccloud/document/document-data.xml"}, type= DatabaseOperation.DELETE_ALL)
    public void findAll_TwoDocumentEntriesFound_ShouldReturnTwoDocumentEntries() {
        List<Document> DocumentEntries = repository.findAll();

        assertThat(DocumentEntries).hasSize(2);

        Document firstDocumentEntry = DocumentEntries.get(0);
        assertThatDocument(firstDocumentEntry)
                .hasId(IntegrationTestConstants.ID_FIRST_DOCUMENT)
                .hasDescription(IntegrationTestConstants.CURRENT_DESCRIPTION)
                .hasTitle(IntegrationTestConstants.CURRENT_TITLE_FIRST_DOCUMENT);

        Document secondDocumentEntry = DocumentEntries.get(1);
        assertThatDocument(secondDocumentEntry)
                .hasId(IntegrationTestConstants.ID_SECOND_DOCUMENT)
                .hasDescription(IntegrationTestConstants.CURRENT_DESCRIPTION)
                .hasTitle(IntegrationTestConstants.CURRENT_TITLE_SECOND_DOCUMENT);
    }

    @Test
    @DatabaseSetup("/ru/doccloud/document/document-data.xml")
    @ExpectedDatabase(assertionMode=DatabaseAssertionMode.NON_STRICT, value ="/ru/doccloud/document/document-data.xml")
    @DatabaseTearDown(value={"/ru/doccloud/document/document-data.xml"}, type= DatabaseOperation.DELETE_ALL)
    public void findById_DocumentEntryFound_ShouldReturnDocument() {
        Document foundDocumentEntry = repository.findById(IntegrationTestConstants.ID_FIRST_DOCUMENT);

        assertThatDocument(foundDocumentEntry)
                .hasId(IntegrationTestConstants.ID_FIRST_DOCUMENT)
                .hasDescription(IntegrationTestConstants.CURRENT_DESCRIPTION)
                .hasTitle(IntegrationTestConstants.CURRENT_TITLE_FIRST_DOCUMENT);
    }

    @Test
    @DatabaseSetup("/ru/doccloud/document/empty-document-data.xml")
    @ExpectedDatabase(assertionMode=DatabaseAssertionMode.NON_STRICT, value ="/ru/doccloud/document/empty-document-data.xml")
    @DatabaseTearDown(value={"/ru/doccloud/document/empty-document-data.xml"}, type= DatabaseOperation.DELETE_ALL)
    public void findById_DocumentEntryNotFound_ShouldThrowException() {
        catchException(repository, DocumentNotFoundException.class).findById(IntegrationTestConstants.ID_FIRST_DOCUMENT);
        assertThat((DocumentNotFoundException) caughtException()).isExactlyInstanceOf(DocumentNotFoundException.class);
    }

    @Test
    @DatabaseSetup("/ru/doccloud/document/document-data.xml")
    @ExpectedDatabase(assertionMode=DatabaseAssertionMode.NON_STRICT, value ="/ru/doccloud/document/document-data.xml")
    @DatabaseTearDown(value={"/ru/doccloud/document/document-data.xml"}, type= DatabaseOperation.DELETE_ALL)
    public void findByUUID_DocumentEntryFound_ShouldReturnDocument() {
        Document foundDocumentEntry = repository.findByUUID(IntegrationTestConstants.FIRST_UUID);

        assertThatDocument(foundDocumentEntry)
                .hasId(IntegrationTestConstants.ID_FIRST_DOCUMENT)
                .hasDescription(IntegrationTestConstants.CURRENT_DESCRIPTION)
                .hasTitle(IntegrationTestConstants.CURRENT_TITLE_FIRST_DOCUMENT);
    }

    @Test
    @DatabaseSetup("/ru/doccloud/document/document-data.xml")
    @ExpectedDatabase(assertionMode=DatabaseAssertionMode.NON_STRICT, value ="/ru/doccloud/document/document-data.xml")
    @DatabaseTearDown(value={"/ru/doccloud/document/document-data.xml"}, type= DatabaseOperation.DELETE_ALL)
    public void findByUUID_DocumentEntryNotFound_ShouldThrowException() {
        catchException(repository, DocumentNotFoundException.class).findByUUID(IntegrationTestConstants.SECOND_UUID);
        assertThat((DocumentNotFoundException) caughtException()).isExactlyInstanceOf(DocumentNotFoundException.class);
    }

    @Test
    @DatabaseSetup("/ru/doccloud/document/empty-document-data.xml")
    @ExpectedDatabase(assertionMode=DatabaseAssertionMode.NON_STRICT, value ="/ru/doccloud/document/empty-document-data.xml")
    @DatabaseTearDown(value={"/ru/doccloud/document/empty-document-data.xml"}, type= DatabaseOperation.DELETE_ALL)
    public void findAllByParent_DocumentEntryNotFound_ShouldThrowException() {
        catchException(repository, DocumentNotFoundException.class).findAllByParent(null,new PageRequest(FIRST_PAGE, PAGE_SIZE_ONE));
        assertThat((DocumentNotFoundException) caughtException()).isExactlyInstanceOf(DocumentNotFoundException.class);
    }

    @Test
    @DatabaseSetup("/ru/doccloud/document/document-parent-type-data.xml")
    @ExpectedDatabase(assertionMode=DatabaseAssertionMode.NON_STRICT, value = "/ru/doccloud/document/document-parent-type-data.xml")
    @DatabaseTearDown(value={"/ru/doccloud/document/document-parent-type-data.xml"}, type= DatabaseOperation.DELETE_ALL)
    public void findAllByParent_NoDocumentEntriesFound_ShouldReturnEmptyList() {
    	Page<Document> documentEntries = repository.findAllByParent(IntegrationTestConstants.ID_SECOND_DOCUMENT,new PageRequest(FIRST_PAGE, PAGE_SIZE_ONE));

    	List<Document> documentList = documentEntries.getContent();

        LOGGER.info("findAllByParent_NoDocumentEntriesFound_ShouldReturnEmptyList() documentList {}", documentList);
        assertThat(documentList).isEmpty();
    }
//
//
    @Test
    @DatabaseSetup("/ru/doccloud/document/document-parent-type-data.xml")
    @ExpectedDatabase(assertionMode=DatabaseAssertionMode.NON_STRICT, value ="/ru/doccloud/document/document-parent-type-data.xml")
    @DatabaseTearDown(value={"/ru/doccloud/document/document-parent-type-data.xml"}, type= DatabaseOperation.DELETE_ALL)
    public void findAllByParent_DocumentEntryFound_ShouldReturnDocument() {
        Page<Document> foundDocumentEntries = repository.findAllByParent(IntegrationTestConstants.PARENT_ID,new PageRequest(FIRST_PAGE, PAGE_SIZE_TWO));
        assertThat(foundDocumentEntries.getContent()).hasSize(2);

        LOGGER.info("findAllByParent_DocumentEntryFound_ShouldReturnDocument() firstDoc {}", foundDocumentEntries.getContent().get(0));
        LOGGER.info("findAllByParent_DocumentEntryFound_ShouldReturnDocument() secondDoc {}", foundDocumentEntries.getContent().get(1));

        assertThatDocument(foundDocumentEntries.getContent().get(0))
                .hasId(IntegrationTestConstants.ID_FIRST_DOCUMENT);

        assertThatDocument(foundDocumentEntries.getContent().get(1))
                .hasId(IntegrationTestConstants.ID_SECOND_DOCUMENT);
    }

    @Test
    @DatabaseSetup(value = "/ru/doccloud/document/document-links-data.xml")
    @ExpectedDatabase(assertionMode=DatabaseAssertionMode.NON_STRICT, value ="/ru/doccloud/document/document-links-data.xml")
    @DatabaseTearDown(value={"/ru/doccloud/document/document-links-data.xml"}, type= DatabaseOperation.DELETE_ALL)
    public void findParents_DocumentEntryNotFound_ShouldThrowException() {
        catchException(repository, DocumentNotFoundException.class).findParents(null);
        assertThat((DocumentNotFoundException) caughtException()).isExactlyInstanceOf(DocumentNotFoundException.class);
    }

    @Test
    @DatabaseSetup("/ru/doccloud/document/document-links-data.xml")
    @ExpectedDatabase(assertionMode=DatabaseAssertionMode.NON_STRICT, value = "/ru/doccloud/document/document-links-data.xml")
    @DatabaseTearDown(value={"/ru/doccloud/document/document-links-data.xml"}, type= DatabaseOperation.DELETE_ALL)
    public void findParents_NoDocumentEntriesFound_ShouldReturnEmptyList() {
        List<Document> documentEntries = repository.findParents(IntegrationTestConstants.ID_FIRST_DOCUMENT);

        LOGGER.info("findParents_NoDocumentEntriesFound_ShouldReturnEmptyList(): documentList {}", documentEntries);
        assertThat(documentEntries).isEmpty();
    }
////
    @Test
    @DatabaseSetup("/ru/doccloud/document/document-links-data.xml")
    @ExpectedDatabase(assertionMode=DatabaseAssertionMode.NON_STRICT, value ="/ru/doccloud/document/document-links-data.xml")
    @DatabaseTearDown(value={"/ru/doccloud/document/document-links-data.xml"}, type= DatabaseOperation.DELETE_ALL)
    public void findParents_DocumentEntryFound_ShouldReturnDocument() {
        List<Document> foundDocumentEntries = repository.findParents(IntegrationTestConstants.ID_THIRD_DOCUMENT);
        LOGGER.info("findParents_DocumentEntryFound_ShouldReturnDocument(): documentList {}", foundDocumentEntries);
        assertThat(foundDocumentEntries).hasSize(2);
        assertThatDocument(foundDocumentEntries.get(0))
                .hasId(IntegrationTestConstants.ID_FIRST_DOCUMENT);
        assertThatDocument(foundDocumentEntries.get(1))
                .hasId(IntegrationTestConstants.ID_SECOND_DOCUMENT);
    }
//
    @Test
    @DatabaseSetup("/ru/doccloud/document/document-links-data.xml")
    @ExpectedDatabase(assertionMode=DatabaseAssertionMode.NON_STRICT, value ="/ru/doccloud/document/document-links-data-deleted.xml")
    @DatabaseTearDown(value={"/ru/doccloud/document/document-links-data-deleted.xml"}, type= DatabaseOperation.DELETE_ALL)
    public void deleteLink_DocumentEntryFound_ShouldReturnDocument() {
        Link link = repository.deleteLink(IntegrationTestConstants.LINK_HEAD_ID, IntegrationTestConstants.LINK_TAIL_THIRD_ID);

        LOGGER.info("deleteLink_DocumentEntryFound_ShouldReturnDocument(): link {}", link);

        assertThatLink(link)
                .hasHeadId(IntegrationTestConstants.LINK_HEAD_ID)
                .hasTailId(IntegrationTestConstants.LINK_TAIL_THIRD_ID);
    }

    @Test
    @DatabaseSetup("/ru/doccloud/document/document-links-data-add-before.xml")
    @ExpectedDatabase(assertionMode=DatabaseAssertionMode.NON_STRICT, value ="/ru/doccloud/document/document-links-data-add.xml")
    @DatabaseTearDown(value={"/ru/doccloud/document/document-links-data-add.xml"}, type= DatabaseOperation.DELETE_ALL)
    public void addLink_DocumentEntryFound_ShouldReturnDocument() {
        Link link = repository.addLink(IntegrationTestConstants.LINK_HEAD_ID, IntegrationTestConstants.LINK_HEAD_SECOND_ID);

        LOGGER.info("addLink_DocumentEntryFound_ShouldReturnDocument(): link {}", link);
        assertThatLink(link)
                .hasHeadId(IntegrationTestConstants.LINK_HEAD_ID)
                .hasTailId(IntegrationTestConstants.LINK_HEAD_SECOND_ID);
    }
//
    @Test
    @DatabaseSetup(value = "/ru/doccloud/document/document-links-data.xml")
    @ExpectedDatabase(assertionMode=DatabaseAssertionMode.NON_STRICT, value ="/ru/doccloud/document/document-links-data.xml")
    @DatabaseTearDown(value={"/ru/doccloud/document/document-links-data.xml"}, type= DatabaseOperation.DELETE_ALL)
    public void findAllByLinkParent_DocumentEntryNotFound_ShouldThrowException() {
        catchException(repository, DocumentNotFoundException.class).findAllByLinkParent(null);
        assertThat((DocumentNotFoundException) caughtException()).isExactlyInstanceOf(DocumentNotFoundException.class);
    }

    @Test
    @DatabaseSetup("/ru/doccloud/document/document-links-data.xml")
    @ExpectedDatabase(assertionMode=DatabaseAssertionMode.NON_STRICT, value = "/ru/doccloud/document/document-links-data.xml")
    @DatabaseTearDown(value={"/ru/doccloud/document/document-links-data.xml"}, type= DatabaseOperation.DELETE_ALL)
    public void findAllByLinkParent_NoDocumentEntriesFound_ShouldReturnEmptyList() {
        List<Document> DocumentEntries = repository.findAllByLinkParent(IntegrationTestConstants.ID_THIRD_DOCUMENT);
        assertThat(DocumentEntries).isEmpty();
    }

    @Test
    @DatabaseSetup("/ru/doccloud/document/document-links-data.xml")
    @ExpectedDatabase(assertionMode=DatabaseAssertionMode.NON_STRICT, value ="/ru/doccloud/document/document-links-data.xml")
    @DatabaseTearDown(value={"/ru/doccloud/document/document-links-data.xml"}, type= DatabaseOperation.DELETE_ALL)
    public void findAllByLinkParent_DocumentEntryFound_ShouldReturnDocument() {
        List<Document> foundDocumentEntries = repository.findAllByLinkParent(IntegrationTestConstants.ID_FIRST_DOCUMENT);
        assertThat(foundDocumentEntries).hasSize(1);
        assertThatDocument(foundDocumentEntries.get(0))
                .hasId(IntegrationTestConstants.ID_THIRD_DOCUMENT);
    }

    @Test
    @DatabaseSetup("/ru/doccloud/document/document-parent-type-data.xml")
    @ExpectedDatabase(assertionMode=DatabaseAssertionMode.NON_STRICT, value ="/ru/doccloud/document/document-parent-type-data.xml")
    @DatabaseTearDown(value={"/ru/doccloud/document/document-parent-type-data.xml"}, type= DatabaseOperation.DELETE_ALL)
    public void findAllByParentAndType_EmptyParentId_ShouldThrowException() {
        catchException(repository, DocumentNotFoundException.class).findAllByParentAndType(null, IntegrationTestConstants.TYPE,
                new PageRequest(FIRST_PAGE, PAGE_SIZE_ONE));
        assertThat((DocumentNotFoundException) caughtException()).isExactlyInstanceOf(DocumentNotFoundException.class);
    }

    @Test
    @DatabaseSetup("/ru/doccloud/document/document-parent-type-data.xml")
    @ExpectedDatabase(assertionMode=DatabaseAssertionMode.NON_STRICT, value ="/ru/doccloud/document/document-parent-type-data.xml")
    @DatabaseTearDown(value={"/ru/doccloud/document/document-parent-type-data.xml"}, type= DatabaseOperation.DELETE_ALL)
    public void findAllByParentAndType_EmptyType_ShouldThrowException() {
        catchException(repository, DocumentNotFoundException.class).findAllByParentAndType(IntegrationTestConstants.PARENT_ID, null, new PageRequest(FIRST_PAGE, PAGE_SIZE_ONE));
        assertThat((DocumentNotFoundException) caughtException()).isExactlyInstanceOf(DocumentNotFoundException.class);
    }
//
    @Test
    @DatabaseSetup("/ru/doccloud/document/document-parent-type-data.xml")
    @ExpectedDatabase(assertionMode=DatabaseAssertionMode.NON_STRICT, value ="/ru/doccloud/document/document-parent-type-data.xml")
    @DatabaseTearDown(value={"/ru/doccloud/document/document-parent-type-data.xml"}, type= DatabaseOperation.DELETE_ALL)
    public void findAllByParentAndType_FirstPageWithPageSizeOne_TwoDocumentEntriesExistAndSortedByTitleAsc_ShouldReturnFirstPageWithSecondDocumentEntry() {
        Sort sortSpecification = new Sort(new Sort.Order(Sort.Direction.ASC, IntegrationTestConstants.SORT_FIELD_TITLE));
        PageRequest pageSpecification = new PageRequest(FIRST_PAGE, PAGE_SIZE_ONE, sortSpecification);

        Page<Document> firstPage = repository.findAllByParentAndType(IntegrationTestConstants.PARENT_ID, IntegrationTestConstants.TYPE, pageSpecification);

        assertThatPage(firstPage)
                .hasPageNumber(FIRST_PAGE)
                .hasPageSize(PAGE_SIZE_ONE)
                .hasNumberOfElements(ONE_ELEMENT_ON_PAGE)
                .isFirstPage()
                .isNotLastPage()
                .hasTotalNumberOfElements(TWO_ELEMENTS)
                .hasTotalNumberOfPages(TWO_PAGES);

        Document foundDocumentEntry = firstPage.getContent().get(0);

        assertThatDocument(foundDocumentEntry)
                .hasId(IntegrationTestConstants.ID_SECOND_DOCUMENT)
                .hasTitle(IntegrationTestConstants.CURRENT_TITLE_SECOND_DOCUMENT);
    }
//

    @Test
    @DatabaseSetup("/ru/doccloud/document/empty-document-data.xml")
    @ExpectedDatabase(assertionMode=DatabaseAssertionMode.NON_STRICT, value ="/ru/doccloud/document/empty-document-data.xml")
    @DatabaseTearDown(value={"/ru/doccloud/document/empty-document-data.xml"}, type= DatabaseOperation.DELETE_ALL)
    public void findBySearchTerm_DocumentEntriesNotFound_ShouldReturnPageWithoutElements() {
        Page<Document> firstPage = repository.findBySearchTerm(IntegrationTestConstants.SEARCH_TERM,
                new PageRequest(FIRST_PAGE, PAGE_SIZE_ONE)
        );

        assertThatPage(firstPage)
                .hasPageNumber(FIRST_PAGE)
                .hasPageSize(PAGE_SIZE_ONE)
                .isEmpty()
                .isFirstPage()
                .isLastPage()
                .hasTotalNumberOfElements(ZERO_ELEMENTS)
                .hasTotalNumberOfPages(ZERO_PAGES);
    }

    @Test
    @DatabaseSetup("/ru/doccloud/document/document-data.xml")
    @ExpectedDatabase(assertionMode=DatabaseAssertionMode.NON_STRICT, value ="/ru/doccloud/document/document-data.xml")
    @DatabaseTearDown(value={"/ru/doccloud/document/document-data.xml"}, type= DatabaseOperation.DELETE_ALL)
    public void findBySearchTerm_FirstPageWithPageSizeOne_TwoDocumentEntriesExist_ShouldReturnFirstPageWithFirstDocumentEntry() {
        Page<Document> firstPage = repository.findBySearchTerm(IntegrationTestConstants.SEARCH_TERM,
                new PageRequest(FIRST_PAGE, PAGE_SIZE_ONE)
        );

        assertThatPage(firstPage)
                .hasPageNumber(FIRST_PAGE)
                .hasPageSize(PAGE_SIZE_ONE)
                .hasNumberOfElements(ONE_ELEMENT_ON_PAGE)
                .isFirstPage()
                .isNotLastPage()
                .hasTotalNumberOfElements(TWO_ELEMENTS)
                .hasTotalNumberOfPages(TWO_PAGES);

        Document foundDocumentEntry = firstPage.getContent().get(0);
        assertThatDocument(foundDocumentEntry)
                .hasId(IntegrationTestConstants.ID_FIRST_DOCUMENT)
                .hasDescription(IntegrationTestConstants.CURRENT_DESCRIPTION)
                .hasTitle(IntegrationTestConstants.CURRENT_TITLE_FIRST_DOCUMENT);
    }

    @Test
    @DatabaseSetup("/ru/doccloud/document/document-data.xml")
    @ExpectedDatabase(assertionMode=DatabaseAssertionMode.NON_STRICT, value ="/ru/doccloud/document/document-data.xml")
    @DatabaseTearDown(value={"/ru/doccloud/document/document-data.xml"}, type= DatabaseOperation.DELETE_ALL)
    public void findBySearchTerm_FirstPageWithPageSizeOne_TwoDocumentEntriesExistAndSortedByTitleAsc_ShouldReturnFirstPageWithSecondDocumentEntry() {
        Sort sortSpecification = new Sort(new Sort.Order(Sort.Direction.ASC, IntegrationTestConstants.SORT_FIELD_TITLE));
        PageRequest pageSpecification = new PageRequest(FIRST_PAGE, PAGE_SIZE_ONE, sortSpecification);

        Page<Document> firstPage = repository.findBySearchTerm(IntegrationTestConstants.SEARCH_TERM, pageSpecification);

        assertThatPage(firstPage)
                .hasPageNumber(FIRST_PAGE)
                .hasPageSize(PAGE_SIZE_ONE)
                .hasNumberOfElements(ONE_ELEMENT_ON_PAGE)
                .isFirstPage()
                .isNotLastPage()
                .hasTotalNumberOfElements(TWO_ELEMENTS)
                .hasTotalNumberOfPages(TWO_PAGES);

        Document foundDocumentEntry = firstPage.getContent().get(0);

        assertThatDocument(foundDocumentEntry)
                .hasId(IntegrationTestConstants.ID_SECOND_DOCUMENT)
                .hasDescription(IntegrationTestConstants.CURRENT_DESCRIPTION)
                .hasTitle(IntegrationTestConstants.CURRENT_TITLE_SECOND_DOCUMENT);
    }

    @Test
    @DatabaseSetup("/ru/doccloud/document/document-data.xml")
    @ExpectedDatabase(assertionMode=DatabaseAssertionMode.NON_STRICT, value ="/ru/doccloud/document/document-data.xml")
    @DatabaseTearDown(value={"/ru/doccloud/document/document-data.xml"}, type= DatabaseOperation.DELETE_ALL)
    public void findBySearchTerm_FirstPageWithPageSizeOne_TwoDocumentEntriesExistAndSortedByTitleDesc_ShouldReturnFirstPageWithFirstDocumentEntry() {
        Sort sortSpecification = new Sort(new Sort.Order(Sort.Direction.DESC, IntegrationTestConstants.SORT_FIELD_TITLE));
        PageRequest pageSpecification = new PageRequest(FIRST_PAGE, PAGE_SIZE_ONE, sortSpecification);

        Page<Document> firstPage = repository.findBySearchTerm(IntegrationTestConstants.SEARCH_TERM, pageSpecification);

        assertThatPage(firstPage)
                .hasPageNumber(FIRST_PAGE)
                .hasPageSize(PAGE_SIZE_ONE)
                .hasNumberOfElements(ONE_ELEMENT_ON_PAGE)
                .isFirstPage()
                .isNotLastPage()
                .hasTotalNumberOfElements(TWO_ELEMENTS)
                .hasTotalNumberOfPages(TWO_PAGES);

        Document foundDocumentEntry = firstPage.getContent().get(0);
        assertThatDocument(foundDocumentEntry)
                .hasId(IntegrationTestConstants.ID_FIRST_DOCUMENT)
                .hasDescription(IntegrationTestConstants.CURRENT_DESCRIPTION)
                .hasTitle(IntegrationTestConstants.CURRENT_TITLE_FIRST_DOCUMENT);
    }


    @Test
    @DatabaseSetup("/ru/doccloud/document/document-data.xml")
    @ExpectedDatabase(assertionMode=DatabaseAssertionMode.NON_STRICT, value ="/ru/doccloud/document/document-data.xml")
    @DatabaseTearDown(value={"/ru/doccloud/document/document-data.xml"}, type= DatabaseOperation.DELETE_ALL)
    public void findBySearchTerm_SecondPageWithPageSizeOne_TwoDocumentEntriesExist_ShouldReturnSecondPageWithSecondDocumentEntry() {
        Page<Document> secondPage = repository.findBySearchTerm(IntegrationTestConstants.SEARCH_TERM,
                new PageRequest(SECOND_PAGE, PAGE_SIZE_ONE)
        );

        assertThatPage(secondPage)
                .hasPageNumber(SECOND_PAGE)
                .hasPageSize(PAGE_SIZE_ONE)
                .hasNumberOfElements(ONE_ELEMENT_ON_PAGE)
                .isNotFirstPage()
                .isLastPage()
                .hasTotalNumberOfElements(TWO_ELEMENTS)
                .hasTotalNumberOfPages(TWO_PAGES);

        Document foundDocumentEntry = secondPage.getContent().get(0);
        assertThatDocument(foundDocumentEntry)
                .hasId(IntegrationTestConstants.ID_SECOND_DOCUMENT)
                .hasDescription(IntegrationTestConstants.CURRENT_DESCRIPTION)
                .hasTitle(IntegrationTestConstants.CURRENT_TITLE_SECOND_DOCUMENT);
    }


    @Test
    @DatabaseSetup("/ru/doccloud/document/document-data.xml")
    @ExpectedDatabase(assertionMode=DatabaseAssertionMode.NON_STRICT, value ="/ru/doccloud/document/document-data.xml")
    @DatabaseTearDown(value={"/ru/doccloud/document/document-data.xml"}, type= DatabaseOperation.DELETE_ALL)
    public void findBySearchTerm_SecondPageWithPageSizeOne_TwoDocumentEntriesExistAndSortedByTitleAsc_ShouldReturnSecondPageWithFirstDocumentEntry() {
        Sort sortSpecification = new Sort(new Sort.Order(Sort.Direction.ASC, IntegrationTestConstants.SORT_FIELD_TITLE));
        PageRequest pageSpecification = new PageRequest(SECOND_PAGE, PAGE_SIZE_ONE, sortSpecification);

        Page<Document> secondPage = repository.findBySearchTerm(IntegrationTestConstants.SEARCH_TERM, pageSpecification);

        assertThatPage(secondPage)
                .hasPageNumber(SECOND_PAGE)
                .hasPageSize(PAGE_SIZE_ONE)
                .hasNumberOfElements(ONE_ELEMENT_ON_PAGE)
                .isNotFirstPage()
                .isLastPage()
                .hasTotalNumberOfElements(TWO_ELEMENTS)
                .hasTotalNumberOfPages(TWO_PAGES);

        Document foundDocumentEntry = secondPage.getContent().get(0);

        LOGGER.info("findBySearchTerm_SecondPageWithPageSizeOne: secondPage Document {}", foundDocumentEntry);

        assertThatDocument(foundDocumentEntry)
                .hasId(IntegrationTestConstants.ID_FIRST_DOCUMENT)
                .hasDescription(IntegrationTestConstants.CURRENT_DESCRIPTION)
                .hasTitle(IntegrationTestConstants.CURRENT_TITLE_FIRST_DOCUMENT);
    }

    @Test
    @DatabaseSetup("/ru/doccloud/document/document-data.xml")
    @ExpectedDatabase(assertionMode=DatabaseAssertionMode.NON_STRICT, value ="/ru/doccloud/document/document-data.xml")
    @DatabaseTearDown(value={"/ru/doccloud/document/document-data.xml"}, type= DatabaseOperation.DELETE_ALL)
    public void findAll_FirstPageWithPageSizeOne_TwoDocumentEntriesExistAndSortedByTitleDesc_ShouldReturnFirstPageWithFirstDocumentEntry() {
        Sort sortSpecification = new Sort(new Sort.Order(Sort.Direction.DESC, IntegrationTestConstants.SORT_FIELD_TITLE));
        PageRequest pageSpecification = new PageRequest(FIRST_PAGE, PAGE_SIZE_ONE, sortSpecification);

        Page<Document> firstPage = repository.findAll(pageSpecification, IntegrationTestConstants.JSON_QUERY);

        assertThatPage(firstPage)
                .hasPageNumber(FIRST_PAGE)
                .hasPageSize(PAGE_SIZE_ONE)
                .hasNumberOfElements(ONE_ELEMENT_ON_PAGE)
                .isFirstPage()
                .isNotLastPage()
                .hasTotalNumberOfElements(TWO_ELEMENTS)
                .hasTotalNumberOfPages(TWO_PAGES);

        Document foundDocumentEntry = firstPage.getContent().get(0);
        assertThatDocument(foundDocumentEntry)
                .hasId(IntegrationTestConstants.ID_FIRST_DOCUMENT)
                .hasAuthor(IntegrationTestConstants.CURRENT_AUTHOR_DOCUMENT);
    }

    @Test
    @DatabaseSetup("/ru/doccloud/document/document-data.xml")
    @ExpectedDatabase(assertionMode=DatabaseAssertionMode.NON_STRICT, value ="/ru/doccloud/document/document-data.xml")
    @DatabaseTearDown(value={"/ru/doccloud/document/document-data.xml"}, type= DatabaseOperation.DELETE_ALL)
    public void findAll_SecondPageWithPageSizeOne_TwoDocumentEntriesExistAndSortedByTitleAsc_ShouldReturnSecondPageWithFirstDocumentEntry() {
        Sort sortSpecification = new Sort(new Sort.Order(Sort.Direction.ASC, IntegrationTestConstants.SORT_FIELD_TITLE));
        PageRequest pageSpecification = new PageRequest(SECOND_PAGE, PAGE_SIZE_ONE, sortSpecification);

        Page<Document> secondPage = repository.findAll(pageSpecification, IntegrationTestConstants.JSON_QUERY);

        assertThatPage(secondPage)
                .hasPageNumber(SECOND_PAGE)
                .hasPageSize(PAGE_SIZE_ONE)
                .hasNumberOfElements(ONE_ELEMENT_ON_PAGE)
                .isNotFirstPage()
                .isLastPage()
                .hasTotalNumberOfElements(TWO_ELEMENTS)
                .hasTotalNumberOfPages(TWO_PAGES);

        Document foundDocumentEntry = secondPage.getContent().get(0);

        LOGGER.info("findBySearchTerm_SecondPageWithPageSizeOne: secondPage Document {}", foundDocumentEntry);

        assertThatDocument(foundDocumentEntry)
                .hasId(IntegrationTestConstants.ID_FIRST_DOCUMENT)
                .hasAuthor(IntegrationTestConstants.CURRENT_AUTHOR_DOCUMENT);
    }
//
//
    @Test
    @DatabaseSetup("/ru/doccloud/document/document-data.xml")
    @ExpectedDatabase(assertionMode=DatabaseAssertionMode.NON_STRICT, value ="/ru/doccloud/document/document-data.xml")
    @DatabaseTearDown(value={"/ru/doccloud/document/document-data.xml"}, type= DatabaseOperation.DELETE_ALL)
    public void findAllByType_FirstPageWithPageSizeOne_EmptyFields_TwoDocumentEntriesExistAndSortedByTitleDesc_ShouldReturnFirstPageWithFirstDocumentEntry() {
        Sort sortSpecification = new Sort(new Sort.Order(Sort.Direction.DESC, IntegrationTestConstants.SORT_FIELD_TYPE));
        PageRequest pageSpecification = new PageRequest(FIRST_PAGE, PAGE_SIZE_ONE, sortSpecification);

        Page<Document> firstPage = repository.findAllByType(
                IntegrationTestConstants.TYPE, null, pageSpecification,
                IntegrationTestConstants.JSON_QUERY, IntegrationTestConstants.USER);

        assertThatPage(firstPage)
                .hasPageNumber(FIRST_PAGE)
                .hasPageSize(PAGE_SIZE_ONE)
                .hasNumberOfElements(ONE_ELEMENT_ON_PAGE)
                .isFirstPage()
                .isNotLastPage()
                .hasTotalNumberOfElements(TWO_ELEMENTS)
                .hasTotalNumberOfPages(TWO_PAGES);

        Document foundDocumentEntry = firstPage.getContent().get(0);
        assertThatDocument(foundDocumentEntry)
                .hasId(IntegrationTestConstants.ID_FIRST_DOCUMENT)
                .hasAuthor(IntegrationTestConstants.CURRENT_AUTHOR_DOCUMENT)
                .hasType(IntegrationTestConstants.TYPE);
    }

    @Test
    @DatabaseSetup("/ru/doccloud/document/document-data.xml")
    @ExpectedDatabase(assertionMode=DatabaseAssertionMode.NON_STRICT, value ="/ru/doccloud/document/document-data.xml")
    @DatabaseTearDown(value={"/ru/doccloud/document/document-data.xml"}, type= DatabaseOperation.DELETE_ALL)
    public void findAllByType_FirstPageWithPageSizeOne_AllFields_TwoDocumentEntriesExistAndSortedByTitleDesc_ShouldReturnFirstPageWithFirstDocumentEntry() {
        Sort sortSpecification = new Sort(new Sort.Order(Sort.Direction.DESC, IntegrationTestConstants.SORT_FIELD_TYPE));
        PageRequest pageSpecification = new PageRequest(FIRST_PAGE, PAGE_SIZE_ONE, sortSpecification);

        Page<Document> firstPage = repository.findAllByType(
                IntegrationTestConstants.TYPE, IntegrationTestConstants.FIELDS_ARR_ALL, pageSpecification,
                IntegrationTestConstants.JSON_QUERY, IntegrationTestConstants.USER);

        assertThatPage(firstPage)
                .hasPageNumber(FIRST_PAGE)
                .hasPageSize(PAGE_SIZE_ONE)
                .hasNumberOfElements(ONE_ELEMENT_ON_PAGE)
                .isFirstPage()
                .isNotLastPage()
                .hasTotalNumberOfElements(TWO_ELEMENTS)
                .hasTotalNumberOfPages(TWO_PAGES);

        Document foundDocumentEntry = firstPage.getContent().get(0);
        assertThatDocument(foundDocumentEntry)
                .hasId(IntegrationTestConstants.ID_FIRST_DOCUMENT)
                .hasAuthor(IntegrationTestConstants.CURRENT_AUTHOR_DOCUMENT)
                .hasType(IntegrationTestConstants.TYPE);
    }

    @Test
    @DatabaseSetup("/ru/doccloud/document/document-data-json.xml")
    @ExpectedDatabase(assertionMode=DatabaseAssertionMode.NON_STRICT, value ="/ru/doccloud/document/document-data-json.xml")
    @DatabaseTearDown(value={"/ru/doccloud/document/document-data-json.xml"}, type= DatabaseOperation.DELETE_ALL)
    public void findAllByType_FirstPageWithPageSizeOne_ArrFields_TwoDocumentEntriesExistAndSortedByTitleDesc_ShouldReturnFirstPageWithFirstDocumentEntry() {
        Sort sortSpecification = new Sort(new Sort.Order(Sort.Direction.DESC, IntegrationTestConstants.SORT_FIELD_TYPE));
        PageRequest pageSpecification = new PageRequest(FIRST_PAGE, PAGE_SIZE_ONE, sortSpecification);

        Page<Document> firstPage = repository.findAllByType(
                IntegrationTestConstants.TYPE, IntegrationTestConstants.FIELDS_ARR_TEST, pageSpecification,
                IntegrationTestConstants.JSON_QUERY_TEST, IntegrationTestConstants.USER);

        assertThatPage(firstPage)
                .hasPageNumber(FIRST_PAGE)
                .hasPageSize(PAGE_SIZE_ONE)
                .hasNumberOfElements(ONE_ELEMENT_ON_PAGE)
                .isFirstPage()
                .isNotLastPage()
                .hasTotalNumberOfElements(TWO_ELEMENTS)
                .hasTotalNumberOfPages(TWO_PAGES);

        Document foundDocumentEntry = firstPage.getContent().get(0);
        assertThatDocument(foundDocumentEntry)
                .hasId(IntegrationTestConstants.ID_FIRST_DOCUMENT)
                .hasAuthor(IntegrationTestConstants.CURRENT_AUTHOR_DOCUMENT)
                .hasType(IntegrationTestConstants.TYPE);
    }

    @Test
    @DatabaseSetup("/ru/doccloud/document/document-data.xml")
    @ExpectedDatabase(assertionMode=DatabaseAssertionMode.NON_STRICT, value ="/ru/doccloud/document/document-data.xml")
    @DatabaseTearDown(value={"/ru/doccloud/document/document-data.xml"}, type= DatabaseOperation.DELETE_ALL)
    public void findAllByType_SecondPageWithPageSizeOne_EmptyFields_TwoDocumentEntriesExistAndSortedByTitleAsc_ShouldReturnSecondPageWithFirstDocumentEntry() {
        Sort sortSpecification = new Sort(new Sort.Order(Sort.Direction.ASC, IntegrationTestConstants.SORT_FIELD_TITLE));
        PageRequest pageSpecification = new PageRequest(SECOND_PAGE, PAGE_SIZE_ONE, sortSpecification);

        Page<Document> secondPage = repository.findAllByType(
                IntegrationTestConstants.TYPE, null, pageSpecification,
                IntegrationTestConstants.JSON_QUERY, IntegrationTestConstants.USER);

        assertThatPage(secondPage)
                .hasPageNumber(SECOND_PAGE)
                .hasPageSize(PAGE_SIZE_ONE)
                .hasNumberOfElements(ONE_ELEMENT_ON_PAGE)
                .isNotFirstPage()
                .isLastPage()
                .hasTotalNumberOfElements(TWO_ELEMENTS)
                .hasTotalNumberOfPages(TWO_PAGES);

        Document foundDocumentEntry = secondPage.getContent().get(0);

        LOGGER.info("findBySearchTerm_SecondPageWithPageSizeOne: secondPage Document {}", foundDocumentEntry);

        assertThatDocument(foundDocumentEntry)
                .hasId(IntegrationTestConstants.ID_FIRST_DOCUMENT)
                .hasTitle(IntegrationTestConstants.CURRENT_TITLE_FIRST_DOCUMENT);
    }

    @Test
    @DatabaseSetup("/ru/doccloud/document/document-data.xml")
    @ExpectedDatabase(assertionMode=DatabaseAssertionMode.NON_STRICT, value ="/ru/doccloud/document/document-data.xml")
    @DatabaseTearDown(value={"/ru/doccloud/document/document-data.xml"}, type= DatabaseOperation.DELETE_ALL)
    public void findAllByType_SecondPageWithPageSizeOne_AllFields_TwoDocumentEntriesExistAndSortedByTitleAsc_ShouldReturnSecondPageWithFirstDocumentEntry() {
        Sort sortSpecification = new Sort(new Sort.Order(Sort.Direction.ASC, IntegrationTestConstants.SORT_FIELD_TITLE));
        PageRequest pageSpecification = new PageRequest(SECOND_PAGE, PAGE_SIZE_ONE, sortSpecification);

        Page<Document> secondPage = repository.findAllByType(
                IntegrationTestConstants.TYPE, IntegrationTestConstants.FIELDS_ARR_ALL, pageSpecification,
                IntegrationTestConstants.JSON_QUERY, IntegrationTestConstants.USER);

        assertThatPage(secondPage)
                .hasPageNumber(SECOND_PAGE)
                .hasPageSize(PAGE_SIZE_ONE)
                .hasNumberOfElements(ONE_ELEMENT_ON_PAGE)
                .isNotFirstPage()
                .isLastPage()
                .hasTotalNumberOfElements(TWO_ELEMENTS)
                .hasTotalNumberOfPages(TWO_PAGES);

        Document foundDocumentEntry = secondPage.getContent().get(0);

        LOGGER.info("findBySearchTerm_SecondPageWithPageSizeOne: secondPage Document {}", foundDocumentEntry);

        assertThatDocument(foundDocumentEntry)
                .hasId(IntegrationTestConstants.ID_FIRST_DOCUMENT)
                .hasTitle(IntegrationTestConstants.CURRENT_TITLE_FIRST_DOCUMENT);
    }
//
    @Test
    @DatabaseSetup("/ru/doccloud/document/document-data-json.xml")
    @ExpectedDatabase(assertionMode=DatabaseAssertionMode.NON_STRICT, value ="/ru/doccloud/document/document-data-json.xml")
    @DatabaseTearDown(value={"/ru/doccloud/document/document-data-json.xml"}, type= DatabaseOperation.DELETE_ALL)
    public void findAllByType_SecondPageWithPageSizeOne_ArrFields_TwoDocumentEntriesExistAndSortedByTitleAsc_ShouldReturnSecondPageWithFirstDocumentEntry() {
        Sort sortSpecification = new Sort(new Sort.Order(Sort.Direction.ASC, IntegrationTestConstants.SORT_FIELD_TITLE));
        PageRequest pageSpecification = new PageRequest(SECOND_PAGE, PAGE_SIZE_ONE, sortSpecification);

        Page<Document> secondPage = repository.findAllByType(
                IntegrationTestConstants.TYPE, IntegrationTestConstants.FIELDS_ARR_TEST, pageSpecification,
                IntegrationTestConstants.JSON_QUERY_TEST, IntegrationTestConstants.USER);

        assertThatPage(secondPage)
                .hasPageNumber(SECOND_PAGE)
                .hasPageSize(PAGE_SIZE_ONE)
                .hasNumberOfElements(ONE_ELEMENT_ON_PAGE)
                .isNotFirstPage()
                .isLastPage()
                .hasTotalNumberOfElements(TWO_ELEMENTS)
                .hasTotalNumberOfPages(TWO_PAGES);

        Document foundDocumentEntry = secondPage.getContent().get(0);

        LOGGER.info("findBySearchTerm_SecondPageWithPageSizeOne: secondPage Document {}", foundDocumentEntry);

        assertThatDocument(foundDocumentEntry)
                .hasId(IntegrationTestConstants.ID_FIRST_DOCUMENT)
                .hasTitle(IntegrationTestConstants.CURRENT_TITLE_FIRST_DOCUMENT);
    }
//
//
    @Test
    @DatabaseSetup("/ru/doccloud/document/document-data.xml")
    @ExpectedDatabase(assertionMode=DatabaseAssertionMode.NON_STRICT, value ="/ru/doccloud/document/document-data.xml")
    @DatabaseTearDown(value={"/ru/doccloud/document/document-data.xml"}, type= DatabaseOperation.DELETE_ALL)
    public void findBySearchTerm_SecondPageWithPageSizeOne_TwoDocumentEntriesExistAndSortedByTitleDesc_ShouldReturnSecondPageWithSecondDocumentEntry() {
        Sort sortSpecification = new Sort(new Sort.Order(Sort.Direction.DESC, IntegrationTestConstants.SORT_FIELD_TITLE));
        PageRequest pageSpecification = new PageRequest(1, 1, sortSpecification);

        Page<Document> secondPage = repository.findBySearchTerm(IntegrationTestConstants.SEARCH_TERM, pageSpecification);

        assertThatPage(secondPage)
                .hasPageNumber(SECOND_PAGE)
                .hasPageSize(PAGE_SIZE_ONE)
                .hasNumberOfElements(ONE_ELEMENT_ON_PAGE)
                .isNotFirstPage()
                .isLastPage()
                .hasTotalNumberOfElements(TWO_ELEMENTS)
                .hasTotalNumberOfPages(TWO_PAGES);

        Document foundDocumentEntry = secondPage.getContent().get(0);
        assertThatDocument(foundDocumentEntry)
                .hasId(IntegrationTestConstants.ID_SECOND_DOCUMENT)
                .hasTitle(IntegrationTestConstants.CURRENT_TITLE_SECOND_DOCUMENT);
    }

//
    @Test
    @DatabaseSetup("/ru/doccloud/document/document-data.xml")
    @ExpectedDatabase(assertionMode=DatabaseAssertionMode.NON_STRICT, value ="/ru/doccloud/document/document-data.xml")
    @DatabaseTearDown(value={"/ru/doccloud/document/document-data.xml"}, type= DatabaseOperation.DELETE_ALL)
    public void findBySearchTerm_ThirdPageWithPageSizeOne_TwoDocumentEntriesExist_ShouldReturnPageWithEmptyList() {
        Page<Document> thirdPage = repository.findBySearchTerm(IntegrationTestConstants.SEARCH_TERM, new PageRequest(2, 1));

        assertThatPage(thirdPage)
                .hasPageNumber(THIRD_PAGE)
                .hasPageSize(PAGE_SIZE_ONE)
                .hasNumberOfElements(ZERO_ELEMENTS_ON_PAGE)
                .isNotFirstPage()
                .isLastPage()
                .hasTotalNumberOfElements(TWO_ELEMENTS)
                .hasTotalNumberOfPages(TWO_PAGES);
    }

    @Test
    @DatabaseSetup("/ru/doccloud/document/document-data.xml")
    @ExpectedDatabase(assertionMode=DatabaseAssertionMode.NON_STRICT, value = "/ru/doccloud/document/document-data.xml")
    @DatabaseTearDown(value={"/ru/doccloud/document/document-data.xml"}, type= DatabaseOperation.DELETE_ALL)
    public void findBySearchTerm_ThirdPageWithPageSizeOne_TwoDocumentEntriesExistAndSortedByTitleAsc_ShouldReturnPageWithEmptyList() {
        Sort sortSpecification = new Sort(new Sort.Order(Sort.Direction.ASC, IntegrationTestConstants.SORT_FIELD_TITLE));
        PageRequest pageSpecification = new PageRequest(2, 1, sortSpecification);

        Page<Document> thirdPage = repository.findBySearchTerm(IntegrationTestConstants.SEARCH_TERM, pageSpecification);

        assertThatPage(thirdPage)
                .hasPageNumber(THIRD_PAGE)
                .hasPageSize(PAGE_SIZE_ONE)
                .hasNumberOfElements(ZERO_ELEMENTS_ON_PAGE)
                .isNotFirstPage()
                .isLastPage()
                .hasTotalNumberOfElements(TWO_ELEMENTS)
                .hasTotalNumberOfPages(TWO_PAGES);
    }

    @Test
    @DatabaseSetup("/ru/doccloud/document/document-data.xml")
    @ExpectedDatabase(assertionMode=DatabaseAssertionMode.NON_STRICT, value = "/ru/doccloud/document/document-data.xml")
    @DatabaseTearDown(value={"/ru/doccloud/document/document-data.xml"}, type= DatabaseOperation.DELETE_ALL)
    public void findBySearchTerm_ThirdPageWithPageSizeOne_TwoDocumentEntriesExistAndSortedByTitleDesc_ShouldReturnPageWithEmptyList() {
        Sort sortSpecification = new Sort(new Sort.Order(Sort.Direction.DESC, IntegrationTestConstants.SORT_FIELD_TITLE));
        PageRequest pageSpecification = new PageRequest(2, 1, sortSpecification);

        Page<Document> thirdPage = repository.findBySearchTerm(IntegrationTestConstants.SEARCH_TERM, pageSpecification);

        assertThatPage(thirdPage)
                .hasPageNumber(THIRD_PAGE)
                .hasPageSize(PAGE_SIZE_ONE)
                .hasNumberOfElements(ZERO_ELEMENTS_ON_PAGE)
                .isNotFirstPage()
                .isLastPage()
                .hasTotalNumberOfElements(TWO_ELEMENTS)
                .hasTotalNumberOfPages(TWO_PAGES);
    }

    @Test
    @DatabaseSetup("/ru/doccloud/document/empty-document-data.xml")
    @ExpectedDatabase(assertionMode=DatabaseAssertionMode.NON_STRICT, value = "/ru/doccloud/document/empty-document-data.xml")
    @DatabaseTearDown(value={"/ru/doccloud/document/empty-document-data.xml"}, type= DatabaseOperation.DELETE_ALL)
    public void update_DocumentEntryNotFound_ShouldThrowException() {
        Document updatedDocumentEntry = Document.getBuilder("title")
                .description("description")
                .id(IntegrationTestConstants.ID_SECOND_DOCUMENT)
                .build();

        catchException(repository, DocumentNotFoundException.class).update(updatedDocumentEntry);
        assertThat((DocumentNotFoundException) caughtException()).isExactlyInstanceOf(DocumentNotFoundException.class);
    }

    @Test
    @DatabaseSetup("/ru/doccloud/document/document-data.xml")
    @ExpectedDatabase(value= "/ru/doccloud/document/document-data-updated.xml", assertionMode = DatabaseAssertionMode.NON_STRICT)
    @DatabaseTearDown(value={"/ru/doccloud/document/document-data-updated.xml"}, type= DatabaseOperation.DELETE_ALL)
    public void update_DocumentEntryFound_ShouldUpdateDocument() {
        Document updatedDocumentEntry = Document.getBuilder(IntegrationTestConstants.NEW_TITLE)
                .description(IntegrationTestConstants.NEW_DESCRIPTION)
                .id(IntegrationTestConstants.ID_SECOND_DOCUMENT)
                .build();

        Document returnedDocumentEntry = repository.update(updatedDocumentEntry);

        assertThatDocument(returnedDocumentEntry)
                .hasId(IntegrationTestConstants.ID_SECOND_DOCUMENT)
                .hasDescription(IntegrationTestConstants.NEW_DESCRIPTION)
                .hasTitle(IntegrationTestConstants.NEW_TITLE);
    }
//
    @Test
    @DatabaseSetup("/ru/doccloud/document/document-parent-type-data.xml")
    @ExpectedDatabase(value= "/ru/doccloud/document/document-parent-data-updated.xml", assertionMode = DatabaseAssertionMode.NON_STRICT)
    @DatabaseTearDown(value={"/ru/doccloud/document/document-data-updated.xml"}, type= DatabaseOperation.DELETE_ALL)
    public void setParent_DocumentEntryFound_ShouldSetParentDocument() {
        Document parentdDocumentEntry = Document.getBuilder(IntegrationTestConstants.NEW_TITLE)
                .description(IntegrationTestConstants.NEW_DESCRIPTION)
                .id(IntegrationTestConstants.ID_FIRST_DOCUMENT)
                .parent(String.valueOf(IntegrationTestConstants.ID_SECOND_DOCUMENT))
                .build();

        Document returnedDocumentEntry = repository.setParent(parentdDocumentEntry);

        assertThatDocument(returnedDocumentEntry)
                .hasId(IntegrationTestConstants.ID_FIRST_DOCUMENT)
                .hasParent(String.valueOf(IntegrationTestConstants.ID_SECOND_DOCUMENT));
    }
//
    @Test
    @DatabaseSetup("/ru/doccloud/document/document-fileinfo-data.xml")
    @ExpectedDatabase(value= "/ru/doccloud/document/document-fileinfo-data-updated.xml", assertionMode = DatabaseAssertionMode.NON_STRICT)
    @DatabaseTearDown(value={"/ru/doccloud/document/document-fileinfo-data-updated.xml"}, type= DatabaseOperation.DELETE_ALL)
    public void updateFileInfo_DocumentEntryFound_ShouldUpdateDocument() {
        Document updatedDocumentEntry = Document.getBuilder(IntegrationTestConstants.CURRENT_TITLE_FIRST_DOCUMENT)
                .id(IntegrationTestConstants.ID_FIRST_DOCUMENT)
                .modifier(IntegrationTestConstants.MODIFIER_NEW)
                .fileLength(IntegrationTestConstants.FILE_LENGHT_NEW)
                .fileMimeType(IntegrationTestConstants.MIME_TYPE_NEW)
                .fileName(IntegrationTestConstants.FILE_NAME_NEW)
                .fileStorage(IntegrationTestConstants.FILE_STORAGE_NEW)
                .filePath(IntegrationTestConstants.FILEPATH_NEW)
                .build();

        Document returnedDocumentEntry = repository.updateFileInfo(updatedDocumentEntry);

        assertThatDocument(returnedDocumentEntry)
                .hasId(IntegrationTestConstants.ID_FIRST_DOCUMENT)
                .hasFileLength(IntegrationTestConstants.FILE_LENGHT_NEW)
                .hasFileName(IntegrationTestConstants.FILE_NAME_NEW)
                .hasFilePath(IntegrationTestConstants.FILEPATH_NEW)
                .hasFileStorage(IntegrationTestConstants.FILE_STORAGE_NEW)
                .hasMimeType(IntegrationTestConstants.MIME_TYPE_NEW)
                .hasModifier(IntegrationTestConstants.MODIFIER_NEW);
    }
}
