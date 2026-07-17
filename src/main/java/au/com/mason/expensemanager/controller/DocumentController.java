package au.com.mason.expensemanager.controller;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import jakarta.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import au.com.mason.expensemanager.domain.Document;
import au.com.mason.expensemanager.domain.Donation;
import au.com.mason.expensemanager.domain.Expense;
import au.com.mason.expensemanager.domain.Income;
import au.com.mason.expensemanager.dto.DocumentDto;
import au.com.mason.expensemanager.dto.DocumentListDto;
import au.com.mason.expensemanager.dto.MoveFilesDto;
import au.com.mason.expensemanager.mapper.DocumentMapper;
import au.com.mason.expensemanager.service.DocumentService;
import au.com.mason.expensemanager.service.DonationService;
import au.com.mason.expensemanager.service.ExpenseService;
import au.com.mason.expensemanager.service.IncomeService;
import au.com.mason.expensemanager.service.S3Service;
import au.com.mason.expensemanager.util.S3Keys;

@RestController
public class DocumentController extends BaseController<Document, DocumentDto> {

	@Value("${docs.location}")
	private String docsRoot;

	@PostConstruct
	void normalizeDocsRoot() {
		docsRoot = S3Keys.normalize(docsRoot);
	}

	@Autowired
	private DonationService donationService;

	@Autowired
	private DocumentService documentService;

	@Autowired
	private ExpenseService expenseService;

	@Autowired
	private IncomeService incomeService;

	@Autowired
	private S3Service s3Service;

	@Autowired
	public DocumentController(DocumentMapper documentMapper) {
		super(documentMapper);
	}

	private static final Logger LOGGER = LogManager.getLogger(DocumentController.class);

	@PostMapping(value = "/documents/move", consumes = {"application/json"})
	String moveFiles(@RequestBody MoveFilesDto moveFilesDto) {
		LOGGER.info("entering DocumentController moveFiles for - " + moveFilesDto.getDirectoryTo());
		String destParent = S3Keys
			.toUiFolderPath("/docs/expenseManager/filofax/" + moveFilesDto.getDirectoryTo().replaceFirst("^/+", ""));
		documentService.moveFiles(destParent, moveFilesDto.getFileIds());
		LOGGER.info("leaving DocumentController moveFiles for - " + moveFilesDto.getDirectoryTo());

		return "{\"folderPath\":\"" + destParent + "\"}";
	}

	@PostMapping(value = "/documents/upload", consumes = {"multipart/form-data"}, produces = "application/json")
	DocumentDto uploadFile(@RequestPart("uploadFile") MultipartFile file, @RequestParam String type,
		@RequestParam(required = false) String path) throws Exception {

		LOGGER.info("entering DocumentController uploadFile");

		Document document = documentService.createDocument(path, type, file);

		LOGGER.info("leaving DocumentController uploadFile");

		return convertToDto(document);
	}

	@PostMapping(value = "/documents", produces = "application/json", consumes = "application/json")
	String createFile(@RequestBody DocumentDto document) throws Exception {

		LOGGER.info("entering DocumentController createFile - " + document.getFileName());

		documentService.updateDocument(convertToEntity(document));

		LOGGER.info("leaving DocumentController createFile - " + document.getFileName());

		return "{\"filePath\":\"" + document.getFolderPath() + "\"}";
	}

	@GetMapping(value = "/documents/{id}", produces = "application/json")
	DocumentDto getDocument(@PathVariable UUID id) throws Exception {
		LOGGER.info("entering DocumentController getDocument - " + id);
		DocumentDto document = convertToDto(documentService.getById(id));
		LOGGER.info("leaving DocumentController getDocument - " + id);
		return document;
	}

	@RequestMapping(value = "/documents/{id}", method = RequestMethod.PUT, produces = "application/json", consumes = "application/json", headers = "Accept=application/json")
	String updateFile(@RequestBody DocumentDto document, @PathVariable UUID id) throws Exception {

		LOGGER.info("entering DocumentController updateFile - " + id);
		document.setFolderPath(S3Keys.toUiFolderPath(document.getFolderPath()));

		documentService.updateDocument(convertToEntity(document));

		LOGGER.info("leaving DocumentController updateFile - " + id);

		return "{\"filePath\":\"" + document.getFolderPath() + "\"}";
	}

	@GetMapping(value = "/documents/{id}/archive", produces = "application/json", consumes = "application/json", headers = "Accept=application/json")
	String archiveFolder(@PathVariable UUID id) throws Exception {

		LOGGER.info("entering DocumentController archiveFolder - " + id);

		Document folder = documentService.getById(id);
		folder.setArchived(true);

		documentService.updateDocument(folder);

		LOGGER.info("leaving DocumentController archiveFolder - " + id);

		return "{\"filePath\":\"" + folder.getFolderPath() + "\"}";
	}

