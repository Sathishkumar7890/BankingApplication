package com.CustomerInfo.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.CustomerInfo.Model.CustomerAccountDetails;

@Repository
public interface AccountDetailsRepo extends JpaRepository<CustomerAccountDetails, String> {

    // Find all records (optional, JpaRepository already has findAll)
    List<CustomerAccountDetails> findAll();
    
    CustomerAccountDetails findByAccountNumber(String accountNumber);

    // Match the entity field name exactly (camelCase)
    List<CustomerAccountDetails> findByRelationshipID(String relationshipID);
}
