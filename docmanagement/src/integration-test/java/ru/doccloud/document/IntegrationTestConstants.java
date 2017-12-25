package ru.doccloud.document;

public class IntegrationTestConstants {

    public static final String CURRENT_DESCRIPTION = "Lorem ipsum";
    public static final String CURRENT_TITLE_FIRST_DOCUMENT = "FooRai";
    public static final String CURRENT_AUTHOR_DOCUMENT = "doccloud";
    public static final String CURRENT_BASE_TYPE_DOCUMENT = "Folder";
    public static final String CURRENT_TITLE_SECOND_DOCUMENT = "Bar";

    public static final Long ID_FIRST_DOCUMENT = 1L;
    public static final Long ID_SECOND_DOCUMENT = 2L;
    public static final Long ID_THIRD_DOCUMENT = 3L;

    public static final String NEW_DESCRIPTION = "description";
    public static final String NEW_TITLE = "title";
    public static final String NEW_AUTHOR ="author";
    public static final String NEW_BASE_TYPE ="Document";

    public static final String SEARCH_TERM = "iPSu";

    public static final String SORT_FIELD_TITLE = "SYS_TITLE";

    public static final String SORT_FIELD_TYPE = "SYS_TYPE";

    public static final Long PARENT_ID = 3L;

    public static final String BASE_TYPE_DOCUMENT = "Document";
    public static final String BASE_TYPE_FOLDER = "Folder";

    public static final String FIRST_UUID = "5e97ac1b-2a0d-4439-8018-044217bbccc5";
    public static final String SECOND_UUID = "ceec86ec-5dba-4857-b39a-abb93cb38c12";

    public static final String MIME_TYPE_NEW = "mimeType_new";
    public static final String MODIFIER_NEW = "modifier_new";
    public static final String FILEPATH_NEW = "filePath_new";
    public static final String FILE_NAME_NEW = "fileName_new";
    public static final String FILE_STORAGE_NEW = "fileStorage_new";
    public static final Long FILE_LENGHT_NEW = 2L;

    public static final String JSON_QUERY = "{\"groupOp\":\"AND\",\"rules\":[{\"field\":\"sys_author\",\"op\":\"cn\",\"data\":\"doccloud\"}]}";

    public static final String JSON_QUERY_TEST = "{\"groupOp\":\"AND\",\"rules\":[{\"field\":\"store\",\"op\":\"cn\",\"data\":\"test\"}]}";

    public static final String[] FIELDS_ARR_TEST = {"store", "name"};

//    public static final String[] FIELDS_ARR = {"sys_author", "sys_title"};

    public static final String[] FIELDS_ARR_ALL = {"all"};

    public static final String TYPE = "doccloud";

    public static final String USER = "test";

    public static final Long LINK_HEAD_ID = 1L;

    public static final Long LINK_TAIL_THIRD_ID = 3L;

    public static final Long LINK_HEAD_SECOND_ID = 2L;


    public static final String SYMBOLIC_NAME = "symbolic_name";

    public static final String SYMBOLIC_NAME_NOT_EXIST = "symbolic_name_not_exist";

    public static final String SETTINGS_KEY_EXIST = "settings_key_exists";

    public static final String SETTINGS_KEY_NOT_EXIST = "settings_key_not_exists";

    private IntegrationTestConstants() {}
}
