package ru.doccloud.docs.model;

import static ru.doccloud.docs.model.DocsAssert.assertThatDoc;

import java.sql.Timestamp;

import org.junit.Test;

import ru.doccloud.docs.common.TestDateUtil;
import ru.doccloud.document.model.Document;

/**
 * @author Petri Kainulainen
 */
public class DocsTest {

    private static final Long ID = 1L;
    private static final String DESCRIPTION = "description";
    private static final String TIMESTAMP_STRING = "2014-02-18 11:13:28";
    private static final Timestamp TIMESTAMP = TestDateUtil.parseTimestamp(TIMESTAMP_STRING);
    private static final String TITLE = "title";

    @Test
    public void build_titleIsSet_shouldCreateNewObjectAndSetTitle() {
    	Document created = Document.getBuilder(TITLE).build();

        assertThatDoc(created)
                .hasNoDescription()
                .hasNoId()
                .hasTitle(TITLE)
                .creationTimeIsNotSet()
                .modificationTimeIsNotSet();
    }

    @Test(expected = IllegalStateException.class)
    public void build_titleIsNull_ShouldThrowException() {
    	Document.getBuilder(null).build();
    }

    @Test(expected = IllegalStateException.class)
    public void build_titleIsEmpty_ShouldThrowException() {
    	Document.getBuilder("").build();
    }

    @Test
    public void build_setAllValues_shouldCreateNewObjectAndSetAllFields() {
    	Document created = Document.getBuilder(TITLE)
                .creationTime(TIMESTAMP)
                .description(DESCRIPTION)
                .id(ID)
                .modificationTime(TIMESTAMP)
                .build();

        assertThatDoc(created)
                .hasDescription(DESCRIPTION)
                .hasId(ID)
                .hasTitle(TITLE)
                .wasCreatedAt(TIMESTAMP_STRING)
                .wasModifiedAt(TIMESTAMP_STRING);
    }
}
