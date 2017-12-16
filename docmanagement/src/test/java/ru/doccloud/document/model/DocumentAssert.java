package ru.doccloud.document.model;

import org.assertj.core.api.AbstractAssert;

import static org.assertj.core.api.Assertions.assertThat;

//todo add own test criteria
public class DocumentAssert extends AbstractAssert<DocumentAssert, Document> {

    private DocumentAssert(Document actual) {
        super(actual, DocumentAssert.class);
    }

    public static DocumentAssert assertThatDocument(Document actual) {
        return new DocumentAssert(actual);
    }

    public DocumentAssert hasDescription(String description) {
        isNotNull();

        assertThat(actual.getDescription())
                .overridingErrorMessage("Expected description to be <%s> but was <%s>", description, actual.getDescription())
                .isEqualTo(description);

        return this;
    }

    public DocumentAssert hasId() {
        isNotNull();

        assertThat(actual.getId())
                .overridingErrorMessage("Expected id to be not null but was <null>")
                .isNotNull();

        return this;
    }

    public DocumentAssert hasId(Long id) {
        isNotNull();

        assertThat(actual.getId())
                .overridingErrorMessage("Expected id to be <%d> but was <%d>", id, actual.getId())
                .isEqualTo(id);

        return this;
    }

    public DocumentAssert hasNoDescription() {
        isNotNull();

        assertThat(actual.getDescription())
                .overridingErrorMessage("Expected description to be <null> but was <%s>", actual.getDescription());

        return this;
    }

    public DocumentAssert hasNoId() {
        isNotNull();

        assertThat(actual.getId())
                .overridingErrorMessage("Expected id to be <null> but was <%d>", actual.getId())
                .isNull();

        return this;
    }

    public DocumentAssert hasTitle(String title) {
        isNotNull();

        assertThat(actual.getTitle())
                .overridingErrorMessage("Expected title to be <%s> but was <%s>", title, actual.getTitle())
                .isEqualTo(title);

        return this;
    }

    public DocumentAssert hasAuthor(String author) {
        isNotNull();

        assertThat(actual.getAuthor())
                .overridingErrorMessage("Expected author to be <%s> but was <%s>", author, actual.getAuthor())
                .isEqualTo(author);

        return this;
    }

    public DocumentAssert hasBaseType(String baseType) {
        isNotNull();

        assertThat(actual.getBaseType())
                .overridingErrorMessage("Expected baseType to be <%s> but was <%s>", baseType, actual.getBaseType())
                .isEqualTo(baseType);

        return this;
    }

    public DocumentAssert hasType(String type) {
        isNotNull();

        assertThat(actual.getType())
                .overridingErrorMessage("Expected tepe to be <%s> but was <%s>", type, actual.getType())
                .isEqualTo(type);

        return this;
    }

    public DocumentAssert hasFilePath(String filePath) {
        isNotNull();

        assertThat(actual.getFilePath())
                .overridingErrorMessage("Expected filePath to be <%s> but was <%s>", filePath, actual.getFilePath())
                .isEqualTo(filePath);

        return this;
    }

    public DocumentAssert hasModifier(String modifier) {
        isNotNull();

        assertThat(actual.getModifier())
                .overridingErrorMessage("Expected modifier to be <%s> but was <%s>", modifier, actual.getModifier())
                .isEqualTo(modifier);

        return this;
    }

    public DocumentAssert hasFileLength(Long fileLenght) {
        isNotNull();

        assertThat(actual.getFileLength())
                .overridingErrorMessage("Expected fileLenght to be <%s> but was <%s>", fileLenght, actual.getFileLength())
                .isEqualTo(fileLenght);

        return this;
    }
    public DocumentAssert hasMimeType(String mimeType) {
        isNotNull();

        assertThat(actual.getFileMimeType())
                .overridingErrorMessage("Expected mimeType to be <%s> but was <%s>", mimeType, actual.getFileMimeType())
                .isEqualTo(mimeType);

        return this;
    }
    public DocumentAssert hasFileName(String fileName) {
        isNotNull();

        assertThat(actual.getFileName())
                .overridingErrorMessage("Expected fileName to be <%s> but was <%s>", fileName, actual.getFileName())
                .isEqualTo(fileName);

        return this;
    }

    public DocumentAssert hasFileStorage(String fileStorage) {
        isNotNull();

        assertThat(actual.getFileStorage())
                .overridingErrorMessage("Expected fileStorage to be <%s> but was <%s>", fileStorage, actual.getFileStorage())
                .isEqualTo(fileStorage);

        return this;
    }

    public DocumentAssert hasParent(String parent) {
        isNotNull();

        assertThat(actual.getParent())
                .overridingErrorMessage("Expected parent to be <%s> but was <%s>", parent, actual.getParent())
                .isEqualTo(parent);

        return this;
    }
}
