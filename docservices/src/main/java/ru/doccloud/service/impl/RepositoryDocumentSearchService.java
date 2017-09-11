package ru.doccloud.service.impl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.jtransfo.JTransfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ru.doccloud.document.model.Document;
import ru.doccloud.repository.DocumentRepository;
import ru.doccloud.service.DocumentSearchService;
import ru.doccloud.service.UserService;
import ru.doccloud.service.document.dto.DocumentDTO;
import ru.doccloud.service.document.dto.UserDTO;

/**
 * @author Andrey Kadnikov
 */
@Service
public class RepositoryDocumentSearchService implements DocumentSearchService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RepositoryDocumentSearchService.class);

    private DocumentRepository repository;

    private JTransfo transformer;
    
    private UserService userService;

    @Autowired
    public RepositoryDocumentSearchService(DocumentRepository repository, JTransfo transformer, UserService userService) {
        this.repository = repository;
        this.transformer = transformer;
        this.userService = userService;
    }

    @Transactional(readOnly = true)
    @Override
    public Page<DocumentDTO> findBySearchTerm(String searchTerm, Pageable pageable) {
        LOGGER.debug("entering findBySearchTerm(searchTerm={}, pageSize= {}, pageNumber = {})",
                searchTerm,
                pageable.getPageSize(),
                pageable.getPageNumber()
        );
        List<DocumentDTO> dtos = new ArrayList<DocumentDTO>();
        long total = 0;
		try {
			Client client = new PreBuiltTransportClient(Settings.EMPTY)
			        .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("doccloud.ru"), 9300));
			HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
			LOGGER.info("httpservlet request from findBySearchTerm {} ", request);
	        
			String username = "test";
	        if(request != null){
	        	username = request.getRemoteUser();
	        }
			UserDTO userDTO = userService.getUserDto(username, "password");
			//List<String> readersArr = new ArrayList<String>();
	        //readersArr.add("test");
	        //readersArr.add("admins");
	        String[] readers = userDTO.getGroups();//readersArr.toArray(new String[0]);
	        LOGGER.debug("username {}, groups {}",username,String.join(",",readers));
	        
			QueryBuilder query = QueryBuilders.boolQuery()
					.must(QueryBuilders.queryStringQuery(searchTerm))
					.filter(QueryBuilders.termsQuery("readers", readers));
			
			SearchResponse scrollResp = client.prepareSearch("doccloud")
			        //.setTypes("609")
			        //.addSort(FieldSortBuilder.DOC_FIELD_NAME, SortOrder.ASC)
			        //.setScroll(new TimeValue(60000))
			        .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
			        .setQuery(query)
			        .setSize(pageable.getPageSize())
			        .get();
			LOGGER.debug("hits found - "+scrollResp.getHits().getHits().length);
			LOGGER.debug(scrollResp.toString());
			ObjectMapper mapper = new ObjectMapper();
			total  = scrollResp.getHits().getTotalHits();
			    for (SearchHit hit : scrollResp.getHits().getHits()) {
			    	LOGGER.debug("hit - "+hit.getId()+hit.getType()+hit.getSourceAsString());
			    	dtos.add(mapper.readValue(hit.getSourceAsString(),DocumentDTO.class));
			       
			    }
			LOGGER.debug("leaving findBySearchTerm(): found {}", dtos);

		    
			
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new PageImpl<>(dtos,
                new PageRequest(pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort()),
                total
        );
    }
    
    public Page<DocumentDTO> findInPgBySearchTerm(String searchTerm, Pageable pageable) {
        Page<Document> searchResults = repository.findBySearchTerm(searchTerm, pageable);

        List<DocumentDTO> dtos = transformer.convertList(searchResults.getContent(), DocumentDTO.class);

        LOGGER.debug("leaving findBySearchTerm(): found {}", dtos);

        return new PageImpl<>(dtos,
                new PageRequest(searchResults.getNumber(), searchResults.getSize(), searchResults.getSort()),
                searchResults.getTotalElements()
        );
    }
}
