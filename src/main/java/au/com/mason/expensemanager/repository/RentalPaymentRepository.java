package au.com.mason.expensemanager.repository;

import javax.transaction.Transactional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import au.com.mason.expensemanager.domain.RentalPayment;

@Repository
@Transactional
public interface RentalPaymentRepository extends CrudRepository<RentalPayment, Long> {
}
