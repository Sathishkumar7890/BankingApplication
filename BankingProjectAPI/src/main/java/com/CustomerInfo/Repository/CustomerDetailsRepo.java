package com.CustomerInfo.Repository;

import java.util.List;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.CustomerInfo.Model.CustomerDetails;


import jakarta.transaction.Transactional;

@Repository
public interface CustomerDetailsRepo extends JpaRepository<CustomerDetails, Integer> {

    List<CustomerDetails> findAll();

    Optional<CustomerDetails> findByMobileNumber(String mobileNumber); // fixed
    
    @Modifying
    @Transactional
    @Query("UPDATE CustomerDetails c SET c.preferredLanguage = :lang WHERE c.relationshipID = :relationshipID")
    int updatePreferredLanguageByRelationshipID(String relationshipID, String lang);

	Optional<CustomerDetails> findByRelationshipID(String relationshipID);
	
	@Query(value =
	        "SELECT * FROM Customer_Information WHERE RTRIM(Relationship_ID) = :relationshipId",
	        nativeQuery = true)
	    Optional<CustomerDetails> findByRelationshipId(
	            @Param("relationshipId") String relationshipId);
	
}
