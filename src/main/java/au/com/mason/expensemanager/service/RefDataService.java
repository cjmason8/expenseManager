package au.com.mason.expensemanager.service;

import java.security.InvalidParameterException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import au.com.mason.expensemanager.dao.RefDataDao;
import au.com.mason.expensemanager.domain.EntityMetadataType;
import au.com.mason.expensemanager.domain.RefData;
import au.com.mason.expensemanager.domain.RefDataType;

@Component
public class RefDataService {

	@Autowired
	private RefDataDao refDataDao;

	@Autowired
	private EntityMetadataService entityMetadataService;

	public List<RefData> getAll() throws Exception {
		List<RefData> results = refDataDao.getAll();
		hydrateRefDatas(results);
		return results;
	}

	public List<RefData> getRefData(String type) throws Exception {
		String typeVal = "";
		if (type.equals("expenseType")) {
			typeVal = RefDataType.EXPENSE_TYPE.name();
		} else if (type.equals("recurringType")) {
			typeVal = RefDataType.RECURRING_TYPE.name();
		} else if (type.equals("cause")) {
			typeVal = RefDataType.CAUSE.name();
		} else if (type.equals("incomeType")) {
			typeVal = RefDataType.INCOME_TYPE.name();
		} else {
			throw new InvalidParameterException("value " + type + " for parameter type not valid.");
		}

		List<RefData> results = refDataDao.getAllByType(typeVal);
		hydrateRefDatas(results);
		return results;
	}

	public RefData updateRefData(RefData refData) throws Exception {
		RefData updated = refDataDao.update(refData);
		persistMetadata(updated);
		hydrateRefData(updated);
		return updated;
	}

	public RefData createRefData(RefData refData) {
		RefData created = refDataDao.create(refData);
		persistMetadata(created);
		hydrateRefData(created);
		return created;
	}

	public void deleteRefData(Long id) {
		RefData refData = getById(id);
		refData.setDeleted(true);

		refDataDao.update(refData);
	}

	public RefData getById(Long id) {
		RefData refData = refDataDao.getById(id);
		hydrateRefData(refData);
		return refData;
	}

	public List<RefData> findRefDatas(RefData refData) {
		List<RefData> results = refDataDao.findRefDatas(refData);
		hydrateRefDatas(results);
		return results;
	}

	public List<RefData> getAllWithEmailKey() {
		List<RefData> results = refDataDao.getAllWithEmailKey();
		hydrateRefDatas(results);
		return results;
	}

	private void hydrateRefData(RefData refData) {
		if (refData == null) {
			return;
		}
		hydrateRefDatas(List.of(refData));
	}

	private void hydrateRefDatas(List<RefData> refDatas) {
		entityMetadataService.hydrateList(EntityMetadataType.REF_DATA, refDatas, r -> String.valueOf(r.getId()),
			(entity, entityMetadata, objectMap, stringMap) -> {
				entity.setEntityMetadata(entityMetadata);
				entity.setMetaData(stringMap);
			});
	}

	private void persistMetadata(RefData refData) {
		if (refData == null || refData.getId() == 0) {
			return;
		}
		entityMetadataService.replace(EntityMetadataType.REF_DATA, String.valueOf(refData.getId()),
			refData.getMetaData());
	}

}
