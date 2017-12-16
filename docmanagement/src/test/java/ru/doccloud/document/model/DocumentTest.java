package ru.doccloud.document.model;

import org.junit.Test;
import ru.doccloud.document.common.TestDateUtil;

import java.sql.Timestamp;

import static ru.doccloud.document.model.DocumentAssert.assertThatDocument;


public class DocumentTest {

    private static final Long ID = 1L;
    private static final String DESCRIPTION = "description";
    private static final String TIMESTAMP_STRING = "2014-02-18 11:13:28";
    private static final Timestamp TIMESTAMP = TestDateUtil.parseTimestamp(TIMESTAMP_STRING);
    private static final String TITLE = "title";

    @Test
    public void build_titleIsSet_shouldCreateNewObjectAndSetTitle() {
        Document created = Document.getBuilder(TITLE).build();

        assertThatDocument(created)
                .hasNoDescription()
                .hasNoId()
                .hasTitle(TITLE);
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

        assertThatDocument(created)
                .hasDescription(DESCRIPTION)
                .hasId(ID)
                .hasTitle(TITLE);
    }
}
