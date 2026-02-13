package com.CustomerInfo.Model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "TPIN_Details")
public class TpinDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tpin_id")
    private Integer tpinId;

    @Column(name = "Relationship_ID")
    private String relationshipId;

    @Column(name = "tpin_hash")
    private String tpinHash;

	public Integer getTpinId() {
		return tpinId;
	}

	public void setTpinId(Integer tpinId) {
		this.tpinId = tpinId;
	}

	public String getRelationshipId() {
		return relationshipId;
	}

	public void setRelationshipId(String relationshipId) {
		this.relationshipId = relationshipId;
	}

	public String getTpinHash() {
		return tpinHash;
	}

	public void setTpinHash(String tpinHash) {
		this.tpinHash = tpinHash;
	}

  

    

}
