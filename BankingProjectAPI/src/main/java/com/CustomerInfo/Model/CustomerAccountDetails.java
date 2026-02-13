package com.CustomerInfo.Model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "Account_Details")
public class CustomerAccountDetails {

    @Id
    @Column(name = "Account_Number") // Using Account_Number as PK for simplicity
    private String accountNumber;

    @Column(name = "Relationship_ID")
    private String relationshipID;

    @Column(name = "Account_Type")
    private String accountType;

    @Column(name = "Currency")
    private String currency;

    @Column(name = "Balance")
    private double balance;

    @Column(name = "Is_Active")
    private String isActive;

    @Column(name = "Product_Code")
    private String productCode;

    // Getters and Setters
    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public String getRelationshipID() { return relationshipID; }
    public void setRelationshipID(String relationshipID) { this.relationshipID = relationshipID; }

    public String getAccountType() { return accountType; }
    public void setAccountType(String accountType) { this.accountType = accountType; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public double getBalance() { return balance; }
    public void setBalance(double balance) { this.balance = balance; }

    public String getIsActive() { return isActive; }
    public void setIsActive(String isActive) { this.isActive = isActive; }

    public String getProductCode() { return productCode; }
    public void setProductCode(String productCode) { this.productCode = productCode; }
}
