package au.com.mason.expensemanager.repository;

import au.com.mason.expensemanager.domain.Donation;
import javax.transaction.Transactional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
@Transactional
public interface DonationRepository extends CrudRepository<Donation, Long> {
}
