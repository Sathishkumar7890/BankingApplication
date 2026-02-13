package com.CustomerInfo.OTP;

public class ValidateOTPRequest {
	private String RelationshipID;
	private String otpReference;
	private String otp;
	private String isEncrypted;
	private String mobileNumber;
	private String ucid;
	private String requestTime;
	public String getRelationshipID() {
		return RelationshipID;
	}
	public void setRelationshipID(String relationshipID) {
		RelationshipID = relationshipID;
	}
	public String getOtpReference() {
		return otpReference;
	}
	public void setOtpReference(String otpReference) {
		this.otpReference = otpReference;
	}
	public String getOtp() {
		return otp;
	}
	public void setOtp(String otp) {
		this.otp = otp;
	}
	public String getIsEncrypted() {
		return isEncrypted;
	}
	public void setIsEncrypted(String isEncrypted) {
		this.isEncrypted = isEncrypted;
	}
	public String getMobileNumber() {
		return mobileNumber;
	}
	public void setMobileNumber(String mobileNumber) {
		this.mobileNumber = mobileNumber;
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
