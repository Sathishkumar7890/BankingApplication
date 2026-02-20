package DTO;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({ "status", "responseCode", "message", "data" })
public class AccountListResponse {

    private String status;
    private String responseCode;

    // Maps JSON "Message" correctly
    @JsonProperty("message")
    private String message;

    private Data data;
    
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
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonPropertyOrder({ "mobileNumber", "accounts" })
    public static class Data {
        private String mobileNumber;
        private List<Account> accounts;

        public String getMobileNumber() { return mobileNumber; }
        public void setMobileNumber(String mobileNumber) { this.mobileNumber = mobileNumber; }

        public List<Account> getAccounts() { return accounts; }
        public void setAccounts(List<Account> accounts) { this.accounts = accounts; }

        @Override
        public String toString() {
            return "Data{" +
                    "mobileNumber='" + mobileNumber + '\'' +
                    ", accounts=" + accounts +
                    '}';
        }
    }

    // ---------- Inner Account Class ----------
    @JsonInclude(JsonInclude.Include.NON_NULL)
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

        public String getAccountNumber() { return accountNumber; }
        public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
        public String getAccountType() { return accountType; }
        public void setAccountType(String accountType) { this.accountType = accountType; }
        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }
        public Double getBalance() { return balance; }
        public void setBalance(Double balance) { this.balance = balance; }
        public String getProductCode() { return productCode; }
        public void setProductCode(String productCode) { this.productCode = productCode; }
        public String getIsActive() { return isActive; }
        public void setIsActive(String isActive) { this.isActive = isActive; }

        @Override
        public String toString() {
            return "Account{" +
                    "accountNumber='" + accountNumber + '\'' +
                    ", accountType='" + accountType + '\'' +
                    ", currency='" + currency + '\'' +
                    ", balance=" + balance +
                    ", productCode='" + productCode + '\'' +
                    ", isActive='" + isActive + '\'' +
                    '}';
        }
    }

    // ---------- Getters & Setters ----------
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getResponseCode() { return responseCode; }
    public void setResponseCode(String responseCode) { this.responseCode = responseCode; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Data getData() { return data; }
    public void setData(Data data) { this.data = data; }

    @Override
    public String toString() {
        return "AccountListResponse{" +
                "status='" + status + '\'' +
                ", responseCode='" + responseCode + '\'' +
                ", message='" + message + '\'' +
                ", data=" + data +
                '}';
    }
}
