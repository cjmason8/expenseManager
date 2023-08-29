package au.com.mason.expensemanager.repository;

import au.com.mason.expensemanager.domain.Donation;
import au.com.mason.expensemanager.domain.Metadata;
import au.com.mason.expensemanager.domain.Statics;
import au.com.mason.expensemanager.dto.DonationSearchDto;
import au.com.mason.expensemanager.dto.SearchParamsDto;
import au.com.mason.expensemanager.util.DateUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import org.springframework.stereotype.Repository;

@Repository
public class CustomRepository<T extends Metadata> {
  @PersistenceContext
  protected EntityManager em;

  private Gson gson = new GsonBuilder().serializeNulls().create();

  public List<Donation> findDonations(DonationSearchDto donationSearchDto) {
    String sql = "SELECT * from donations d LEFT JOIN refdata r on d.causeId = r.id where ";
    boolean addAnd = false;
    if (donationSearchDto.getCause() != null) {
      addAnd = true;
      sql += "r.description = '" + donationSearchDto.getCause().getDescription() + "'";
    }
    if (donationSearchDto.getStartDate() != null) {
      if (addAnd) {
        sql += " AND ";
      }
      addAnd = true;
      sql += "d.dueDate >= to_date('" + DateUtil.getFormattedDbDate(donationSearchDto.getStartDate()) + "', 'yyyy-mm-dd') ";
    }
    if (donationSearchDto.getEndDate() != null) {
      if (addAnd) {
        sql += " AND ";
      }
      addAnd = true;
      sql += "d.dueDate <= to_date('" + DateUtil.getFormattedDbDate(donationSearchDto.getEndDate()) + "', 'yyyy-mm-dd') ";
    }
    if (donationSearchDto.getMetaDataChunk() != null) {
      Map<String, String> metaData = gson.fromJson(donationSearchDto.getMetaDataChunk(), Map.class);
      if (addAnd) {
        sql += " AND ";
      }
      addAnd = true;
      boolean firstOne = true;
      for (String val : metaData.keySet()) {
        if (!firstOne) {
          sql += " AND ";
        }
        firstOne = false;
        sql += "d.metaData->>'" + val + "' = '" + metaData.get(val) + "'";
      }
    }
    sql += " ORDER BY dueDate DESC,r.description";

    Query query = em.createNativeQuery(sql, Donation.class);
    query.setMaxResults(Statics.MAX_RESULTS.getIntValue());
    return query.getResultList();
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
              if (values.stream().filter(value -> convertToStringAndLower(value).equals(convertToStringAndLower(item))).count() > 0) {
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

  public void deleteById(Long id) {
    T donation = em.find(typeParameterClass, id);
    em.remove(donation);
  }

  public T getById(Long id) {
    Optional<T> item = this.findById(id);

    if (item.isEmpty()) {
      throw new RuntimeException(String.format("Item of type %s and id %s could not be found.", typeParameterClass, id));
    }

    return item.get();
  }

  private String convertToStringAndLower(Object val) {
    String stringVal = (String) val;

    return stringVal == null ? null : stringVal.toLowerCase();
  }
}
