package au.com.mason.expensemanager.controller;

import au.com.mason.expensemanager.domain.RefData;
import au.com.mason.expensemanager.dto.RefDataDto;
import au.com.mason.expensemanager.dto.StatusResponseDto;
import au.com.mason.expensemanager.mapper.BaseMapper;
import au.com.mason.expensemanager.mapper.RefDataMapper;
import au.com.mason.expensemanager.service.RefDataService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RefDataController extends BaseController<RefData, RefDataDto> {
	
	@Autowired
	private RefDataService refDataService;
	
	@Autowired
	public RefDataController(RefDataMapper refDataMapper) {
		super(refDataMapper);
	}
	
	private static Logger LOGGER = LogManager.getLogger(RefDataController.class);
	private Gson gson = new GsonBuilder().serializeNulls().create();
	
	@RequestMapping(value = "/refDatas/type/{type}", method = RequestMethod.GET, produces = "application/json")
	List<RefDataDto> getRefDatasByType(@PathVariable String type) throws Exception {
		LOGGER.info("entering RefDataController getRefDatasByType - " + type);
		List<RefData> refDatas = refDataService.getRefData(type);
		LOGGER.info("leaving RefDataController getRefDatasByType - " + type);
		return convertList(refDatas);
    }
	
	@RequestMapping(value = "/refDatas", method = RequestMethod.GET, produces = "application/json")
	List<RefDataDto> getRefDatas() throws Exception {
		LOGGER.info("entering RefDataController getRefDatas");
		List<RefData> refDatas = refDataService.getAll();
		LOGGER.info("leaving RefDataController getRefDatas");
		return convertList(refDatas);
    }

	@RequestMapping(value = "/refDatas", method = RequestMethod.POST, produces = "application/json", 
			consumes = "application/json", headers = "Accept=application/json")
	RefDataDto addRefData(@RequestBody RefDataDto refDataDto) throws Exception {
		LOGGER.info("entering RefDataController addRefData - " + refDataDto.getDescription());
		RefData refData = refDataService.createRefData(convertToEntity(refDataDto));
		LOGGER.info("leaving RefDataController addRefData - " + refData.getDescription());
		
		return convertToDto(refData);
    }
	
	@RequestMapping(value = "/refDatas/{id}", method = RequestMethod.PUT, produces = "application/json", 
			consumes = "application/json", headers = "Accept=application/json")
	RefDataDto updateRefData(@RequestBody RefDataDto refDataDto, Long id) throws Exception {
		LOGGER.info("entering RefDataController updateRefData - " + refDataDto.getDescription());
		RefData refData = refDataService.updateRefData(convertToEntity(refDataDto));
		
		LOGGER.info("entering RefDataController updateRefData - " + refDataDto.getDescription());
		
		return convertToDto(refData);
    }
	
	@RequestMapping(value = "/refDatas/{id}", method = RequestMethod.GET, produces = "application/json")
	RefDataDto getRefData(@PathVariable Long id) throws Exception {
		LOGGER.info("entering RefDataController getRefData - " + id);
		RefData refData = refDataService.getById(id);
		LOGGER.info("leaving RefDataController getRefData - " + id);
		
		return convertToDto(refData);
        
    }
	
	@RequestMapping(value = "/refDatas/{id}", method = RequestMethod.DELETE, produces = "application/json",
			consumes = "application/json", headers = "Accept=application/json")
	StatusResponseDto deleteRefData(@PathVariable Long id) throws Exception {
		LOGGER.info("entering RefDataController deleteRefData - " + id);
		refDataService.deleteRefData(id);
		LOGGER.info("leaving RefDataController deleteRefData - " + id);

		return new StatusResponseDto("success");
    }
	
	@RequestMapping(value = "/refDatas/search", method = RequestMethod.POST, produces = "application/json", 
			consumes = "application/json", headers = "Accept=application/json")
	List<RefDataDto> findRefDatas(@RequestBody RefDataDto refDataDto) throws Exception {
		LOGGER.info("entering RefDataController findRefDatas - " + refDataDto.getDescription());
		List<RefData> findRefDatas = refDataService.findRefDatas(convertToEntity(refDataDto));
		LOGGER.info("leaving RefDataController findRefDatas - " + refDataDto.getDescription());
		
		return convertList(findRefDatas);
    }
	
}
