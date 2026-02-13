package com.CustomerInfo.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TpinRequest {

	@JsonProperty("RelationshipID")
	private String relationshipId;
    private String relationshipID;
    private String action;
    private String tpin;
    private String ucid;
    private String isEncrypted;
    private String requestTime;
	public String getRelationshipID() {
		return relationshipID;
	}
	public void setRelationshipID(String relationshipID) {
		this.relationshipID = relationshipID;
	}
	public String getAction() {
		return action;
	}
	public void setAction(String action) {
		this.action = action;
	}
	public String getTpin() {
		return tpin;
	}
	public void setTpin(String tpin) {
		this.tpin = tpin;
	}
	public String getUcid() {
		return ucid;
	}
	public void setUcid(String ucid) {
		this.ucid = ucid;
	}
	public String getIsEncrypted() {
		return isEncrypted;
	}
	public void setIsEncrypted(String isEncrypted) {
		this.isEncrypted = isEncrypted;
	}
	public String getRequestTime() {
		return requestTime;
	}
	public void setRequestTime(String requestTime) {
		this.requestTime = requestTime;
	}
    
    
}