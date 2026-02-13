package com.CustomerInfo.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.CustomerInfo.Model.TpinDetails;

public interface TpinDetailsRepo extends JpaRepository<TpinDetails, Integer> {

	 Optional<TpinDetails> findByRelationshipId(String relationshipId);
	    
	    @Query(value =
	            "SELECT * FROM TPIN_Details WHERE RTRIM(Relationship_ID) = :relationshipId",
	            nativeQuery = true)
	        Optional<TpinDetails> findByRelationshipIdNative(
	                @Param("relationshipId") String relationshipId);
}

