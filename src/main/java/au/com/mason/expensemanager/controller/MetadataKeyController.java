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

import au.com.mason.expensemanager.domain.MetadataKey;
import au.com.mason.expensemanager.dto.MetadataKeyDto;
import au.com.mason.expensemanager.dto.StatusResponseDto;
import au.com.mason.expensemanager.mapper.MetadataKeyMapper;
import au.com.mason.expensemanager.service.MetadataKeyService;

@RestController
public class MetadataKeyController extends BaseController<MetadataKey, MetadataKeyDto> {

	@Autowired
	private MetadataKeyService metadataKeyService;

	@Autowired
	public MetadataKeyController(MetadataKeyMapper metadataKeyMapper) {
		super(metadataKeyMapper);
	}

	private static final Logger LOGGER = LogManager.getLogger(MetadataKeyController.class);

	@RequestMapping(value = "/metadataKeys", method = RequestMethod.GET, produces = "application/json")
	List<MetadataKeyDto> getMetadataKeys() {
		LOGGER.info("entering MetadataKeyController getMetadataKeys");
		List<MetadataKey> metadataKeys = metadataKeyService.getAll();
		LOGGER.info("leaving MetadataKeyController getMetadataKeys");
		return convertList(metadataKeys);
	}

	@RequestMapping(value = "/metadataKeys", method = RequestMethod.POST, produces = "application/json", consumes = "application/json", headers = "Accept=application/json")
	MetadataKeyDto addMetadataKey(@RequestBody MetadataKeyDto metadataKeyDto) {
		LOGGER.info("entering MetadataKeyController addMetadataKey - " + metadataKeyDto.getName());
		MetadataKey metadataKey = metadataKeyService.create(convertToEntity(metadataKeyDto));
		LOGGER.info("leaving MetadataKeyController addMetadataKey - " + metadataKey.getName());
		return convertToDto(metadataKey);
	}

	@RequestMapping(value = "/metadataKeys/{id}", method = RequestMethod.DELETE, produces = "application/json", headers = "Accept=application/json")
	StatusResponseDto deleteMetadataKey(@PathVariable Long id) {
		LOGGER.info("entering MetadataKeyController deleteMetadataKey - " + id);
		metadataKeyService.delete(id);
		LOGGER.info("leaving MetadataKeyController deleteMetadataKey - " + id);
		return new StatusResponseDto("success");
	}

}
