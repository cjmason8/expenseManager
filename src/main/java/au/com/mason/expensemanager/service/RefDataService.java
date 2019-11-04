package au.com.mason.expensemanager.service;

import java.security.InvalidParameterException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import au.com.mason.expensemanager.dao.RefDataDao;
import au.com.mason.expensemanager.domain.RefData;
import au.com.mason.expensemanager.domain.RefDataType;

@Component
public class RefDataService {
	
	@Autowired
	private RefDataDao refDataDao;
	
	public List<RefData> getAll() throws Exception {
		return refDataDao.getAll();
	}	
	
	public List<RefData> getRefData(String type) throws Exception {
		String typeVal = "";
		if (type.equals("expenseType")) {
			typeVal = RefDataType.EXPENSE_TYPE.name();
		}
		else if (type.equals("recurringType")) {
			typeVal = RefDataType.RECURRING_TYPE.name();
		}
		else if (type.equals("cause")) {
			typeVal = RefDataType.CAUSE.name();
		}		
		else if (type.equals("incomeType")) {
			typeVal = RefDataType.INCOME_TYPE.name();
		}		
		else {
			throw new InvalidParameterException("value " + type + " for parameter type not valid.");
		}
		
		return refDataDao.getAll(typeVal);
	}
	
	public RefData updateRefData(RefData refData) throws Exception {
		return refDataDao.update(refData);
	}
	
	public RefData createRefData(RefData refData) throws Exception {
		return refDataDao.create(refData);
	}
	
	public void deleteRefData(Long id) {
		refDataDao.deleteById(id);
	}
	
	public RefData getById(Long id) throws Exception {
		return refDataDao.getById(id);
	}
	
	public List<RefData> findRefDatas(RefData refData) throws Exception {
		return refDataDao.findRefDatas(refData);
	}
	
	public List<RefData> getAllWithEmailKey() {
		return refDataDao.getAllWithEmailKey();
	}

}
