package ru.doccloud.docs.dto;

import org.assertj.core.api.AbstractAssert;
import org.joda.time.LocalDateTime;
import ru.doccloud.docs.common.TestDateUtil;
import ru.doccloud.document.dto.DocumentDTO;

import static org.assertj.core.api.Assertions.assertThat;

/**
 */
public class DocsDTOAssert extends AbstractAssert<DocsDTOAssert, DocumentDTO> {

    private DocsDTOAssert(DocumentDTO actual) {
        super(actual, DocsDTOAssert.class);
    }

    public static DocsDTOAssert assertThatTodoDTO(DocumentDTO actual) {
        return new DocsDTOAssert(actual);
    }

    public DocsDTOAssert hasDescription(String description) {
        isNotNull();

        assertThat(actual.getDescription())
                .overridingErrorMessage("Expected description to be <%s> but was <%s>", description, actual.getDescription())
                .isEqualTo(description);

        return this;
    }

    public DocsDTOAssert hasId(Long id) {
        isNotNull();

        assertThat(actual.getId())
                .overridingErrorMessage("Expected id to be <%d> but was <%d>", id, actual.getId())
                .isEqualTo(id);

        return this;
    }

    public DocsDTOAssert hasNoCreationTime() {
        isNotNull();

        assertThat(actual.getCreationTime())
                .overridingErrorMessage("Expected creationTime to be <null> but was <%s>", actual.getCreationTime())
                .isNull();

        return this;
    }

    public DocsDTOAssert hasNoId() {
        isNotNull();

        assertThat(actual.getId())
                .overridingErrorMessage("Expected id to be <null> but was <%d>", actual.getId())
                .isNull();

        return this;
    }

    public DocsDTOAssert hasNoModificationTime() {
        isNotNull();

        assertThat(actual.getModificationTime())
                .overridingErrorMessage("Expected modificationTime to be <null> but was <%s>", actual.getModificationTime())
                .isNull();

        return this;
    }

    public DocsDTOAssert hasTitle(String title) {
        isNotNull();

        assertThat(actual.getTitle())
                .overridingErrorMessage("Expected title to be <%s> but was <%s>", title, actual.getTitle())
                .isEqualTo(title);

        return this;
    }

    public DocsDTOAssert wasCreatedAt(String creationTime) {
        isNotNull();

        LocalDateTime createdAt = TestDateUtil.parseLocalDateTime(creationTime);

        assertThat(actual.getCreationTime())
                .overridingErrorMessage("Expected creationTime to be <%s> but was <%s>.", createdAt, actual.getCreationTime())
                .isEqualTo(createdAt);

        return this;
    }

    public DocsDTOAssert wasModifiedAt(String modificationTime) {
        isNotNull();

        LocalDateTime modifiedAt = TestDateUtil.parseLocalDateTime(modificationTime);

        assertThat(actual.getModificationTime())
                .overridingErrorMessage("Expected moficationTime to be <%s> but was <%s>", modifiedAt, actual.getModificationTime())
                .isEqualTo(modifiedAt);

        return this;
    }
}
