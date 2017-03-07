package ru.doccloud.document.controller;

import java.io.IOException;
import java.util.Calendar;
import java.util.Iterator;

import javax.servlet.http.HttpServletResponse;

import org.jooq.tools.json.JSONArray;
import org.jooq.tools.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

@Controller
@RequestMapping("/api/file")
public class FileUploadController {
	private static final Logger LOGGER = LoggerFactory.getLogger(FileUploadController.class);
	  private UploadedFile ufile;
	  public FileUploadController(){
		LOGGER.info("init FileUploadController");
	    ufile = new UploadedFile();
	  }
	 
	  @RequestMapping(value = "/get/{value}", method = RequestMethod.GET)
	  public void get(HttpServletResponse response,@PathVariable String value){
	        try {
//	            todo return file from FS
	            LOGGER.info("FileUploadController method get/value " + value);
	            response.setContentType(ufile.getType());
	            response.setContentLength(ufile.getLength());
	            FileCopyUtils.copy(ufile.getBytes(), response.getOutputStream());
	 
	        } catch (IOException e) {
	            // TODO Auto-generated catch block
	            e.printStackTrace();
	        }
	  }
	 
	   @RequestMapping(value="/upload",headers="content-type=multipart/*",method=RequestMethod.POST)
	   public @ResponseBody JSONObject upload(MultipartHttpServletRequest request, HttpServletResponse response) {                 
	 
	     //0. notice, we have used MultipartHttpServletRequest
		   LOGGER.info("FileUploadController method upload ");
	     //1. get the files from the request object

	     Iterator<String> itr =  request.getFileNames();

	     MultipartFile mpf = request.getFile(itr.next());

	     LOGGER.info(mpf.getOriginalFilename() +" uploaded!");
	 
	     try {
	                //just temporary save file info into ufile
	        ufile.setLength(mpf.getBytes().length);
	        ufile.setBytes(mpf.getBytes());;
	        ufile.setType( mpf.getContentType());
	        ufile.setName(mpf.getOriginalFilename());
	 
	    } catch (IOException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
	    }
	 
	     JSONObject file = new JSONObject();
	     file.put("url", "/api/file/get/"+Calendar.getInstance().getTimeInMillis());
	     file.put("thumbnailUrl", "/api/file/get/"+Calendar.getInstance().getTimeInMillis());
	     file.put("deleteUrl", "/api/file/get/"+Calendar.getInstance().getTimeInMillis());
	     file.put("deleteType", "DELETE");
	     
	     file.put("size", ufile.getLength());
	     file.put("type", ufile.getType());
	     file.put("name", ufile.getName());
	     JSONArray ar = new JSONArray();
	     ar.add(file);
	     JSONObject resultJson = new JSONObject();
	     resultJson.put("files", ar);
		if(LOGGER.isTraceEnabled())
			LOGGER.trace(resultJson.toString());
	     return resultJson;
	 
	  }


    /**
     * Created by ilya on 3/4/17.
     */
    public static class UploadedFile {
        private int length;
        private byte[] bytes;
        private String name;
        private String type;

        public int getLength() {
            return length;
        }

        public byte[] getBytes() {
            return bytes;
        }

        public String getName() {
            return name;
        }

        public String getType() {
            return type;
        }

        public void setLength(int length) {
            this.length = length;
        }

        public void setBytes(byte[] bytes) {
            this.bytes = bytes;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setType(String type) {
            this.type = type;
        }
    }
}
