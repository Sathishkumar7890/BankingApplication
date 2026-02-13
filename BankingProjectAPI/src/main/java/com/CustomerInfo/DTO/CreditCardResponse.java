package com.CustomerInfo.DTO;

import java.util.List;

import com.CustomerInfo.Model.CustomerCardDetails;

public  class CreditCardResponse {
	
	private String responseCode;
    private List<CustomerCardDetails> creditCards;
    private String Message;
        

    public String getMessage() {
		return Message;
	}
	public void setMessage(String message) {
		Message = message;
	}
	public String getResponseCode() { return responseCode; }
    public void setResponseCode(String responseCode) { this.responseCode = responseCode; }

    public List<CustomerCardDetails> getCreditCards() { return creditCards; }
    public void setCreditCards(List<CustomerCardDetails> creditCards) {
        this.creditCards = creditCards;
    }
	
}