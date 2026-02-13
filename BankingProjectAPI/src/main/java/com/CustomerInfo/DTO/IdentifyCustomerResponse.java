package com.CustomerInfo.DTO;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "responseCode", "data" })
public class IdentifyCustomerResponse {

    private String responseCode;
    private String Message;
    
    @JsonInclude(JsonInclude.Include.ALWAYS)
    private Data data;

    public IdentifyCustomerResponse() {}

    public String getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(String responseCode) {
        this.responseCode = responseCode;
    }

    public String getMessage() {
        return Message;
    }

    public void setMessage(String errorMessage) {
        this.Message = errorMessage;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    // ================= DATA =================
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonPropertyOrder({
        "mobileNumber",
        "RelationshipID",
        "isRMN",
        "preferredLanguage",
        "hasTPIN",
        "products"
    })
    public static class Data {

        private String mobileNumber;

        @JsonProperty("RelationshipID")
        private String relationshipID;

        @JsonProperty("isRMN")
        private Boolean isRMN;

        private String preferredLanguage;

        @JsonProperty("hasTPIN")
        private Boolean hasTPIN;

        private List<Product> products;

        public String getMobileNumber() {
            return mobileNumber;
        }

        public void setMobileNumber(String mobileNumber) {
            this.mobileNumber = mobileNumber;
        }

        public String getRelationshipID() {
            return relationshipID;
        }

        public void setRelationshipID(String relationshipID) {
            this.relationshipID = relationshipID;
        }

        // Prevent duplicate RMN field
        @JsonIgnore
        public Boolean isRMN() {
            return isRMN;
        }

        public void setRMN(Boolean isRMN) {
            this.isRMN = isRMN;
        }

        public String getPreferredLanguage() {
            return preferredLanguage;
        }

        public void setPreferredLanguage(String preferredLanguage) {
            this.preferredLanguage = preferredLanguage;
        }

        public Boolean getHasTPIN() {
            return hasTPIN;
        }

        public void setHasTPIN(Boolean hasTPIN) {
            this.hasTPIN = hasTPIN;
        }

        public List<Product> getProducts() {
            return products;
        }

        public void setProducts(List<Product> products) {
            this.products = products;
        }
    }

    // ================= PRODUCT =================
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonPropertyOrder({
        "number",
        "type",
        "isActive",
        "accountType",
        "cardType",
        "productCode"
    })
    public static class Product {

        private String number;
        private String type;
        private String isActive;

        // ONLY for AC
        private String accountType;

        // ONLY for CC
        private String cardType;

        private String productCode;

        public String getNumber() {
            return number;
        }

        public void setNumber(String number) {
            this.number = number;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getIsActive() {
            return isActive;
        }

        public void setIsActive(String isActive) {
            this.isActive = isActive;
        }

        public String getAccountType() {
            return accountType;
        }

        public void setAccountType(String accountType) {
            this.accountType = accountType;
        }

        public String getCardType() {
            return cardType;
        }

        public void setCardType(String cardType) {
            this.cardType = cardType;
        }

        public String getProductCode() {
            return productCode;
        }

        public void setProductCode(String productCode) {
            this.productCode = productCode;
        }
    }
}
