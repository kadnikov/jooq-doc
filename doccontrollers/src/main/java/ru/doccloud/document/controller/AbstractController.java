package ru.doccloud.document.controller;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import ru.doccloud.service.DocumentCrudService;
import ru.doccloud.service.FileService;
import ru.doccloud.service.document.dto.AbstractDocumentDTO;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Enumeration;


abstract class AbstractController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractController.class);

    private final DocumentCrudService crudService;

    FileService fileService;

    AbstractController(FileService fileService, DocumentCrudService crudService) throws Exception {
        this.fileService = fileService;
        this.crudService = crudService;
    }

    private void initFileParamsFromRequest(AbstractDocumentDTO dto, MultipartFile mpf) throws Exception {
        LOGGER.debug("entering initFileParamsFromRequest(fileLength = {}, contentType={}, fileName={})",
                mpf.getBytes().length, mpf.getContentType(), mpf.getOriginalFilename());

        dto.setFileLength((long) mpf.getBytes().length);
        dto.setFileMimeType(mpf.getContentType());
        dto.setFileName(mpf.getOriginalFilename());
        dto.setTitle(FilenameUtils.removeExtension(mpf.getOriginalFilename()));
        LOGGER.debug("leaving initFileParamsFromRequest(): dto={}", dto);
    }

    boolean checkMultipartFile(MultipartFile mpf) throws IOException {
        return !StringUtils.isBlank(mpf.getOriginalFilename()) &&
                !StringUtils.isBlank(mpf.getContentType()) &&
                mpf.getBytes().length > 0;
    }

    void populateFilePartDto(AbstractDocumentDTO dto, MultipartHttpServletRequest request, MultipartFile mpf) throws Exception {

        LOGGER.debug("entering populateFilePartDto(dto={}, request= {}, mpf={})", dto, request, mpf);

        debugMultiPartRequestHeaderNames(request);

        initFileParamsFromRequest(dto, mpf);

        LOGGER.debug("leaving populateFilePartDto(): dto={}", dto);
    }


    private void debugMultiPartRequestHeaderNames(MultipartHttpServletRequest request) {

        if(LOGGER.isDebugEnabled()) {
            LOGGER.debug("debugMultiPartRequestHeaderNames(): getting all header parameters");
            Enumeration<String> headerNames = request.getParameterNames();//.getAttribute("data");

            if (headerNames != null) {
                while (headerNames.hasMoreElements()) {
                    String headerName = headerNames.nextElement();
                    LOGGER.debug("Header: " + headerName + " - " + request.getHeader(headerName));
                }
            }
        }
    }

    void setUser(){
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        LOGGER.info("httpservlet request from setUser {} ", request);
        if(request == null)
            crudService.setUser();
        else
            crudService.setUser(request.getRemoteUser());
    }
}
