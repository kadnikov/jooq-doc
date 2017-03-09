package ru.doccloud.common;

import org.junit.Test;
import ru.doccloud.common.util.VersionHelper;

/**
 * Created by ilya on 3/9/17.
 */
public class TestGenerateVersions {

//    @Test
    public void generateVersions(){

        String startVersion = VersionHelper.generateMinorDocVersion(null);
        System.out.println(startVersion);
        for(int i = 1; i<=15; i++){
            startVersion = VersionHelper.generateMinorDocVersion(startVersion);
            System.out.println(startVersion);
        }
    }


}
