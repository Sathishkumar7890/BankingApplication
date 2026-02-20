package DTO;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class NrmnValidationResponse {

    private String responseCode;
    private String message;
    private Data data;

    public String getResponseCode() { return responseCode; }
    public void setResponseCode(String responseCode) { this.responseCode = responseCode; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Data getData() { return data; }
    public void setData(Data data) { this.data = data; }
    
    
    @JsonIgnore
    private String url;

    @JsonIgnore
    private int httpStatus;
    
    public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public int getHttpStatus() {
		return httpStatus;
	}

	public void setHttpStatus(int httpStatus) {
		this.httpStatus = httpStatus;
	}

	@JsonIgnore   // VERY IMPORTANT
    private String token;
	
	
   
    public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}
    

    // ---------- Inner Data Class ----------
    public static class Data {
        private String mobileNumber;
        @JsonProperty("RelationshipID")
        private String RelationshipID;
        private List<Product> products;
        
        @JsonProperty("isRMN")
        private Boolean isRMN;

     
        @JsonProperty("hasTPIN")
        private Boolean hasTPIN;
        

        public Boolean getIsRMN() {
			return isRMN;
		}
		public void setIsRMN(Boolean isRMN) {
			this.isRMN = isRMN;
		}
		public Boolean getHasTPIN() {
			return hasTPIN;
		}
		public void setHasTPIN(Boolean hasTPIN) {
			this.hasTPIN = hasTPIN;
		}
		public String getMobileNumber() { return mobileNumber; }
        public void setMobileNumber(String mobileNumber) { this.mobileNumber = mobileNumber; }

        public String getRelationshipID() { return RelationshipID; }
        public void setRelationshipID(String relationshipID) { RelationshipID = relationshipID; }

        public List<Product> getProducts() { return products; }
        public void setProducts(List<Product> products) { this.products = products; }
    }

    // ---------- Inner Product Class ----------
    public static class Product {
        private String number;
        private String type;
        private String isActive;
        private String accountType;
        private String productCode;
        private String cardType;
       
        public String getNumber() { return number; }
        public void setNumber(String number) { this.number = number; }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public String getIsActive() { return isActive; }
        public void setIsActive(String isActive) { this.isActive = isActive; }

        public String getAccountType() { return accountType; }
        public void setAccountType(String accountType) { this.accountType = accountType; }

        public String getProductCode() { return productCode; }
        public void setProductCode(String productCode) { this.productCode = productCode; }

        public String getCardType() { return cardType; }
        public void setCardType(String cardType) { this.cardType = cardType; }
    }
}
