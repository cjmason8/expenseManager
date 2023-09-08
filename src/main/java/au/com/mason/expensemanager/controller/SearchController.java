package au.com.mason.expensemanager.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import au.com.mason.expensemanager.dto.SearchParamsDto;
import au.com.mason.expensemanager.dto.SearchResultsDto;
import au.com.mason.expensemanager.service.SearchService;

@RestController
public class SearchController {
	
	@Autowired
	private SearchService searchService;
	
	private static Logger LOGGER = LogManager.getLogger(SearchController.class);
	
	@PostMapping(value = "/search", produces = "application/json",
			consumes = "application/json", headers = "Accept=application/json")
	SearchResultsDto findSearchResults(@RequestBody SearchParamsDto searchParamsDto) throws Exception {
		
		LOGGER.info("entering SearchController findSearchResults");
		
		SearchResultsDto findSearchResults = searchService.findSearchResults(searchParamsDto);
		
		LOGGER.info("leaving SearchController findSearchResults");
		
		return findSearchResults;
    }	
	
}