	@PostMapping(value = "/documents/directory", produces = "application/json", consumes = "application/json")
	String createDirectory(@RequestBody DocumentDto directory) throws Exception {

		LOGGER.info("entering DocumentController createDirectory - " + directory.getFileName());

		Document created = documentService.createDirectory(convertToEntity(directory));

		LOGGER.info("leaving DocumentController createDirectory - " + directory.getFileName());

		return "{\"folderPath\":\"" + created.getFilePath() + "\"}";
	}

	@RequestMapping(value = "/documents/directory", produces = "application/json", consumes = "application/json", method = RequestMethod.PUT)
	String updateDirectory(@RequestBody DocumentDto directory) throws Exception {
		LOGGER.info("entering DocumentController updateDocument - " + directory.getFileName());
		Document newDirectory = documentService.updateDocument(convertToEntity(directory));

		LOGGER.info("leaving DocumentController updateDocument - " + newDirectory.getFileName());

		return "{\"folderPath\":\"" + newDirectory.getFilePath() + "\"}";
	}

	@DeleteMapping(value = "/documents/{id}", produces = "application/json")
	String deleteDocument(@PathVariable UUID id) throws Exception {
		LOGGER.info("entering DocumentController deleteDocument - " + id);

		Document document = documentService.getById(id);
		String parentFolder = document.getFolderPath();

		documentService.deleteDocument(document);

		LOGGER.info("leaving DocumentController deleteDocument - " + id);

		return "{\"folderPath\":\"" + parentFolder + "\"}";
	}

	@RequestMapping(value = "/documents/get/{type}/{id}", method = RequestMethod.GET)
	public ResponseEntity<byte[]> getFile(@PathVariable Long id, @PathVariable String type) throws Exception {
		LOGGER.info("entering DocumentController getFile - " + id);

		String objectKey = getObjectKey(id, type);
		String nameHint = getFileNameHint(id, type);
		String mediaType = getContentType(nameHint);

		byte[] body = s3Service.getObjectAsBytes(objectKey);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.parseMediaType(mediaType));
		int dot = nameHint.lastIndexOf('.');
		String ext = dot >= 0 ? nameHint.substring(dot) : "";
		String filename = "output" + ext;
		headers.setContentDispositionFormData(filename, filename);
		headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

		LOGGER.info("leaving DocumentController getFile - " + id);

		return new ResponseEntity<>(body, headers, HttpStatus.OK);
	}

	@RequestMapping(value = "/documents/get/{id}", method = RequestMethod.GET)
	public ResponseEntity<byte[]> getFileById(@PathVariable UUID id) throws Exception {

		LOGGER.info("enterting DocumentController getFileById - " + id);

		Document document = documentService.getById(id);

		HttpHeaders headers = new HttpHeaders();
		String mediaType = getContentType(document.getFileName());
		headers.setContentType(MediaType.parseMediaType(mediaType));
		String filename = "output.pdf";
		headers.setContentDispositionFormData(filename, filename);
		headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

		LOGGER.info("leaving DocumentController getFileById - " + id);

		byte[] body = s3Service.getObjectAsBytes(document.getFilePath());
		return new ResponseEntity<>(body, headers, HttpStatus.OK);
	}

	@RequestMapping(value = "/documents/list", method = RequestMethod.POST)
	public List<DocumentDto> getFiles(@RequestBody DocumentListDto documentListDto) throws Exception {
		LOGGER.info("entering DocumentController getFiles - " + documentListDto.getFolderPath());
		List<DocumentDto> documents = convertList(
			documentService.getAll(documentListDto.getFolderPath(), documentListDto.getIncludeArchived()));

		Collections.sort(documents);

		LOGGER.info("leaving DocumentController getFiles - " + documentListDto.getFolderPath());

		return documents;
	}

	private String getContentType(String path) {
		if (path == null) {
			return "application/octet-stream";
		}
		String mediaType = "application/pdf";
		if (path.endsWith("doc") || path.endsWith("docx")) {
			mediaType = "application/msword";
		}
		if (path.endsWith("jpg") || path.endsWith("jpeg")) {
			mediaType = "image/jpeg";
		} else if (path.endsWith("xls") || path.endsWith("xlsx")) {
			mediaType = "application/vnd.ms-excel";
		}
		return mediaType;
	}

	private String getObjectKey(Long id, String type) throws Exception {
		if (type.equals("donations")) {
			Donation donation = donationService.getById(id);
			return donation.getDocument().getFilePath();
		} else if (type.equals("expenses")) {
			Expense expense = expenseService.getById(id);
			return expense.getDocument().getFilePath();
		} else if (type.equals("incomes")) {
			Income income = incomeService.getById(id);
			return income.getDocument().getFilePath();
		}

		return null;
	}

	private String getFileNameHint(Long id, String type) throws Exception {
		if (type.equals("donations")) {
			return donationService.getById(id).getDocument().getFileName();
		} else if (type.equals("expenses")) {
			return expenseService.getById(id).getDocument().getFileName();
		} else if (type.equals("incomes")) {
			return incomeService.getById(id).getDocument().getFileName();
		}
		return "";
	}
}
