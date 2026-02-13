package com.CustomerInfo.OTP;

public class GenerateOTPRequest {
	private String RelationshipID;
	private String mobileNumber;
	private String channel;
	private String ucid;
	private String requestTime;
	
	public String getRelationshipID() {
		return RelationshipID;
	}
	public void setRelationshipID(String relationshipID) {
		RelationshipID = relationshipID;
	}
	public String getMobileNumber() {
		return mobileNumber;
	}
	public void setMobileNumber(String mobileNumber) {
		this.mobileNumber = mobileNumber;
	}
	public String getChannel() {
		return channel;
	}
	public void setChannel(String channel) {
		this.channel = channel;
	}
	public String getUcid() {
		return ucid;
	}
	public void setUcid(String ucid) {
		this.ucid = ucid;
	}
	public String getRequestTime() {
		return requestTime;
	}
	public void setRequestTime(String requestTime) {
		this.requestTime = requestTime;
	}
	
	
}

