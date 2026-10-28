package au.com.mason.expensemanager.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class DocumentServiceTest {

	@Test
	void isMaintenanceFolder_matchesPropertyYearMaintenanceFolders() {
		assertTrue(DocumentService
			.isMaintenanceFolder("/docs/expenseManager/filofax/IPs/South Kingsville/2025-2026/Maintenance"));
		assertTrue(DocumentService.isMaintenanceFolder("expenseManager/filofax/IPs/Wodonga/2018-2019/Maintenance/"));
		assertTrue(
			DocumentService.isMaintenanceFolder("/docs/expenseManager/filofax/IPs/Launceston/2015-2016/maintenance"));
	}

	@Test
	void isMaintenanceFolder_rejectsOtherFolders() {
		assertFalse(DocumentService.isMaintenanceFolder("/docs/expenseManager/filofax/IPs/South Kingsville/2025-2026"));
		assertFalse(DocumentService
			.isMaintenanceFolder("/docs/expenseManager/filofax/IPs/South Kingsville/2025-2026/Maintenance/Quotes"));
		assertFalse(
			DocumentService.isMaintenanceFolder("/docs/expenseManager/filofax/IPs/South Kingsville/Maintenance"));
		assertFalse(DocumentService.isMaintenanceFolder("/docs/expenseManager/filofax/Car/2025-2026/Maintenance"));
		assertFalse(DocumentService.isMaintenanceFolder(null));
	}

}
