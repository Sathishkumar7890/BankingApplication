package DTO;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CreditCardResponse {

    private String responseCode;

    // Map JSON "Message" correctly
    @JsonProperty("Message")
    private String message;
    
    
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
    

    private List<CustomerCardDetails> creditCards;

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getResponseCode() { return responseCode; }
    public void setResponseCode(String responseCode) { this.responseCode = responseCode; }

    public List<CustomerCardDetails> getCreditCards() { return creditCards; }
    public void setCreditCards(List<CustomerCardDetails> creditCards) { this.creditCards = creditCards; }

    @Override
    public String toString() {
        return "CreditCardResponse{" +
                "responseCode='" + responseCode + '\'' +
                ", message='" + message + '\'' +
                ", creditCards=" + creditCards +
                '}';
    }
}
