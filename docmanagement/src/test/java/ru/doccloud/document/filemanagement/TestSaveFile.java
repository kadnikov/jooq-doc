package ru.doccloud.document.filemanagement;


import org.junit.Before;
import org.junit.Test;
import ru.doccloud.document.repository.FileRepositoryImpl;

import java.util.UUID;

public class TestSaveFile {

    private FileRepositoryImpl fileRepositoryImpl;
    private final String filePath = "/home/ilya/Pictures/Screenshot from 2017-01-18 22-40-24.png";
    @Before
    public void init(){
        fileRepositoryImpl = new FileRepositoryImpl();
    }

    @Test
    public void writeFile(){

        try {
            String fileName1 = fileRepositoryImpl.writeFile( UUID.randomUUID(), getFileAsByteArr(filePath));
            System.out.println("fileName1 " + fileName1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    @Test
    public void writeFiles(){
        String fileName = "testfilename";
        for(int i=0; i <15; i++){
            fileName = fileName + i;
            try {
                String fileName1 = fileRepositoryImpl.writeFile(UUID.randomUUID(),  getFileAsByteArr(filePath));
                System.out.println("fileName1 " + fileName1);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

//    @Test
    public void readFile(){
        try {
            byte[] fileArr = getFileAsByteArr(filePath);
            System.out.println("fileSize " + fileArr.length);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private byte[] getFileAsByteArr(String filePath) throws Exception {
        return fileRepositoryImpl.readFile(filePath);
    }
}
