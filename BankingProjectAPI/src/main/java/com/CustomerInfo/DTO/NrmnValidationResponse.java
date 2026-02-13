package com.CustomerInfo.DTO;

import java.util.List;

	public class NrmnValidationResponse {
		private String responseCode;
	    private Data data;
	    private String message;

	    
	    // getters and setters
	    
	    public String getResponseCode() {
			return responseCode;
		}

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
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

		public static class Data {
	        private String mobileNumber;
	        private String RelationshipID;
	        private List<Product> products;
	        // getters and setters
			public String getMobileNumber() {
				return mobileNumber;
			}
			public void setMobileNumber(String mobileNumber) {
				this.mobileNumber = mobileNumber;
			}
			public String getRelationshipID() {
				return RelationshipID;
			}
			public void setRelationshipID(String relationshipID) {
				RelationshipID = relationshipID;
			}
			public List<Product> getProducts() {
				return products;
			}
			public void setProducts(List<Product> products) {
				this.products = products;
			}
	        
	    }

	    public static class Product {
	        private String number;
	        private String type;
	        private String isActive;
	        private String accountType;
	        private String productCode;
	        private String cardType;
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
			public String getProductCode() {
				return productCode;
			}
			public void setProductCode(String productCode) {
				this.productCode = productCode;
			}
			public String getCardType() {
				return cardType;
			}
			public void setCardType(String cardType) {
				this.cardType = cardType;
			}
	       
			
	        
	    }
	        
	        
	       
	    
	}
