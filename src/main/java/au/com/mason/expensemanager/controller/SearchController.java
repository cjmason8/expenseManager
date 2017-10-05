package au.com.mason.expensemanager.controller;

import org.springframework.beans.factory.annotation.Autowired;
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
	
	@RequestMapping(value = "/search", method = RequestMethod.POST, produces = "application/json", 
			consumes = "application/json", headers = "Accept=application/json")
	SearchResultsDto findSearchResults(@RequestBody SearchParamsDto searchParamsDto) throws Exception {
		
		return searchService.findSearchResults(searchParamsDto);
    }	
	
}
