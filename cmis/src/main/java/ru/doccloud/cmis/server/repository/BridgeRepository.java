package ru.doccloud.cmis.server.repository;

import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.chemistry.opencmis.commons.impl.Base64;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

abstract class BridgeRepository {
    static final String ROOT_ID = "0";

    static final String USER_UNKNOWN = "<unknown>";

    private static final String ID_PREFIX = "0000000";

    static final int BUFFER_SIZE = 64 * 1024;

    static final Pattern IN_FOLDER_QUERY_PATTERN = Pattern
            .compile("(?i)select\\s+.+\\s+from\\s+(\\S*).*\\s+where\\s+in_folder\\('(.*)'\\)");


    /** Root directory. */
    protected final File root;

    BridgeRepository(String rootPath) {
        // check root folder
        if (StringUtils.isBlank(rootPath)) {
            throw new IllegalArgumentException("Invalid root folder!");
        }

        root = new File(rootPath);

        if (!root.isDirectory()) {
            throw new IllegalArgumentException("Root is not a directory!");
        }
    }

    boolean isFolder(String docType) {
        return "folder".equals(docType);
    }

    /**
     * Returns the id of a File object or throws an appropriate exception.
     */

    static String getId(Long docID) {
        return ID_PREFIX + docID;
    }

    String getId(final File file) {
        try {
            return fileToId(file);
        } catch (Exception e) {
            throw new CmisRuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Converts an id to a File object. A simple and insecure implementation,
     * but good enough for now.
     */
    private File idToFile(final String id) throws IOException {
        if (StringUtils.isBlank(id)) {
            throw new CmisInvalidArgumentException("Id is not valid!");
        }

        if (id.equals(ROOT_ID)) {
            return root;
        }

        return new File(root, (new String(Base64.decode(id.getBytes("US-ASCII")), "UTF-8")).replace('/',
                File.separatorChar));
    }



    /**
     * Creates a File object from an id. A simple and insecure implementation,
     * but good enough for now.
     */
    String fileToId(final File file) throws IOException {
        if (file == null) {
            throw new IllegalArgumentException("File is not valid!");
        }

        if (root.equals(file)) {
            return ROOT_ID;
        }

        String path = getRepositoryPath(file);

        return Base64.encodeBytes(path.getBytes("UTF-8"));
    }

    String getRepositoryPath(final File file) {
        String path = file.getAbsolutePath().substring(root.getAbsolutePath().length())
                .replace(File.separatorChar, '/');
        if (path.length() == 0) {
            path = "/";
        } else if (path.charAt(0) != '/') {
            path = "/" + path;
        }
        return path;
    }

    /**
     * Returns the File object by id or throws an appropriate exception.
     */
    File getFile(final String id) {
        try {
            return idToFile(id);
        } catch (Exception e) {
            throw new CmisObjectNotFoundException(e.getMessage(), e);
        }
    }

    /**
     * Checks if a folder is empty. A folder is considered as empty if no files
     * or only the shadow file reside in the folder.
     *
     * @param folder
     *            the folder
     *
     * @return <code>true</code> if the folder is empty.
     */
    private boolean isFolderEmpty(final File folder) {
        if (!folder.isDirectory()) {
            return true;
        }

        String[] fileNames = folder.list();

        return (fileNames == null) || (fileNames.length == 0);

    }

    abstract boolean checkUser(CallContext context, boolean writeRequired);
}

