package com.CustomerInfo.Model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "CreditCard_Details")
public class CustomerCardDetails {

    @Id
    @Column(name = "Cc_id")
    private String ccId;

    @Column(name = "Relationship_ID")
    private String relationshipId;

    @Column(name = "CreditCard_Number")
    private String creditCardNumber;

    @Column(name = "CreditCard_Type")
    private String creditCardType;

    @Column(name = "CreditCard_Balance")
    private String creditCardBalance;

    @Column(name = "Status")
    private String status;
    
    @Column(name = "Product_Code")
    private  String productCode;
 
    // Default no-arg constructor (required by JPA)
    public CustomerCardDetails() {
    }

    // Parameterized constructor
    public CustomerCardDetails(String ccId, String relationshipId, String creditCardNumber,
                               String creditCardType, String creditCardBalance, String status, String productCode) {
        this.ccId = ccId;
        this.relationshipId = relationshipId;
        this.creditCardNumber = creditCardNumber;
        this.creditCardType = creditCardType;
        this.creditCardBalance = creditCardBalance;
        this.status = status;
       this.productCode = productCode;
    }

    
    public String getProductCode() {
		return productCode;
	}

	public void setProductCode(String productCode) {
		this.productCode = productCode;
	}

	// Getters and Setters
    public String getCcId() {
        return ccId;
    }

    public void setCcId(String ccId) {
        this.ccId = ccId;
    }

    public String getRelationshipId() {
        return relationshipId;
    }

    public void setRelationshipId(String relationshipId) {
        this.relationshipId = relationshipId;
    }

    public String getCreditCardNumber() {
        return creditCardNumber;
    }

    public void setCreditCardNumber(String creditCardNumber) {
        this.creditCardNumber = creditCardNumber;
    }

    public String getCreditCardType() {
        return creditCardType;
    }

    public void setCreditCardType(String creditCardType) {
        this.creditCardType = creditCardType;
    }

    public String getCreditCardBalance() {
        return creditCardBalance;
    }

    public void setCreditCardBalance(String creditCardBalance) {
        this.creditCardBalance = creditCardBalance;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
