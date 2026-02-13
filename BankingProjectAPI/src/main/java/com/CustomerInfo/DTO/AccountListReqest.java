package com.CustomerInfo.DTO;

public class AccountListReqest {
	private String ucid;
	private String RelationshipID;
	private String requestTime;
	
	public String getUcid() {
		return ucid;
	}
	public void setUcid(String ucid) {
		this.ucid = ucid;
	}
	public String getRelationshipID() {
		return RelationshipID;
	}
	public void setRelationshipID(String relationshipID) {
		RelationshipID = relationshipID;
	}
	public String getRequestTime() {
		return requestTime;
	}
	public void setRequestTime(String requestTime) {
		this.requestTime = requestTime;
	}
	
}
