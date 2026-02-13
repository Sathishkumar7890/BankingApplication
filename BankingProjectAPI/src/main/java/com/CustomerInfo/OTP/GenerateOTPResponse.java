package com.CustomerInfo.OTP;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({
    "responseCode",
    "otpReference",
    "message",
    "expiry"
})
public class GenerateOTPResponse {
	private String responseCode;
	private String otpReference;
	private String message;
	private String expiry;
	
	public String getResponseCode() {
		return responseCode;
	}
	public void setResponseCode(String responseCode) {
		this.responseCode = responseCode;
	}
	public String getOtpReference() {
		return otpReference;
	}
	public void setOtpReference(String otpReference) {
		this.otpReference = otpReference;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getExpiry() {
		return expiry;
	}
	public void setExpiry(String expiry) {
		this.expiry = expiry;
	}
	
}
