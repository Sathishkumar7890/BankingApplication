package com.HostJar.Flow;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import org.json.JSONArray;
import org.json.JSONObject;

import com.HostJar.LoadValues.Load;

public class IVRFlow {
	
	

	// Load load = new  Load().getInstance();
	// Load.getInstance().loadConfig(configPath);
	
	public static JSONObject getNodeByDescription(String nodeName) {
	    Load obj = Load.getInstance();
	    JSONObject flowMap = obj.loadMenuJson();

	    
	    if (flowMap == null) {
	        System.out.println("Menu JSON not loaded. Cannot find node: " + nodeName);
	        return null;
	    }

	    JSONArray flowArray = flowMap.getJSONArray("flow");

	    System.out.println("Looking for node: " + nodeName);
	    for (int i = 0; i < flowArray.length(); i++) {
	        JSONObject node = flowArray.getJSONObject(i);
	        String description = node.optString("Description", "").trim();
	        System.out.println("Checking node description: '" + description + "'");

	        if (nodeName.equalsIgnoreCase(description)) {
	            // Decide type
	            String nodeType = description.toUpperCase().contains("_MENU") ? "MenuNode" : "Announce";
	            node.put("nodeType", nodeType);
	            System.out.println("Node matched: " + description);
	            return node;
	        }
	    }

	    System.out.println("Node not found in menu JSON: " + nodeName);
	    return null;
	}


	 private static final DateTimeFormatter FORMATTER =
	            DateTimeFormatter.ofPattern("HH:mm");

	    public static boolean shouldPlayEmergencyMsg()
	            {
	    	
	    	String businessHours = Load.getInstance().getProperty("BUSINESS_HOURS");
			String emgFlag = Load.getInstance().getProperty("EMG_FLAG");
	    	
	        boolean playMsg = false;

	        try {

	            if (businessHours != null &&
	                emgFlag != null &&
	                "Y".equalsIgnoreCase(emgFlag)) {

	                LocalTime now = LocalTime.now(ZoneId.of("Asia/Kolkata"));

	                String[] ranges = businessHours.split(",");

	                for (String range : ranges) {

	                    String[] times = range.trim().split("-");

	                    LocalTime start =
	                            LocalTime.parse(times[0].trim(), FORMATTER);
	                    LocalTime end =
	                            LocalTime.parse(times[1].trim(), FORMATTER);

	                    if (start.isAfter(end)) {

	                        
	                        if (!now.isBefore(start) || !now.isAfter(end)) {	                        	

	                            playMsg = true;
	                            break;
	                        }

	                    } else {

	                        if (!now.isBefore(start) && !now.isAfter(end)) {

	                            playMsg = true;
	                            break;
	                        }
	                    }
	                }
	            }

	        } catch (Exception e) {

	        }
	        return playMsg;
	    }
	
	    public static boolean isWithinBusinessHours() {
	    	
	    	String hours = Load.getInstance().getProperty("TRANSFER");
	        LocalTime now = LocalTime.now(); 

	        String[] ranges = hours.split(",");

	        for (String range : ranges) {

	            String[] times = range.trim().split("-");

	            LocalTime start = LocalTime.parse(times[0]);
	            LocalTime end = LocalTime.parse(times[1]);

	            
	            if (!now.isBefore(start) && !now.isAfter(end)) {
	                return true;
	            }
	        }
	        return false;
	    }

	    
	    
	    public static String getTransferVDN(String language, String exitLocation) {
	        Load obj = Load.getInstance();
	        JSONObject VDN = obj.loadVDNJson();// Make sure this loads your VDN JSON
 
	        if (VDN == null) {
	            System.out.println("VDN JSON not loaded.");
	            return null;
	        }
 
	        JSONArray vdnArray = VDN.getJSONArray("VDNList");
 
	        for (int i = 0; i < vdnArray.length(); i++) {
	            JSONObject node = vdnArray.getJSONObject(i);
 
	            String lang = node.optString("Language", "").trim();
	            String location = node.optString("ExitLocation", "").trim();
 
	            if (language.equalsIgnoreCase(lang) &&
	                exitLocation.equalsIgnoreCase(location)) {
 
	                return node.optString("TransferVDN", null);
	            }
	        }
 
	        System.out.println("No matching VDN found for Language: "
	                + language + " and ExitLocation: " + exitLocation);
 
	        return null;
	    }
	    public static void main(String[] args) {
	        String configPath = "D:/Banking_Project/Config/Config.properties";
	        Load.getInstance().loadConfig(configPath);

 
//	        JSONObject nextNode =
//	                IVRFlow.getNodeByDescription("GENERAL_MESSAGE");
	        String vdn = getTransferVDN("ENGLISH", "BANKING_SERVICES");
	        System.out.println(vdn);
 
	        System.out.println("-----------------------------------------------------------------------");
	    
	    }
//	    public static void main(String[] args) {
//	    	
//	        String configPath = "D:/Banking_Project/Config/Config.properties";
//	        Load.getInstance().loadConfig(configPath);
//
//	        
//	        
//	       boolean flagTrue =  isWithinBusinessHours();
//	       System.out.println("Flagture " + flagTrue);
//	        
//	        JSONObject nextNode =
//	                IVRFlow.getNodeByDescription("GENERAL_MESSAGE");
//
//	        System.out.println("-----------------------------------------------------------------------");
//
//	        if (nextNode != null) {
//	            System.out.println(nextNode);
//
//	            if (nextNode.has("nextNode")) {
//	                System.out.println("Next Node: " + nextNode.get("nextNode"));
//	            }
//	            if (nextNode.has("nodeType")) {
//	                System.out.println("Node Type: " + nextNode.getString("nodeType"));
//	            }
//	        } else {
//	            System.out.println("Node not found or Menu JSON not loaded.");
//	        }
//	    }

}
