package au.com.mason.expensemanager.controller;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import au.com.mason.expensemanager.domain.EntityEntry;
import au.com.mason.expensemanager.domain.EntityType;
import au.com.mason.expensemanager.dto.EntityEntryDto;
import au.com.mason.expensemanager.dto.StatusResponseDto;
import au.com.mason.expensemanager.mapper.EntityEntryMapper;
import au.com.mason.expensemanager.service.EntityEntryService;

@RestController
public class EntityEntryController extends BaseController<EntityEntry, EntityEntryDto> {

	private static final Logger LOGGER = LogManager.getLogger(EntityEntryController.class);

	@Autowired
	private EntityEntryService entityEntryService;

	@Autowired
	public EntityEntryController(EntityEntryMapper entityEntryMapper) {
		super(entityEntryMapper);
	}

	@GetMapping(value = "/entities", produces = "application/json")
	List<EntityEntryDto> getEntities(@RequestParam(required = false) EntityType type) throws Exception {
		LOGGER.info("entering EntityEntryController getEntities - {}", type);
		List<EntityEntry> results = type == null ? entityEntryService.getAll() : entityEntryService.getAllByType(type);
		LOGGER.info("leaving EntityEntryController getEntities - {}", type);
		return convertList(results);
	}

	@GetMapping(value = "/entities/{id}", produces = "application/json")
	EntityEntryDto getEntity(@PathVariable Long id) throws Exception {
		LOGGER.info("entering EntityEntryController getEntity - {}", id);
		EntityEntryDto entityEntry = convertToDto(entityEntryService.getById(id));
		LOGGER.info("leaving EntityEntryController getEntity - {}", id);
		return entityEntry;
	}

	@PostMapping(value = "/entities", produces = "application/json", consumes = "application/json")
	EntityEntryDto addEntity(@RequestBody EntityEntryDto entityEntryDto) throws Exception {
		LOGGER.info("entering EntityEntryController addEntity - {}", entityEntryDto.getName());
		EntityEntryDto created = convertToDto(entityEntryService.createEntityEntry(convertToEntity(entityEntryDto)));
		LOGGER.info("leaving EntityEntryController addEntity - {}", entityEntryDto.getName());
		return created;
	}

	@PutMapping(value = "/entities/{id}", produces = "application/json", consumes = "application/json")
	EntityEntryDto updateEntity(@RequestBody EntityEntryDto entityEntryDto, @PathVariable Long id) throws Exception {
		LOGGER.info("entering EntityEntryController updateEntity - {}", id);
		EntityEntryDto updated = convertToDto(entityEntryService.updateEntityEntry(convertToEntity(entityEntryDto)));
		LOGGER.info("leaving EntityEntryController updateEntity - {}", id);
		return updated;
	}

	@DeleteMapping(value = "/entities/{id}", produces = "application/json")
	StatusResponseDto deleteEntity(@PathVariable Long id) {
		LOGGER.info("entering EntityEntryController deleteEntity - {}", id);
		entityEntryService.deleteEntityEntry(id);
		LOGGER.info("leaving EntityEntryController deleteEntity - {}", id);
		return new StatusResponseDto("success");
	}

}
