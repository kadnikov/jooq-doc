package ru.doccloud.document.filemanagement;


import org.junit.Before;
import org.junit.Test;
import ru.doccloud.document.controller.util.FileHelper;

public class TestSaveFile {

    private FileHelper fileHelper;
    private final String filePath = "/home/ilya/filenet_workspace/tasks.txt";
    @Before
    public void init(){
        fileHelper = new FileHelper();
    }

//    @Test
    public void writeFile(){

        try {
            String fileName1 = fileHelper.writeFile("testFileNama", getFileAsByteArr(filePath));
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
                String fileName1 = fileHelper.writeFile(fileName, getFileAsByteArr(filePath));
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
        return fileHelper.readFile(filePath);
    }
}
