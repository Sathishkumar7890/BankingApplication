package DTO;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
