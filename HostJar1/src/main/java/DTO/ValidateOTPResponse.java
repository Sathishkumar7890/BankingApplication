package DTO;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class ValidateOTPResponse {
    private String responseCode;

    // Map JSON "Message" correctly
    @com.fasterxml.jackson.annotation.JsonProperty("Message")
    private String message;

    public String getResponseCode() { return responseCode; }
    public void setResponseCode(String responseCode) { this.responseCode = responseCode; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    
    
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
    
    
    @Override
    public String toString() {
        return "ValidateOTPResponse{" +
                "responseCode='" + responseCode + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
