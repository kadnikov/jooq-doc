package ru.doccloud.document.mockdocumentcontroller.mock;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import ru.doccloud.filestorage.repository.FileRepositoryImpl;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Created by ilya on 3/6/17.
 */
@WebAppConfiguration
@ContextConfiguration(classes = WebConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class TestAddContent {

    private FileRepositoryImpl fileRepositoryImpl;
    private final String filePath = "/home/ilya/filenet_workspace/tasks.txt";
    @Before
    public void init(){
        fileRepositoryImpl = new FileRepositoryImpl();
    }

    @Autowired
    private WebApplicationContext webApplicationContext;

//    @Test
    public void test() throws Exception {

        MockMultipartFile firstFile = new MockMultipartFile("data", "tasks.txt", "text/plain", getFileAsByteArr(filePath));
//        MockMultipartFile secondFile = new MockMultipartFile("data", "other-file-name.data", "text/plain", "some other type".getBytes());
//        MockMultipartFile jsonFile = new MockMultipartFile("json", "", "application/json", "{\"json\": \"someValue\"}".getBytes());

        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        mockMvc.perform(MockMvcRequestBuilders.fileUpload("/api/docs/addcontent")
                .file(firstFile)
//                .file(secondFile).file(jsonFile)
                .param("some-random", "4"))
                .andExpect(status().is(200))
                .andExpect(content().string("success"));
    }


    private byte[] getFileAsByteArr(String filePath) throws Exception {
        return fileRepositoryImpl.readFile(filePath);
    }
}
