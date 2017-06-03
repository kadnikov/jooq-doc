package ru.doccloud.docs.service;

import org.jtransfo.JTransfo;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import ru.doccloud.common.exception.DocumentNotFoundException;
import ru.doccloud.docs.common.TestDateUtil;
import ru.doccloud.docs.config.ServiceTestContext;
import ru.doccloud.docs.dto.DocsDTOBuilder;
import ru.doccloud.document.dto.DocumentDTO;
import ru.doccloud.document.model.Document;
import ru.doccloud.repository.DocumentRepository;
import ru.doccloud.service.RepositoryDocumentCrudService;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;

import static com.googlecode.catchexception.CatchException.catchException;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;
import static ru.doccloud.docs.dto.DocsDTOAssert.assertThatTodoDTO;
import static ru.doccloud.docs.model.DocsAssert.assertThatDoc;

/**
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {ServiceTestContext.class})
public class RepositoryDocsCrudServiceTest {

    private static final String CREATION_TIME_STRING = TestDateUtil.CURRENT_TIMESTAMP;
    private static final Timestamp CREATION_TIME = TestDateUtil.parseTimestamp(CREATION_TIME_STRING);
    private static final String DESCRIPTION = "description";
    private static final Long ID = 1L;
    private static final String MODIFICATION_TIME_STRING = TestDateUtil.CURRENT_TIMESTAMP;
    private static final Timestamp MODIFICATION_TIME = TestDateUtil.parseTimestamp(MODIFICATION_TIME_STRING);
    private static final String TITLE = "title";

    @Mock
    private DocumentRepository repositoryMock;

    private RepositoryDocumentCrudService service;

    @Autowired
    private JTransfo transformer;

    @Before
    public void setUp() {
        initMocks(this);

        service = new RepositoryDocumentCrudService(repositoryMock, transformer);
    }

    @Test
    public void add_NewTodoEntry_ShouldAddTodoEntryAndReturnAddedEntry() {
    	DocumentDTO newTodoEntry = new DocsDTOBuilder()
                .description(DESCRIPTION)
                .title(TITLE)
                .build();

        Document addedTodoEntry = Document.getBuilder(TITLE)
                .creationTime(CREATION_TIME)
                .description(DESCRIPTION)
                .id(ID)
                .modificationTime(MODIFICATION_TIME)
                .build();

        when(repositoryMock.add(isA(Document.class))).thenReturn(addedTodoEntry);

        DocumentDTO returnedTodoEntry = service.add(newTodoEntry, "test");

        ArgumentCaptor<Document> repositoryMethodArgument = ArgumentCaptor.forClass(Document.class);

        verify(repositoryMock, times(1)).add(repositoryMethodArgument.capture());
        verifyNoMoreInteractions(repositoryMock);

        Document repositoryMethodArgumentValue = repositoryMethodArgument.getValue();

        assertThatDoc(repositoryMethodArgumentValue)
                .creationTimeIsNotSet()
                .hasDescription(DESCRIPTION)
                .hasNoId()
                .hasTitle(TITLE)
                .modificationTimeIsNotSet();

        assertThatTodoDTO(returnedTodoEntry)
                .hasDescription(DESCRIPTION)
                .hasId(ID)
                .hasTitle(TITLE)
                .wasCreatedAt(CREATION_TIME_STRING)
                .wasModifiedAt(MODIFICATION_TIME_STRING);
    }

    @Test
    public void delete_TodoEntryNotFound_ShouldThrowException() {
        when(repositoryMock.delete(ID)).thenThrow(new DocumentNotFoundException(""));

        catchException(service).delete(ID);
        //assertThat(caughtException()).isExactlyInstanceOf(TodoNotFoundException.class);

        verify(repositoryMock, times(1)).delete(ID);
        verifyNoMoreInteractions(repositoryMock);
    }

    @Test
    public void delete_TodoEntryFound_ShouldReturnDeletedTodoEntry() {
    	Document deletedTodoEntry = Document.getBuilder(TITLE)
                .creationTime(CREATION_TIME)
                .description(DESCRIPTION)
                .id(ID)
                .modificationTime(MODIFICATION_TIME)
                .build();

        when(repositoryMock.delete(ID)).thenReturn(deletedTodoEntry);

        DocumentDTO returnedTodoEntry = service.delete(ID);

        verify(repositoryMock, times(1)).delete(ID);
        verifyNoMoreInteractions(repositoryMock);

        assertThatTodoDTO(returnedTodoEntry)
                .hasId(ID)
                .hasDescription(DESCRIPTION)
                .hasTitle(TITLE)
                .wasCreatedAt(CREATION_TIME_STRING)
                .wasModifiedAt(MODIFICATION_TIME_STRING);
    }

    @Test
    public void findAll_OneTodoEntryFound_ShouldReturnTheFoundTodoEntry() {
    	Document foundTodoEntry = Document.getBuilder(TITLE)
                .creationTime(CREATION_TIME)
                .description(DESCRIPTION)
                .id(ID)
                .modificationTime(MODIFICATION_TIME)
                .build();

        when(repositoryMock.findAll()).thenReturn(Arrays.asList(foundTodoEntry));

        List<DocumentDTO> returnedTodoEntries = service.findAll();

        verify(repositoryMock, times(1)).findAll();
        verifyNoMoreInteractions(repositoryMock);

        assertThat(returnedTodoEntries).hasSize(1);

        DocumentDTO returnedTodoEntry = returnedTodoEntries.get(0);
        assertThatTodoDTO(returnedTodoEntry)
                .hasDescription(DESCRIPTION)
                .hasId(ID)
                .hasTitle(TITLE)
                .wasCreatedAt(CREATION_TIME_STRING)
                .wasModifiedAt(MODIFICATION_TIME_STRING);
    }

    @Test
    public void findById_TodoEntryNotFound_ShouldThrowException() {
        when(repositoryMock.findById(ID)).thenThrow(new DocumentNotFoundException(""));

        catchException(service).findById(ID);
        //assertThat(caughtException()).isExactlyInstanceOf(TodoNotFoundException.class);

        verify(repositoryMock, times(1)).findById(ID);
        verifyNoMoreInteractions(repositoryMock);
    }

    @Test
    public void findById_TodoEntryFound_ShouldReturnFoundTodoEntry() {
    	Document foundTodoEntry = Document.getBuilder(TITLE)
                .creationTime(CREATION_TIME)
                .description(DESCRIPTION)
                .id(ID)
                .modificationTime(MODIFICATION_TIME)
                .build();

        when(repositoryMock.findById(ID)).thenReturn(foundTodoEntry);

        DocumentDTO returnedTodoEntry = service.findById(ID);

        verify(repositoryMock, times(1)).findById(ID);
        verifyNoMoreInteractions(repositoryMock);

        assertThatTodoDTO(returnedTodoEntry)
                .hasId(ID)
                .hasDescription(DESCRIPTION)
                .hasTitle(TITLE)
                .wasCreatedAt(CREATION_TIME_STRING)
                .wasModifiedAt(MODIFICATION_TIME_STRING);
    }

    @Test
    public void update_TodoEntryNotFound_ShouldThrowException() {
        DocumentDTO updatedTodoEntry = new DocsDTOBuilder()
                .id(ID)
                .description(DESCRIPTION)
                .title(TITLE)
                .build();

        when(repositoryMock.update(isA(Document.class))).thenThrow(new DocumentNotFoundException(""));

        catchException(service).update(updatedTodoEntry, "test");
        //assertThat(caughtException()).isExactlyInstanceOf(DocumentNotFoundException.class);

        ArgumentCaptor<Document> repositoryMethodArgument = ArgumentCaptor.forClass(Document.class);

        verify(repositoryMock, times(1)).update(repositoryMethodArgument.capture());
        verifyNoMoreInteractions(repositoryMock);

        Document repositoryMethodArgumentValue = repositoryMethodArgument.getValue();

        assertThatDoc(repositoryMethodArgumentValue)
                .hasDescription(DESCRIPTION)
                .hasId(ID)
                .hasTitle(TITLE)
                .creationTimeIsNotSet()
                .modificationTimeIsNotSet();
    }

    @Test
    public void update_TodoEntryFound_ShouldUpdateTodoEntryAndReturnUpdatedTodoEntry() {
        DocumentDTO existingTodoEntry = new DocsDTOBuilder()
                .id(ID)
                .description(DESCRIPTION)
                .title(TITLE)
                .build();

        Document updatedTodoEntry = Document.getBuilder(TITLE)
                .creationTime(CREATION_TIME)
                .description(DESCRIPTION)
                .id(ID)
                .modificationTime(MODIFICATION_TIME)
                .build();

        when(repositoryMock.update(isA(Document.class))).thenReturn(updatedTodoEntry);

        DocumentDTO returnedTodoEntry = service.update(existingTodoEntry, "test");

        ArgumentCaptor<Document> repositoryMethodArgument = ArgumentCaptor.forClass(Document.class);

        verify(repositoryMock, times(1)).update(repositoryMethodArgument.capture());
        verifyNoMoreInteractions(repositoryMock);

        Document repositoryMethodArgumentValue = repositoryMethodArgument.getValue();

        assertThatDoc(repositoryMethodArgumentValue)
                .hasDescription(DESCRIPTION)
                .hasId(ID)
                .hasTitle(TITLE)
                .creationTimeIsNotSet()
                .modificationTimeIsNotSet();

        assertThatTodoDTO(returnedTodoEntry)
                .hasDescription(DESCRIPTION)
                .hasId(ID)
                .hasTitle(TITLE)
                .wasCreatedAt(CREATION_TIME_STRING)
                .wasModifiedAt(MODIFICATION_TIME_STRING);
    }
}
