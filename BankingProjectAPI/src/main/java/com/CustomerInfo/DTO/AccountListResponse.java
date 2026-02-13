package com.CustomerInfo.DTO;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "status", "responseCode", "data" })
public class AccountListResponse {

    private String status;
    private String responseCode;
    private String message;
    private Data data;

    // ---------- Inner Data Class ----------
    @JsonPropertyOrder({ "mobileNumber", "accounts" }) // ⭐ KEY FIX
    public static class Data {

        private String mobileNumber;
        private List<Account> accounts;

        public String getMobileNumber() {
            return mobileNumber;
        }

        public void setMobileNumber(String mobileNumber) {
            this.mobileNumber = mobileNumber;
        }

        public List<Account> getAccounts() {
            return accounts;
        }

        public void setAccounts(List<Account> accounts) {
            this.accounts = accounts;
        }
    }

    // ---------- Inner Account Class ----------
    @JsonPropertyOrder({
        "accountNumber",
        "accountType",
        "currency",
        "balance",
        "productCode",
        "isActive"
    })
    public static class Account {

        private String accountNumber;
        private String accountType;
        private String currency;
        private Double balance;
        private String productCode;
        private String isActive;

        public String getAccountNumber() {
            return accountNumber;
        }
        public void setAccountNumber(String accountNumber) {
            this.accountNumber = accountNumber;
        }
        public String getAccountType() {
            return accountType;
        }
        public void setAccountType(String accountType) {
            this.accountType = accountType;
        }
        public String getCurrency() {
            return currency;
        }
        public void setCurrency(String currency) {
            this.currency = currency;
        }
        public Double getBalance() {
            return balance;
        }
        public void setBalance(Double balance) {
            this.balance = balance;
        }
        public String getProductCode() {
            return productCode;
        }
        public void setProductCode(String productCode) {
            this.productCode = productCode;
        }
        public String getIsActive() {
            return isActive;
        }
        public void setIsActive(String isActive) {
            this.isActive = isActive;
        }
    }

    // ---------- Getters & Setters ----------
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public String getResponseCode() {
        return responseCode;
    }
    public void setResponseCode(String responseCode) {
        this.responseCode = responseCode;
    }
    public Data getData() {
        return data;
    }
    public void setData(Data data) {
        this.data = data;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
}
