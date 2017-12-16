package ru.doccloud.document.model;

import org.assertj.core.api.AbstractAssert;
import ru.doccloud.document.model.SystemDocument;

import static org.assertj.core.api.Assertions.assertThat;

//todo add own test criteria
public class SystemAssert extends AbstractAssert<SystemAssert, SystemDocument> {

    private SystemAssert(SystemDocument actual) {
        super(actual, SystemAssert.class);
    }

    public static SystemAssert assertThatSystemDocument(SystemDocument actual) {
        return new SystemAssert(actual);
    }

    public SystemAssert hasDescription(String description) {
        isNotNull();

        assertThat(actual.getDescription())
                .overridingErrorMessage("Expected description to be <%s> but was <%s>", description, actual.getDescription())
                .isEqualTo(description);

        return this;
    }

    public SystemAssert hasId() {
        isNotNull();

        assertThat(actual.getId())
                .overridingErrorMessage("Expected id to be not null but was <null>")
                .isNotNull();

        return this;
    }

    public SystemAssert hasId(Long id) {
        isNotNull();

        assertThat(actual.getId())
                .overridingErrorMessage("Expected id to be <%d> but was <%d>", id, actual.getId())
                .isEqualTo(id);

        return this;
    }

    public SystemAssert hasNoDescription() {
        isNotNull();

        assertThat(actual.getDescription())
                .overridingErrorMessage("Expected description to be <null> but was <%s>", actual.getDescription());

        return this;
    }

    public SystemAssert hasNoId() {
        isNotNull();

        assertThat(actual.getId())
                .overridingErrorMessage("Expected id to be <null> but was <%d>", actual.getId())
                .isNull();

        return this;
    }

    public SystemAssert hasTitle(String title) {
        isNotNull();

        assertThat(actual.getTitle())
                .overridingErrorMessage("Expected title to be <%s> but was <%s>", title, actual.getTitle())
                .isEqualTo(title);

        return this;
    }

    public SystemAssert hasAuthor(String author) {
        isNotNull();

        assertThat(actual.getAuthor())
                .overridingErrorMessage("Expected author to be <%s> but was <%s>", author, actual.getAuthor())
                .isEqualTo(author);

        return this;
    }

    public SystemAssert hasSymbolicName(String symbolicName) {
        isNotNull();

        assertThat(actual.getSymbolicName())
                .overridingErrorMessage("Expected symbolicName to be <%s> but was <%s>", symbolicName, actual.getSymbolicName())
                .isEqualTo(symbolicName);

        return this;
    }

    public SystemAssert hasType(String type) {
        isNotNull();

        assertThat(actual.getType())
                .overridingErrorMessage("Expected tepe to be <%s> but was <%s>", type, actual.getType())
                .isEqualTo(type);

        return this;
    }

    public SystemAssert hasFilePath(String filePath) {
        isNotNull();

        assertThat(actual.getFilePath())
                .overridingErrorMessage("Expected filePath to be <%s> but was <%s>", filePath, actual.getFilePath())
                .isEqualTo(filePath);

        return this;
    }

    public SystemAssert hasModifier(String modifier) {
        isNotNull();

        assertThat(actual.getModifier())
                .overridingErrorMessage("Expected modifier to be <%s> but was <%s>", modifier, actual.getModifier())
                .isEqualTo(modifier);

        return this;
    }

    public SystemAssert hasFileLength(Long fileLenght) {
        isNotNull();

        assertThat(actual.getFileLength())
                .overridingErrorMessage("Expected fileLenght to be <%s> but was <%s>", fileLenght, actual.getFileLength())
                .isEqualTo(fileLenght);

        return this;
    }
    public SystemAssert hasMimeType(String mimeType) {
        isNotNull();

        assertThat(actual.getFileMimeType())
                .overridingErrorMessage("Expected mimeType to be <%s> but was <%s>", mimeType, actual.getFileMimeType())
                .isEqualTo(mimeType);

        return this;
    }
    public SystemAssert hasFileName(String fileName) {
        isNotNull();

        assertThat(actual.getFileName())
                .overridingErrorMessage("Expected fileName to be <%s> but was <%s>", fileName, actual.getFileName())
                .isEqualTo(fileName);

        return this;
    }

//    public SystemAssert hasFileStorage(String fileStorage) {
//        isNotNull();
//
//        assertThat(actual.getFileStorage())
//                .overridingErrorMessage("Expected fileStorage to be <%s> but was <%s>", fileStorage, actual.getFileStorage())
//                .isEqualTo(fileStorage);
//
//        return this;
//    }

    public SystemAssert hasParent(String parent) {
        isNotNull();

        assertThat(actual.getParent())
                .overridingErrorMessage("Expected parent to be <%s> but was <%s>", parent, actual.getParent())
                .isEqualTo(parent);

        return this;
    }
}
