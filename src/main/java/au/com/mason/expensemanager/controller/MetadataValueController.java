package au.com.mason.expensemanager.controller;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import au.com.mason.expensemanager.domain.MetadataValue;
import au.com.mason.expensemanager.dto.MetadataValueDto;
import au.com.mason.expensemanager.dto.StatusResponseDto;
import au.com.mason.expensemanager.mapper.MetadataValueMapper;
import au.com.mason.expensemanager.service.MetadataValueService;

@RestController
public class MetadataValueController extends BaseController<MetadataValue, MetadataValueDto> {

	@Autowired
	private MetadataValueService metadataValueService;

	@Autowired
	public MetadataValueController(MetadataValueMapper metadataValueMapper) {
		super(metadataValueMapper);
	}

	private static final Logger LOGGER = LogManager.getLogger(MetadataValueController.class);

	@RequestMapping(value = "/metadataValues", method = RequestMethod.GET, produces = "application/json")
	List<MetadataValueDto> getMetadataValues() {
		LOGGER.info("entering MetadataValueController getMetadataValues");
		List<MetadataValue> metadataValues = metadataValueService.getAll();
		LOGGER.info("leaving MetadataValueController getMetadataValues");
		return convertList(metadataValues);
	}

	@RequestMapping(value = "/metadataValues/key/{metadataKeyId}", method = RequestMethod.GET, produces = "application/json")
	List<MetadataValueDto> getMetadataValuesByKey(@PathVariable Long metadataKeyId) {
		LOGGER.info("entering MetadataValueController getMetadataValuesByKey - " + metadataKeyId);
		List<MetadataValue> metadataValues = metadataValueService.getAllByKey(metadataKeyId);
		LOGGER.info("leaving MetadataValueController getMetadataValuesByKey - " + metadataKeyId);
		return convertList(metadataValues);
	}

	@RequestMapping(value = "/metadataValues", method = RequestMethod.POST, produces = "application/json", consumes = "application/json", headers = "Accept=application/json")
	MetadataValueDto addMetadataValue(@RequestBody MetadataValueDto metadataValueDto) {
		LOGGER.info("entering MetadataValueController addMetadataValue - " + metadataValueDto.getValue());
		MetadataValue metadataValue = metadataValueService.create(convertToEntity(metadataValueDto));
		LOGGER.info("leaving MetadataValueController addMetadataValue - " + metadataValue.getValue());
		return convertToDto(metadataValue);
	}

	@RequestMapping(value = "/metadataValues/{id}", method = RequestMethod.DELETE, produces = "application/json", headers = "Accept=application/json")
	StatusResponseDto deleteMetadataValue(@PathVariable Long id) {
		LOGGER.info("entering MetadataValueController deleteMetadataValue - " + id);
		metadataValueService.delete(id);
		LOGGER.info("leaving MetadataValueController deleteMetadataValue - " + id);
		return new StatusResponseDto("success");
	}

}
