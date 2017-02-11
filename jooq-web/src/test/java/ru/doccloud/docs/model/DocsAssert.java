package ru.doccloud.docs.model;

import org.assertj.core.api.AbstractAssert;
import org.joda.time.LocalDateTime;
import ru.doccloud.docs.common.TestDateUtil;
import ru.doccloud.document.model.Document;

import static org.assertj.core.api.Assertions.assertThat;

/**
 */
public class DocsAssert extends AbstractAssert<DocsAssert, Document> {

    private DocsAssert(Document actual) {
        super(actual, DocsAssert.class);
    }

    public static DocsAssert assertThatDoc(Document actual) {
        return new DocsAssert(actual);
    }

    public DocsAssert creationTimeIsNotSet() {
        isNotNull();

        assertThat(actual.getCreationTime())
                .overridingErrorMessage("Expected creationTime to be <null> but was <%s>", actual.getCreationTime())
                .isNull();

        return this;
    }

    public DocsAssert hasDescription(String description) {
        isNotNull();

        assertThat(actual.getDescription())
                .overridingErrorMessage("Expected description to be <%s> but was <%s>", description, actual.getDescription())
                .isEqualTo(description);

        return this;
    }

    public DocsAssert hasId() {
        isNotNull();

        assertThat(actual.getId())
                .overridingErrorMessage("Expected id to be not null but was <null>")
                .isNotNull();

        return this;
    }

    public DocsAssert hasId(Long id) {
        isNotNull();

        assertThat(actual.getId())
                .overridingErrorMessage("Expected id to be <%d> but was <%d>", id, actual.getId())
                .isEqualTo(id);

        return this;
    }

    public DocsAssert hasNoDescription() {
        isNotNull();

        assertThat(actual.getDescription())
                .overridingErrorMessage("Expected description to be <null> but was <%s>", actual.getDescription());

        return this;
    }

    public DocsAssert hasNoId() {
        isNotNull();

        assertThat(actual.getId())
                .overridingErrorMessage("Expected id to be <null> but was <%d>", actual.getId())
                .isNull();

        return this;
    }

    public DocsAssert hasTitle(String title) {
        isNotNull();

        assertThat(actual.getTitle())
                .overridingErrorMessage("Expected title to be <%s> but was <%s>", title, actual.getTitle())
                .isEqualTo(title);

        return this;
    }

    public DocsAssert modificationTimeIsNotSet() {
        isNotNull();

        assertThat(actual.getModificationTime())
                .overridingErrorMessage("Expected modificationTime to be <null> but was <%s>", actual.getModificationTime())
                .isNull();

        return this;
    }

    public DocsAssert wasCreatedAt(String creationTime) {
        isNotNull();

        LocalDateTime createdAt = TestDateUtil.parseLocalDateTime(creationTime);

        assertThat(actual.getCreationTime())
                .overridingErrorMessage("Expected creationTime to be <%s> but was <%s>", createdAt, actual.getCreationTime())
                .isEqualTo(createdAt);

        return this;
    }

    public DocsAssert wasModifiedAt(String modificationTime) {
        isNotNull();

        LocalDateTime modifiedAt = TestDateUtil.parseLocalDateTime(modificationTime);

        assertThat(actual.getModificationTime())
                .overridingErrorMessage("Expected modificationTime to be <%s> but was <%s>", modifiedAt, actual.getModificationTime())
                .isEqualTo(modifiedAt);

        return this;
    }
}
