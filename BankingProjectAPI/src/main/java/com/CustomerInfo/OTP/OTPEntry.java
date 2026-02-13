package com.CustomerInfo.OTP;

import java.time.Instant;

public class OTPEntry {

	    private String mobileNumber;
	    private String otpValue;
	    private Instant expiryTime;

		public OTPEntry(String mobileNumber, String otpValue, Instant expiryTime) {
	        this.mobileNumber = mobileNumber;
	        this.otpValue = otpValue;
	        this.expiryTime = expiryTime;
	    }
		
		public String getMobileNumber() {
			return mobileNumber;
		}

		public void setMobileNumber(String mobileNumber) {
			this.mobileNumber = mobileNumber;
		}

		public String getOtpValue() {
			return otpValue;
		}

		public void setOtpValue(String otpValue) {
			this.otpValue = otpValue;
		}

		public Instant getExpiryTime() {
			return expiryTime;
		}

		public void setExpiryTime(Instant expiryTime) {
			this.expiryTime = expiryTime;
		}    
}
