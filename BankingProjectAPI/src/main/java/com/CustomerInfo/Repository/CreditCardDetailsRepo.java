package com.CustomerInfo.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.CustomerInfo.Model.CustomerCardDetails;

@Repository
public interface CreditCardDetailsRepo extends JpaRepository<CustomerCardDetails, String> {
	
	@Query("SELECT c FROM CustomerCardDetails c WHERE c.creditCardNumber = :cardNum")
	CustomerCardDetails findByCreditCardNumber(@Param("cardNum") String cardNum);


    // Custom query method using proper Java property naming conventions
    List<CustomerCardDetails> findByRelationshipId(String relationshipId);
}
