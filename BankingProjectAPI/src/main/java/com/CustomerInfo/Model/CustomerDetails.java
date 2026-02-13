package com.CustomerInfo.Model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "Customer_Information")
public class CustomerDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private int slNo;

    @Column(name = "Mobile_Number")
    private String mobileNumber;

    @Column(name = "Relationship_ID")
    private String relationshipID;

    @Column(name = "Is_RMN")
    private Boolean isRMN;

    @Column(name = "Preferred_Language")
    private String preferredLanguage;

    @Column(name = "Has_tpin")
    private boolean hasTpin;

    public CustomerDetails() {
    }

    public CustomerDetails(int slNo, String mobileNumber, String relationshipID, Boolean isRMN, String preferredLanguage, Boolean hasTpin) {
        this.slNo = slNo;
        this.mobileNumber = mobileNumber;
        this.relationshipID = relationshipID;
        this.isRMN = isRMN;
        this.preferredLanguage = preferredLanguage;
        this.hasTpin = hasTpin;
    }

    // Getters and Setters
    public int getSlNo() { return slNo; }
    public void setSlNo(int slNo) { this.slNo = slNo; }

    public String getMobileNumber() { return mobileNumber; }
    public void setMobileNumber(String mobileNumber) { this.mobileNumber = mobileNumber; }

    public String getRelationshipID() { return relationshipID; }
    public void setRelationshipID(String relationshipID) { this.relationshipID = relationshipID; }

    public Boolean getIsRMN() { return isRMN; }
    public void setIsRMN(Boolean isRMN) { this.isRMN = isRMN; }

    public String getPreferredLanguage() { return preferredLanguage; }
    public void setPreferredLanguage(String preferredLanguage) { this.preferredLanguage = preferredLanguage; }

    public Boolean getHasTpin() { return hasTpin; }
    public void setHasTpin(Boolean hasTpin) { this.hasTpin = hasTpin; }

}
