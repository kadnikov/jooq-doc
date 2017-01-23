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

import ru.doccloud.document.model.UploadedFile;

@Controller
@RequestMapping("/api/file")
public class FileUploadController {
	private static final Logger LOGGER = LoggerFactory.getLogger(FileUploadController.class);
	  UploadedFile ufile;
	  public FileUploadController(){
		LOGGER.info("init FileUploadController");
	    ufile = new UploadedFile();
	  }
	 
	  @RequestMapping(value = "/get/{value}", method = RequestMethod.GET)
	  public void get(HttpServletResponse response,@PathVariable String value){
	        try {
	 
	            response.setContentType(ufile.type);
	            response.setContentLength(ufile.length);
	            FileCopyUtils.copy(ufile.bytes, response.getOutputStream());
	 
	        } catch (IOException e) {
	            // TODO Auto-generated catch block
	            e.printStackTrace();
	        }
	  }
	 
	   @RequestMapping(value="/upload",headers="content-type=multipart/*",method=RequestMethod.POST)
	   public @ResponseBody JSONObject upload(MultipartHttpServletRequest request, HttpServletResponse response) {                 
	 
	     //0. notice, we have used MultipartHttpServletRequest
	 
	     //1. get the files from the request object
	     Iterator<String> itr =  request.getFileNames();
	 
	     MultipartFile mpf = request.getFile(itr.next());
	     LOGGER.info(mpf.getOriginalFilename() +" uploaded!");
	 
	     try {
	                //just temporary save file info into ufile
	        ufile.length = mpf.getBytes().length;
	        ufile.bytes= mpf.getBytes();
	        ufile.type = mpf.getContentType();
	        ufile.name = mpf.getOriginalFilename();
	 
	    } catch (IOException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
	    }
	 
	     JSONObject file = new JSONObject();
	     file.put("url", "http://localhost:8080/file/get/"+Calendar.getInstance().getTimeInMillis());
	     file.put("thumbnailUrl", "http://localhost:8080/file/get/"+Calendar.getInstance().getTimeInMillis());
	     file.put("deleteUrl", "http://localhost:8080/file/get/"+Calendar.getInstance().getTimeInMillis());
	     file.put("deleteType", "DELETE");
	     
	     file.put("size", ufile.length);
	     file.put("type", ufile.type);
	     file.put("name", ufile.name);
	     JSONArray ar = new JSONArray();
	     ar.add(file);
	     JSONObject resultJson = new JSONObject();
	     resultJson.put("files", ar);
	     return resultJson;
	 
	  }
	 
}
