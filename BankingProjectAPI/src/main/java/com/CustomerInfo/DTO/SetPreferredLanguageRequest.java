package com.CustomerInfo.DTO;

public class SetPreferredLanguageRequest {
	private String RelationshipID;
	private String preferredLanguage;
	private String ucid;
	private String requestTime;
	public String getRelationshipID() {
		return RelationshipID;
	}
	public void setRelationshipID(String relationshipID) {
		RelationshipID = relationshipID;
	}
	public String getPreferredLanguage() {
		return preferredLanguage;
	}
	public void setPreferredLanguage(String PreferredLanguage) {
		preferredLanguage = PreferredLanguage;
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