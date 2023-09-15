package au.com.mason.expensemanager.dao;

import au.com.mason.expensemanager.domain.Metadata;
import au.com.mason.expensemanager.dto.SearchParamsDto;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MetaDataDao<T extends Metadata> extends BaseDao<T> {

    private Gson gson = new GsonBuilder().serializeNulls().create();
    public MetaDataDao(Class<T> className, EntityManager entityManager) {
        super(className, entityManager);
    }

    protected List<T> filterByMetadata(SearchParamsDto searchParamsDto, List<T> results) {
        Map<String, Object> metaData = (Map<String, Object>) gson.fromJson(searchParamsDto.getMetaDataChunk(), Map.class);
        List<T> validResults = new ArrayList<>();
        results.forEach(result -> {
            boolean isValid = false;
            for (String val : metaData.keySet()) {
                if (result.getMetaData().get(val) == null) continue;

                if (metaData.get(val) instanceof ArrayList) {
                    for (Object item: (ArrayList) metaData.get(val)) {
                        if (result.getMetaData().get(val) instanceof String
                                && (convertToStringAndLower(result.getMetaData().get(val)).equals(convertToStringAndLower(item)))) {
                            isValid = true;
                            break;
                        }
                        else if (result.getMetaData().get(val) instanceof ArrayList) {
                            List<String> values = (List<String>) result.getMetaData().get(val);
                            if (values.stream().anyMatch(value -> convertToStringAndLower(value).equals(convertToStringAndLower(item)))) {
                                isValid = true;
                                break;
                            }
                        }
                    }
                }
                else if (result.getMetaData().get(val) instanceof ArrayList) {
                    List<String> values = (List<String>) result.getMetaData().get(val);
                    if (values.stream().filter(value -> convertToStringAndLower(value).equals(convertToStringAndLower(metaData.get(val)))).count() > 0) {
                        isValid = true;
                    }
                }
                else if (convertToStringAndLower(result.getMetaData().get(val)).equals(convertToStringAndLower(metaData.get(val)))) {
                    isValid = true;
                }
                if (searchParamsDto.getKeyWords() != null && result.getMetaData().get(val) != null
                        && convertToStringAndLower(result.getMetaData().get(val)).indexOf(convertToStringAndLower(searchParamsDto.getKeyWords())) != -1) {
                    isValid = true;
                }
            }
            if (isValid) validResults.add(result);
        });

        return validResults;
    }

    private String convertToStringAndLower(Object val) {
        String stringVal = (String) val;

        return stringVal == null ? null : stringVal.toLowerCase();
    }
}
