package ru.doccloud.document.mockdocumentcontroller.mock;

import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

/**
 * Created by ilya on 3/6/17.
 */
@WebAppConfiguration
@ContextConfiguration(classes = WebConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class TestAddContent {

//    private FileRepositoryImpl fileRepositoryImpl;
//    private final String filePath = "/home/ilya/filenet_workspace/tasks.txt";
//    @Before
//    public void init(){
//        fileRepositoryImpl = new FileRepositoryImpl();
//    }
//
//    @Autowired
//    private WebApplicationContext webApplicationContext;
//
////    @Test
//    public void test() throws Exception {
//
//        MockMultipartFile firstFile = new MockMultipartFile("data", "tasks.txt", "text/plain", getFileAsByteArr(filePath));
////        MockMultipartFile secondFile = new MockMultipartFile("data", "other-file-name.data", "text/plain", "some other type".getBytes());
////        MockMultipartFile jsonFile = new MockMultipartFile("json", "", "application/json", "{\"json\": \"someValue\"}".getBytes());
//
//        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
//        mockMvc.perform(MockMvcRequestBuilders.fileUpload("/api/docs/addcontent")
//                .file(firstFile)
////                .file(secondFile).file(jsonFile)
//                .param("some-random", "4"))
//                .andExpect(status().is(200))
//                .andExpect(content().string("success"));
//    }
//
//
//    private byte[] getFileAsByteArr(String filePath) throws Exception {
//        return fileRepositoryImpl.readFile(filePath);
//    }
}
