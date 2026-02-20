package com.HostJar.API;

import java.io.FileInputStream;
import java.net.InetAddress;
import java.security.KeyStore;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.net.ssl.SSLContext;
import javax.sql.DataSource;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import com.HostJar.LoadValues.Load;
import com.fasterxml.jackson.databind.ObjectMapper;

import DTO.AccountListResponse;
import DTO.CreditCardResponse;
import DTO.DBConnectionPool;
import DTO.GenerateOTPResponse;
import DTO.IdentifyCustomerResponse;
import DTO.NrmnValidationResponse;
import DTO.SetPreferredLanguageResponse;
import DTO.TpinResponse;
import DTO.TransactionDao;
import DTO.ValidateOTPResponse;

public class api {

	private static final ObjectMapper mapper = new ObjectMapper();
	
	
	private static final Logger log = LogManager.getLogger(api.class);
	// Call this once when application starts
	private static TransactionDao transactionDao;

    //------------------------------------------------
    // STATIC BLOCK → runs once when class loads
    //------------------------------------------------
	
	public static String getServerIp() {
	    try {
	        return InetAddress.getLocalHost().getHostAddress();
	    } catch (Exception e) {
	        return "UNKNOWN";
	    }
	}

    static {
        try {

            DataSource ds = DBConnectionPool.getDataSource();
            transactionDao = new TransactionDao(ds);

            System.out.println("✅ TransactionDao initialized");

        } catch (Exception e) {

            System.out.println("❌ Failed to initialize TransactionDao");
            e.printStackTrace();
        }
    }

	// private static final Logger logger =LoggerFactory.getLogger(api.class);

	//Create ONE secure HttpClient using a single .p12 (contains 2 certs)
	private static volatile String accessToken = null;
	private static volatile long tokenExpiryTime = 0;

	private static final Object tokenLock = new Object();

	// ✅ ALWAYS use singleton
	private static final Load config = Load.getInstance();

	// =====================================================
	// ✅ LOAD CONFIG ONCE (VERY IMPORTANT)
	// =====================================================

	private static String requireProperty(String key) {

		String value = config.getProperty(key);

		if (value == null || value.trim().isEmpty()) {
			throw new RuntimeException("Missing property in Config.properties → " + key);
		}

		return value.trim();
	}

	static {

	    try {

	        String configPath = "D:/Banking_Project/Config/Config.properties";

	        Load.getInstance().loadConfig(configPath);   // ⭐⭐⭐ THIS LINE

	        System.out.println("✅ Config initialized in API");

	    } catch (Exception e) {

	        throw new RuntimeException("Failed to load config at startup", e);
	    }
	}

	private static void invalidateToken() {

		accessToken = null;
		tokenExpiryTime = 0;
	}

	// =====================================================
	// ✅ BUFFER TIME
	// =====================================================

	private static long getBufferTime() {

		return config.getLong("OAUTH_BUFFER_SECONDS", 300) * 1000;
	}

	// =====================================================
	// ✅ CREATE SECURE HTTP CLIENT
	// =====================================================

	public static void createSecureHttpClient() throws Exception {

		if (Load.getClient() != null)
			return;

		synchronized (api.class) {

			if (Load.getClient() != null)
				return;

			System.out.println("🔐 Creating Secure HttpClient...");

			String p12Path = requireProperty("p12Path");
			String p12Password = requireProperty("p12Password");

			KeyStore keyStore = KeyStore.getInstance("PKCS12");

			try (FileInputStream fis = new FileInputStream(p12Path)) {
				keyStore.load(fis, p12Password.toCharArray());
			}

			SSLContext sslContext = SSLContexts.custom().loadKeyMaterial(keyStore, p12Password.toCharArray())
					.loadTrustMaterial(keyStore, null).build();

			CloseableHttpClient client = HttpClients.custom().setSSLContext(sslContext).build();

			Load.setClient(client);

			System.out.println("✅ Secure HttpClient Created");
		}
	}

	// =====================================================
	// ✅ GET OAUTH TOKEN (BANKING SAFE)
	// =====================================================

	public static String getOAuthToken() throws Exception {

		long now = System.currentTimeMillis();
		long buffer = config.getLong("OAUTH_BUFFER_SECONDS", 300) * 1000;

		if (accessToken != null && now < (tokenExpiryTime - buffer)) {
			return accessToken;
		}

		synchronized (tokenLock) {

			now = System.currentTimeMillis();

			if (accessToken != null && now < (tokenExpiryTime - buffer)) {
				return accessToken;
			}

			createSecureHttpClient();

			CloseableHttpClient client = Load.getClient();

			System.out.println("🔐 Generating NEW OAuth token...");

			HttpPost post = new HttpPost(requireProperty("OAUTH_TOKEN_URL"));

			List<NameValuePair> params = new ArrayList<>();

			params.add(new BasicNameValuePair("grant_type", requireProperty("OAUTH_GRANT_TYPE")));

			params.add(new BasicNameValuePair("client_id", requireProperty("OAUTH_CLIENT_ID")));

			params.add(new BasicNameValuePair("client_secret", requireProperty("OAUTH_CLIENT_SECRET")));

			post.setEntity(new UrlEncodedFormEntity(params));
			post.setHeader("Accept", "application/json");
			post.setHeader("Content-Type", "application/x-www-form-urlencoded");

			try (CloseableHttpResponse response = client.execute(post)) {

				String body = EntityUtils.toString(response.getEntity());

				if (response.getStatusLine().getStatusCode() != 200) {
					throw new RuntimeException("OAuth failed → " + body);
				}

				JSONObject json = new JSONObject(body);

				accessToken = json.getString("access_token");

				long expiresIn = json.optLong("expires_in", 3600);

				tokenExpiryTime = System.currentTimeMillis() + (expiresIn * 1000);

				System.out.println("✅ Token Generated. Valid for " + expiresIn + " sec");

				return accessToken;
			}
		}
	}

	public static IdentifyCustomerResponse RMNIdentificationAPI(
	        String ucid,
	        String mobilenumber,
	        String requesttime) throws Exception {

	    String referenceNumber = UUID.randomUUID().toString();
	    String apiUrl = requireProperty("RMN_VALIDATE");

	    log.info("========== RMNIdentificationAPI START ==========");
	    log.info("ReferenceNumber: {}", referenceNumber);
	    log.info("UCID: {}, MobileNumber: {}", ucid, mobilenumber);
	    log.info("API URL: {}", apiUrl);

	    createSecureHttpClient();
	    CloseableHttpClient client = Load.getClient();

	    JSONObject json = new JSONObject();
	    json.put("ucid", ucid);
	    json.put("mobileNumber", mobilenumber);
	    json.put("requestTime", requesttime);

	    String token = getOAuthToken();

	    log.debug("Generated OAuth Token Successfully");
	    log.debug("Request Payload: {}", json.toString());

	    String body = "";
	    int statusCode = 500;
	    String status = "FAILED";

	    Timestamp startTimestamp = new Timestamp(System.currentTimeMillis());
	    long startTime = System.currentTimeMillis();

	    try {

	        HttpPost post = new HttpPost(apiUrl);
	        post.setHeader("Authorization", "Bearer " + token);
	        post.setHeader("Accept", "application/json");
	        post.setHeader("Content-Type", "application/json");
	        post.setEntity(new StringEntity(json.toString(), ContentType.APPLICATION_JSON));

	        log.info("Calling RMN Identification API...");

	        try (CloseableHttpResponse response = client.execute(post)) {

	            statusCode = response.getStatusLine().getStatusCode();
	            body = EntityUtils.toString(response.getEntity());

	            log.info("API Response Status Code: {}", statusCode);
	            log.debug("API Response Body: {}", body);

	            //-----------------------------------
	            // TOKEN RETRY
	            //-----------------------------------
	            if (statusCode == 401 || statusCode == 403) {

	                log.warn("Token expired or unauthorized. Retrying with new token...");

	                invalidateToken();
	                token = getOAuthToken();

	                HttpPost retryPost = new HttpPost(apiUrl);
	                retryPost.setHeader("Authorization", "Bearer " + token);
	                retryPost.setHeader("Accept", "application/json");
	                retryPost.setHeader("Content-Type", "application/json");
	                retryPost.setEntity(new StringEntity(json.toString(), ContentType.APPLICATION_JSON));

	                try (CloseableHttpResponse retryResponse =
	                             client.execute(retryPost)) {

	                    statusCode = retryResponse.getStatusLine().getStatusCode();
	                    body = EntityUtils.toString(retryResponse.getEntity());

	                    log.info("Retry Response Status Code: {}", statusCode);
	                    log.debug("Retry Response Body: {}", body);
	                }
	            }
	        }

	        IdentifyCustomerResponse responseDto =
	                mapper.readValue(body, IdentifyCustomerResponse.class);

	        responseDto.setHttpStatus(statusCode);

	        if ("0000".equals(responseDto.getResponseCode())) {
	            status = "SUCCESS";
	        }

	        long endTime = System.currentTimeMillis();
	        log.info("Execution Time: {} ms", (endTime - startTime));
	        log.info("RMNIdentificationAPI Completed with Status: {}", status);

	        return responseDto;

	    } catch (Exception ex) {

	        body = ex.getMessage();
	        log.error("Exception occurred in RMNIdentificationAPI", ex);
	        throw ex;

	    } finally {

	        Timestamp endTimestamp = new Timestamp(System.currentTimeMillis());

	        if (transactionDao != null) {

	            boolean inserted = transactionDao.insertTransaction(
	                    ucid,
	                    referenceNumber,
	                    startTimestamp,
	                    endTimestamp,
	                    "RMN_IDENTIFICATION",
	                    apiUrl,
	                    json.toString(),
	                    body,
	                    status,
	                    statusCode,
	                    getServerIp()
	            );

	            if (inserted) {
	                log.info("Transaction stored successfully: {}", referenceNumber);
	            } else {
	                log.error("Transaction NOT stored!");
	            }
	        }

	        log.info("========== RMNIdentificationAPI END ==========");
	    }
	}



	// ------------------------ SetPreferredLanguageAPI ------------------------
	public static SetPreferredLanguageResponse SetPreferredLanguageAPI(
	        String ucid,
	        String relationShipID,
	        String preferredLanguage,
	        String requesttime) throws Exception {

	    String referenceNumber = UUID.randomUUID().toString();
	    String apiUrl = requireProperty("SET_PREFERRED_LANG");

	    log.info("========== SetPreferredLanguageAPI START ==========");
	    log.info("ReferenceNumber: {}", referenceNumber);
	    log.info("UCID: {}", ucid);
	    log.info("RelationshipID: {}", relationShipID);
	    log.info("PreferredLanguage: {}", preferredLanguage);
	    log.info("API URL: {}", apiUrl);

	    CloseableHttpClient client = Load.getClient();
	    if (client == null) {
	        createSecureHttpClient();
	        client = Load.getClient();
	        log.debug("Secure HttpClient Created");
	    }

	    //-----------------------------------
	    // Build JSON
	    //-----------------------------------
	    JSONObject json = new JSONObject();
	    json.put("RelationshipID", relationShipID);
	    json.put("preferredLanguage", preferredLanguage);
	    json.put("ucid", ucid);
	    json.put("requestTime", requesttime);

	    String token = getOAuthToken();

	    log.debug("OAuth Token Generated Successfully");
	    log.debug("Request Payload: {}", json.toString());

	    String body = "";
	    int statusCode = 500;
	    String status = "FAILED";

	    Timestamp startTimestamp =
	            new Timestamp(System.currentTimeMillis());

	    long startTime = System.currentTimeMillis();

	    try {

	        HttpPost post = new HttpPost(apiUrl);
	        post.setHeader("Authorization", "Bearer " + token);
	        post.setHeader("Accept", "application/json");
	        post.setHeader("Content-Type", "application/json");
	        post.setEntity(new StringEntity(json.toString(), ContentType.APPLICATION_JSON));

	        log.info("Calling Set Preferred Language API...");

	        //-----------------------------------
	        // FIRST CALL
	        //-----------------------------------
	        try (CloseableHttpResponse response = client.execute(post)) {

	            statusCode = response.getStatusLine().getStatusCode();
	            body = EntityUtils.toString(response.getEntity());

	            log.info("API Response Status Code: {}", statusCode);
	            log.debug("API Response Body: {}", body);

	            //-----------------------------------
	            // TOKEN RETRY
	            //-----------------------------------
	            if (statusCode == 401 || statusCode == 403) {

	                log.warn("Token expired or unauthorized. Retrying with new token...");

	                invalidateToken();
	                token = getOAuthToken();

	                HttpPost retryPost = new HttpPost(apiUrl);
	                retryPost.setHeader("Authorization", "Bearer " + token);
	                retryPost.setHeader("Accept", "application/json");
	                retryPost.setHeader("Content-Type", "application/json");
	                retryPost.setEntity(new StringEntity(json.toString(), ContentType.APPLICATION_JSON));

	                try (CloseableHttpResponse retryResponse =
	                             client.execute(retryPost)) {

	                    statusCode = retryResponse.getStatusLine().getStatusCode();
	                    body = EntityUtils.toString(retryResponse.getEntity());

	                    log.info("Retry Response Status Code: {}", statusCode);
	                    log.debug("Retry Response Body: {}", body);
	                }
	            }
	        }

	        //-----------------------------------
	        // PARSE RESPONSE
	        //-----------------------------------
	        SetPreferredLanguageResponse responseDto =
	                mapper.readValue(body, SetPreferredLanguageResponse.class);

	        responseDto.setToken(token);
	        responseDto.setUrl(apiUrl);
	        responseDto.setHttpStatus(statusCode);

	        if ("0000".equals(responseDto.getResponseCode())) {
	            status = "SUCCESS";
	        }

	        long endTime = System.currentTimeMillis();
	        log.info("Execution Time: {} ms", (endTime - startTime));
	        log.info("SetPreferredLanguageAPI Completed with Status: {}", status);

	        return responseDto;

	    } catch (Exception ex) {

	        body = ex.getMessage();
	        log.error("Exception occurred in SetPreferredLanguageAPI", ex);
	        throw ex;

	    } finally {

	        Timestamp endTimestamp =
	                new Timestamp(System.currentTimeMillis());

	        //-----------------------------------
	        // INSERT TRANSACTION
	        //-----------------------------------
	        try {

	            if (transactionDao != null) {

	                boolean inserted =
	                        transactionDao.insertTransaction(
	                                ucid,
	                                referenceNumber,
	                                startTimestamp,
	                                endTimestamp,
	                                "SET_PREFERRED_LANGUAGE",
	                                apiUrl,
	                                json.toString(),
	                                body,
	                                status,
	                                statusCode,
	                                getServerIp()
	                        );

	                if (inserted) {
	                    log.info("Transaction Stored Successfully: {}", referenceNumber);
	                } else {
	                    log.error("Transaction NOT Stored!");
	                }
	            }

	        } catch (Exception logEx) {

	            // NEVER break API because of logging
	            log.error("Error while inserting transaction log", logEx);
	        }

	        log.info("========== SetPreferredLanguageAPI END ==========");
	    }
	}


	public static AccountListResponse AccountList(
	        String ucid,
	        String relationshipID,
	        String requestTime) throws Exception {

	    String referenceNumber = UUID.randomUUID().toString();
	    String apiUrl = requireProperty("ACC_LIST");

	    log.info("========== AccountList API START ==========");
	    log.info("ReferenceNumber: {}", referenceNumber);
	    log.info("UCID: {}", ucid);
	    log.info("RelationshipID: {}", relationshipID);
	    log.info("API URL: {}", apiUrl);

	    CloseableHttpClient client = Load.getClient();
	    if (client == null) {
	        createSecureHttpClient();
	        client = Load.getClient();
	        log.debug("Secure HttpClient Created");
	    }

	    //-----------------------------------
	    // Build JSON
	    //-----------------------------------
	    JSONObject json = new JSONObject();
	    json.put("ucid", ucid);
	    json.put("RelationshipID", relationshipID);
	    json.put("requestTime", requestTime);

	    String token = getOAuthToken();

	    log.debug("OAuth Token Generated Successfully");
	    log.debug("Request Payload: {}", json.toString());

	    String body = "";
	    int statusCode = 500;
	    String status = "FAILED";

	    Timestamp startTimestamp =
	            new Timestamp(System.currentTimeMillis());

	    long startTime = System.currentTimeMillis();

	    try {

	        HttpPost post = new HttpPost(apiUrl);
	        post.setHeader("Authorization", "Bearer " + token);
	        post.setHeader("Accept", "application/json");
	        post.setHeader("Content-Type", "application/json");
	        post.setEntity(new StringEntity(json.toString(), ContentType.APPLICATION_JSON));

	        log.info("Calling AccountList API...");

	        //-----------------------------------
	        // FIRST CALL
	        //-----------------------------------
	        try (CloseableHttpResponse response = client.execute(post)) {

	            statusCode = response.getStatusLine().getStatusCode();
	            body = EntityUtils.toString(response.getEntity());

	            log.info("API Response Status Code: {}", statusCode);
	            log.debug("API Response Body: {}", body);

	            //-----------------------------------
	            // TOKEN RETRY
	            //-----------------------------------
	            if (statusCode == 401 || statusCode == 403) {

	                log.warn("Token expired or unauthorized. Retrying with new token...");

	                invalidateToken();
	                token = getOAuthToken();

	                HttpPost retryPost = new HttpPost(apiUrl);
	                retryPost.setHeader("Authorization", "Bearer " + token);
	                retryPost.setHeader("Accept", "application/json");
	                retryPost.setHeader("Content-Type", "application/json");
	                retryPost.setEntity(new StringEntity(json.toString(), ContentType.APPLICATION_JSON));

	                try (CloseableHttpResponse retryResponse =
	                             client.execute(retryPost)) {

	                    statusCode = retryResponse.getStatusLine().getStatusCode();
	                    body = EntityUtils.toString(retryResponse.getEntity());

	                    log.info("Retry Response Status Code: {}", statusCode);
	                    log.debug("Retry Response Body: {}", body);
	                }
	            }
	        }

	        //-----------------------------------
	        // PARSE RESPONSE
	        //-----------------------------------
	        AccountListResponse responseDto =
	                mapper.readValue(body, AccountListResponse.class);

	        responseDto.setToken(token);
	        responseDto.setUrl(apiUrl);
	        responseDto.setHttpStatus(statusCode);

	        if ("0000".equals(responseDto.getResponseCode())) {
	            status = "SUCCESS";
	        }

	        long endTime = System.currentTimeMillis();
	        log.info("Execution Time: {} ms", (endTime - startTime));
	        log.info("AccountList API Completed with Status: {}", status);

	        return responseDto;

	    } catch (Exception ex) {

	        body = ex.getMessage();
	        log.error("Exception occurred in AccountList API", ex);
	        throw ex;

	    } finally {

	        Timestamp endTimestamp =
	                new Timestamp(System.currentTimeMillis());

	        //-----------------------------------
	        // INSERT TRANSACTION
	        //-----------------------------------
	        try {

	            if (transactionDao != null) {

	                boolean inserted =
	                        transactionDao.insertTransaction(
	                                ucid,
	                                referenceNumber,
	                                startTimestamp,
	                                endTimestamp,
	                                "ACCOUNT_LIST",
	                                apiUrl,
	                                json.toString(),
	                                body,
	                                status,
	                                statusCode,
	                                getServerIp()
	                        );

	                if (inserted) {
	                    log.info("Transaction Stored Successfully: {}", referenceNumber);
	                } else {
	                    log.error("Transaction NOT Stored");
	                }
	            }

	        } catch (Exception logEx) {

	            log.error("Error while inserting transaction log", logEx);
	        }

	        log.info("========== AccountList API END ==========");
	    }
	}



	// ------------------------ GenerateOTP ------------------------
	public static GenerateOTPResponse GenerateOTP(
	        String ucid,
	        String RelationShipID,
	        String mobilenumber,
	        String Channel,
	        String requesttime) throws Exception {

	    String referenceNumber = UUID.randomUUID().toString();
	    String apiUrl = requireProperty("OTP_GENERATE");

	    log.info("========== GenerateOTP API START ==========");
	    log.info("ReferenceNumber: {}", referenceNumber);
	    log.info("UCID: {}", ucid);
	    log.info("RelationshipID: {}", RelationShipID);
	    log.info("Channel: {}", Channel);

	    // Mask mobile number for security
	    if (mobilenumber != null && mobilenumber.length() >= 4) {
	        log.info("MobileNumber: ****{}", 
	                mobilenumber.substring(mobilenumber.length() - 4));
	    }

	    log.info("API URL: {}", apiUrl);

	    CloseableHttpClient client = Load.getClient();
	    if (client == null) {
	        createSecureHttpClient();
	        client = Load.getClient();
	        log.debug("Secure HttpClient Created");
	    }

	    //-----------------------------------
	    // Build JSON
	    //-----------------------------------
	    JSONObject json = new JSONObject();
	    json.put("RelationshipID", RelationShipID);
	    json.put("mobileNumber", mobilenumber);
	    json.put("channel", Channel);
	    json.put("ucid", ucid);
	    json.put("requestTime", requesttime);

	    String token = getOAuthToken();

	    log.debug("OAuth Token Generated Successfully");
	    log.debug("Request Payload: {}", json.toString());

	    String body = "";
	    int statusCode = 500;
	    String status = "FAILED";

	    Timestamp startTimestamp =
	            new Timestamp(System.currentTimeMillis());

	    long startTime = System.currentTimeMillis();

	    try {

	        HttpPost post = new HttpPost(apiUrl);
	        post.setHeader("Authorization", "Bearer " + token);
	        post.setHeader("Accept", "application/json");
	        post.setHeader("Content-Type", "application/json");
	        post.setEntity(new StringEntity(json.toString(), ContentType.APPLICATION_JSON));

	        log.info("Calling GenerateOTP API...");

	        //-----------------------------------
	        // FIRST CALL
	        //-----------------------------------
	        try (CloseableHttpResponse response = client.execute(post)) {

	            statusCode = response.getStatusLine().getStatusCode();
	            body = EntityUtils.toString(response.getEntity());

	            log.info("API Response Status Code: {}", statusCode);
	            log.debug("API Response Body: {}", body);

	            //-----------------------------------
	            // TOKEN RETRY
	            //-----------------------------------
	            if (statusCode == 401 || statusCode == 403) {

	                log.warn("Token expired or unauthorized. Retrying with new token...");

	                invalidateToken();
	                token = getOAuthToken();

	                HttpPost retryPost = new HttpPost(apiUrl);
	                retryPost.setHeader("Authorization", "Bearer " + token);
	                retryPost.setHeader("Accept", "application/json");
	                retryPost.setHeader("Content-Type", "application/json");
	                retryPost.setEntity(new StringEntity(json.toString(), ContentType.APPLICATION_JSON));

	                try (CloseableHttpResponse retryResponse =
	                             client.execute(retryPost)) {

	                    statusCode = retryResponse.getStatusLine().getStatusCode();
	                    body = EntityUtils.toString(retryResponse.getEntity());

	                    log.info("Retry Response Status Code: {}", statusCode);
	                    log.debug("Retry Response Body: {}", body);
	                }
	            }
	        }

	        //-----------------------------------
	        // PARSE RESPONSE
	        //-----------------------------------
	        GenerateOTPResponse responseDto =
	                mapper.readValue(body, GenerateOTPResponse.class);

	        responseDto.setToken(token);
	        responseDto.setUrl(apiUrl);
	        responseDto.setHttpStatus(statusCode);

	        if ("0000".equals(responseDto.getResponseCode())) {
	            status = "SUCCESS";
	        }

	        long endTime = System.currentTimeMillis();
	        log.info("Execution Time: {} ms", (endTime - startTime));
	        log.info("GenerateOTP API Completed with Status: {}", status);

	        return responseDto;

	    } catch (Exception ex) {

	        body = ex.getMessage();
	        log.error("Exception occurred in GenerateOTP API", ex);
	        throw ex;

	    } finally {

	        Timestamp endTimestamp =
	                new Timestamp(System.currentTimeMillis());

	        //-----------------------------------
	        // INSERT TRANSACTION
	        //-----------------------------------
	        try {

	            if (transactionDao != null) {

	                boolean inserted =
	                        transactionDao.insertTransaction(
	                                ucid,
	                                referenceNumber,
	                                startTimestamp,
	                                endTimestamp,
	                                "GENERATE_OTP",
	                                apiUrl,
	                                json.toString(),
	                                body,
	                                status,
	                                statusCode,
	                                getServerIp()
	                        );

	                if (inserted) {
	                    log.info("OTP Transaction Stored Successfully: {}", referenceNumber);
	                } else {
	                    log.error("OTP Transaction NOT Stored");
	                }
	            }

	        } catch (Exception logEx) {

	            // NEVER break API because logging failed
	            log.error("Error while inserting OTP transaction log", logEx);
	        }

	        log.info("========== GenerateOTP API END ==========");
	    }
	}


	// ------------------------ ValidateOTP ------------------------
	public static ValidateOTPResponse ValidateOTP(
	        String ucid,
	        String RelationShipID,
	        String otpReference,
	        String isEncrypted,
	        String mobilenumber,
	        String otp,
	        String requesttime) throws Exception {

	    String referenceNumber = UUID.randomUUID().toString();
	    String apiUrl = requireProperty("OTP_VALIDATE");

	    log.info("========== ValidateOTP API START ==========");
	    log.info("ReferenceNumber: {}", referenceNumber);
	    log.info("UCID: {}", ucid);
	    log.info("RelationshipID: {}", RelationShipID);
	    log.info("OTP Reference: {}", otpReference);
	    log.info("IsEncrypted: {}", isEncrypted);

	    // Mask mobile number
	    if (mobilenumber != null && mobilenumber.length() >= 4) {
	        log.info("MobileNumber: ****{}",
	                mobilenumber.substring(mobilenumber.length() - 4));
	    }

	    log.info("API URL: {}", apiUrl);

	    CloseableHttpClient client = Load.getClient();
	    if (client == null) {
	        createSecureHttpClient();
	        client = Load.getClient();
	        log.debug("Secure HttpClient Created");
	    }

	    //-----------------------------------
	    // Build JSON
	    //-----------------------------------
	    JSONObject json = new JSONObject();
	    json.put("RelationshipID", RelationShipID);
	    json.put("otpReference", otpReference);
	    json.put("otp", otp);  // DO NOT log this
	    json.put("isEncrypted", isEncrypted);
	    json.put("mobileNumber", mobilenumber);
	    json.put("ucid", ucid);
	    json.put("requestTime", requesttime);

	    String token = getOAuthToken();

	    log.debug("OAuth Token Generated Successfully");

	    // ⚠️ NEVER log OTP value
	    log.debug("Request Payload prepared (OTP masked)");

	    String body = "";
	    int statusCode = 500;
	    String status = "FAILED";

	    Timestamp startTimestamp =
	            new Timestamp(System.currentTimeMillis());

	    long startTime = System.currentTimeMillis();

	    try {

	        HttpPost post = new HttpPost(apiUrl);
	        post.setHeader("Authorization", "Bearer " + token);
	        post.setHeader("Accept", "application/json");
	        post.setHeader("Content-Type", "application/json");
	        post.setEntity(new StringEntity(json.toString(), ContentType.APPLICATION_JSON));

	        log.info("Calling ValidateOTP API...");

	        //-----------------------------------
	        // FIRST CALL
	        //-----------------------------------
	        try (CloseableHttpResponse response = client.execute(post)) {

	            statusCode = response.getStatusLine().getStatusCode();
	            body = EntityUtils.toString(response.getEntity());

	            log.info("API Response Status Code: {}", statusCode);
	            log.debug("API Response Body: {}", body);

	            //-----------------------------------
	            // TOKEN RETRY
	            //-----------------------------------
	            if (statusCode == 401 || statusCode == 403) {

	                log.warn("Token expired or unauthorized. Retrying with new token...");

	                invalidateToken();
	                token = getOAuthToken();

	                HttpPost retryPost = new HttpPost(apiUrl);
	                retryPost.setHeader("Authorization", "Bearer " + token);
	                retryPost.setHeader("Accept", "application/json");
	                retryPost.setHeader("Content-Type", "application/json");
	                retryPost.setEntity(new StringEntity(json.toString(), ContentType.APPLICATION_JSON));

	                try (CloseableHttpResponse retryResponse =
	                             client.execute(retryPost)) {

	                    statusCode = retryResponse.getStatusLine().getStatusCode();
	                    body = EntityUtils.toString(retryResponse.getEntity());

	                    log.info("Retry Response Status Code: {}", statusCode);
	                    log.debug("Retry Response Body: {}", body);
	                }
	            }
	        }

	        //-----------------------------------
	        // PARSE RESPONSE
	        //-----------------------------------
	        ValidateOTPResponse responseDto =
	                mapper.readValue(body, ValidateOTPResponse.class);

	        responseDto.setToken(token);
	        responseDto.setUrl(apiUrl);
	        responseDto.setHttpStatus(statusCode);

	        if ("0000".equals(responseDto.getResponseCode())) {
	            status = "SUCCESS";
	        }

	        long endTime = System.currentTimeMillis();
	        log.info("Execution Time: {} ms", (endTime - startTime));
	        log.info("ValidateOTP API Completed with Status: {}", status);

	        return responseDto;

	    } catch (Exception ex) {

	        body = ex.getMessage();
	        log.error("Exception occurred in ValidateOTP API", ex);
	        throw ex;

	    } finally {

	        Timestamp endTimestamp =
	                new Timestamp(System.currentTimeMillis());

	        //-----------------------------------
	        // INSERT TRANSACTION
	        //-----------------------------------
	        try {

	            if (transactionDao != null) {

	                boolean inserted =
	                        transactionDao.insertTransaction(
	                                ucid,
	                                referenceNumber,
	                                startTimestamp,
	                                endTimestamp,
	                                "VALIDATE_OTP",
	                                apiUrl,
	                                json.toString(),
	                                body,
	                                status,
	                                statusCode,
	                                getServerIp()
	                        );

	                if (inserted) {
	                    log.info("ValidateOTP Transaction Stored Successfully: {}", referenceNumber);
	                } else {
	                    log.error("ValidateOTP Transaction NOT Stored");
	                }
	            }

	        } catch (Exception logEx) {

	            // NEVER break API because logging failed
	            log.error("Error while inserting ValidateOTP transaction log", logEx);
	        }

	        log.info("========== ValidateOTP API END ==========");
	    }
	}


	// ------------------------ CreditCardList ------------------------
	public static CreditCardResponse CreditCardList(
	        String ucid,
	        String RelationShipID,
	        String requesttime) throws Exception {

	    String referenceNumber = UUID.randomUUID().toString();
	    String apiUrl = requireProperty("CREDIT_CARD_LIST");

	    log.info("========== CreditCardList API START ==========");
	    log.info("ReferenceNumber: {}", referenceNumber);
	    log.info("UCID: {}", ucid);
	    log.info("RelationshipID: {}", RelationShipID);
	    log.info("API URL: {}", apiUrl);

	    CloseableHttpClient client = Load.getClient();
	    if (client == null) {
	        createSecureHttpClient();
	        client = Load.getClient();
	        log.debug("Secure HttpClient Created");
	    }

	    //-----------------------------------
	    // Build JSON
	    //-----------------------------------
	    JSONObject json = new JSONObject();
	    json.put("relationshipId", RelationShipID);
	    json.put("ucid", ucid);
	    json.put("requestTime", requesttime);

	    String token = getOAuthToken();

	    log.debug("OAuth Token Generated Successfully");
	    log.debug("Request Payload: {}", json.toString());

	    String body = "";
	    int statusCode = 500;
	    String status = "FAILED";

	    Timestamp startTimestamp =
	            new Timestamp(System.currentTimeMillis());

	    long startTime = System.currentTimeMillis();

	    try {

	        HttpPost post = new HttpPost(apiUrl);
	        post.setHeader("Authorization", "Bearer " + token);
	        post.setHeader("Accept", "application/json");
	        post.setHeader("Content-Type", "application/json");
	        post.setEntity(new StringEntity(json.toString(), ContentType.APPLICATION_JSON));

	        log.info("Calling CreditCardList API...");

	        //-----------------------------------
	        // FIRST CALL
	        //-----------------------------------
	        try (CloseableHttpResponse response = client.execute(post)) {

	            statusCode = response.getStatusLine().getStatusCode();
	            body = EntityUtils.toString(response.getEntity());

	            log.info("API Response Status Code: {}", statusCode);
	            log.debug("API Response Body: {}", body);

	            //-----------------------------------
	            // TOKEN RETRY
	            //-----------------------------------
	            if (statusCode == 401 || statusCode == 403) {

	                log.warn("Token expired or unauthorized. Retrying with new token...");

	                invalidateToken();
	                token = getOAuthToken();

	                HttpPost retryPost = new HttpPost(apiUrl);
	                retryPost.setHeader("Authorization", "Bearer " + token);
	                retryPost.setHeader("Accept", "application/json");
	                retryPost.setHeader("Content-Type", "application/json");
	                retryPost.setEntity(new StringEntity(json.toString(), ContentType.APPLICATION_JSON));

	                try (CloseableHttpResponse retryResponse =
	                             client.execute(retryPost)) {

	                    statusCode = retryResponse.getStatusLine().getStatusCode();
	                    body = EntityUtils.toString(retryResponse.getEntity());

	                    log.info("Retry Response Status Code: {}", statusCode);
	                    log.debug("Retry Response Body: {}", body);
	                }
	            }
	        }

	        //-----------------------------------
	        // PARSE RESPONSE
	        //-----------------------------------
	        CreditCardResponse responseDto =
	                mapper.readValue(body, CreditCardResponse.class);

	        responseDto.setToken(token);
	        responseDto.setUrl(apiUrl);
	        responseDto.setHttpStatus(statusCode);

	        if ("0000".equals(responseDto.getResponseCode())) {
	            status = "SUCCESS";
	        }

	        long endTime = System.currentTimeMillis();
	        log.info("Execution Time: {} ms", (endTime - startTime));
	        log.info("CreditCardList API Completed with Status: {}", status);

	        return responseDto;

	    } catch (Exception ex) {

	        body = ex.getMessage();
	        log.error("Exception occurred in CreditCardList API", ex);
	        throw ex;

	    } finally {

	        Timestamp endTimestamp =
	                new Timestamp(System.currentTimeMillis());

	        //-----------------------------------
	        // INSERT TRANSACTION
	        //-----------------------------------
	        try {

	            if (transactionDao != null) {

	                boolean inserted =
	                        transactionDao.insertTransaction(
	                                ucid,
	                                referenceNumber,
	                                startTimestamp,
	                                endTimestamp,
	                                "CREDIT_CARD_LIST",
	                                apiUrl,
	                                json.toString(),
	                                body,
	                                status,
	                                statusCode,
	                                getServerIp()
	                        );

	                if (inserted) {
	                    log.info("CreditCardList Transaction Stored Successfully: {}", referenceNumber);
	                } else {
	                    log.error("CreditCardList Transaction NOT Stored");
	                }
	            }

	        } catch (Exception logEx) {

	            // NEVER break API because logging failed
	            log.error("Error while inserting CreditCardList transaction log", logEx);
	        }

	        log.info("========== CreditCardList API END ==========");
	    }
	}

	// ------------------------ ValidateTPIN ------------------------
	public static TpinResponse ValidateTPIN(
	        String isEncrypted,
	        String TPIN,
	        String ucid,
	        String RelationShipID,
	        String requesttime) throws Exception {

	    String referenceNumber = UUID.randomUUID().toString();
	    String apiUrl = requireProperty("TPIN_AUTHENTICATION");

	    log.info("========== ValidateTPIN API START ==========");
	    log.info("ReferenceNumber: {}", referenceNumber);
	    log.info("UCID: {}", ucid);
	    log.info("RelationshipID: {}", RelationShipID);
	    log.info("IsEncrypted: {}", isEncrypted);
	    log.info("API URL: {}", apiUrl);

	    CloseableHttpClient client = Load.getClient();
	    if (client == null) {
	        createSecureHttpClient();
	        client = Load.getClient();
	        log.debug("Secure HttpClient Created");
	    }

	    //-----------------------------------
	    // Build JSON
	    //-----------------------------------
	    JSONObject json = new JSONObject();
	    json.put("relationshipID", RelationShipID);
	    json.put("tpin", TPIN); // ⚠️ Sensitive, avoid logging
	    json.put("ucid", ucid);
	    json.put("isEncrypted", isEncrypted);
	    json.put("requestTime", requesttime);

	    String token = getOAuthToken();

	    log.debug("OAuth Token Generated Successfully");
	    log.debug("Request Payload Prepared (TPIN masked)");

	    String body = "";
	    int statusCode = 500;
	    String status = "FAILED";

	    Timestamp startTimestamp =
	            new Timestamp(System.currentTimeMillis());
	    long startTime = System.currentTimeMillis();

	    try {

	        HttpPost post = new HttpPost(apiUrl);
	        post.setHeader("Authorization", "Bearer " + token);
	        post.setHeader("Accept", "application/json");
	        post.setHeader("Content-Type", "application/json");
	        post.setEntity(new StringEntity(json.toString(), ContentType.APPLICATION_JSON));

	        log.info("Calling ValidateTPIN API...");

	        //-----------------------------------
	        // FIRST CALL
	        //-----------------------------------
	        try (CloseableHttpResponse response = client.execute(post)) {

	            statusCode = response.getStatusLine().getStatusCode();
	            body = EntityUtils.toString(response.getEntity());

	            log.info("API Response Status Code: {}", statusCode);
	            log.debug("API Response Body: {}", body);

	            //-----------------------------------
	            // TOKEN RETRY
	            //-----------------------------------
	            if (statusCode == 401 || statusCode == 403) {

	                log.warn("Token expired or unauthorized. Retrying with new token...");

	                invalidateToken();
	                token = getOAuthToken();

	                HttpPost retryPost = new HttpPost(apiUrl);
	                retryPost.setHeader("Authorization", "Bearer " + token);
	                retryPost.setHeader("Accept", "application/json");
	                retryPost.setHeader("Content-Type", "application/json");
	                retryPost.setEntity(new StringEntity(json.toString(), ContentType.APPLICATION_JSON));

	                try (CloseableHttpResponse retryResponse =
	                             client.execute(retryPost)) {

	                    statusCode = retryResponse.getStatusLine().getStatusCode();
	                    body = EntityUtils.toString(retryResponse.getEntity());

	                    log.info("Retry Response Status Code: {}", statusCode);
	                    log.debug("Retry Response Body: {}", body);
	                }
	            }
	        }

	        //-----------------------------------
	        // PARSE RESPONSE
	        //-----------------------------------
	        TpinResponse responseDto =
	                mapper.readValue(body, TpinResponse.class);

	        responseDto.setToken(token);
	        responseDto.setUrl(apiUrl);
	        responseDto.setHttpStatus(statusCode);

	        if ("0000".equals(responseDto.getResponseCode())) {
	            status = "SUCCESS";
	        }

	        long endTime = System.currentTimeMillis();
	        log.info("Execution Time: {} ms", (endTime - startTime));
	        log.info("ValidateTPIN API Completed with Status: {}", status);

	        return responseDto;

	    } catch (Exception ex) {

	        body = ex.getMessage();
	        log.error("Exception occurred in ValidateTPIN API", ex);
	        throw ex;

	    } finally {

	        Timestamp endTimestamp =
	                new Timestamp(System.currentTimeMillis());

	        //-----------------------------------
	        // INSERT TRANSACTION
	        //-----------------------------------
	        try {

	            if (transactionDao != null) {

	                boolean inserted =
	                        transactionDao.insertTransaction(
	                                ucid,
	                                referenceNumber,
	                                startTimestamp,
	                                endTimestamp,
	                                "VALIDATE_TPIN",
	                                apiUrl,
	                                json.toString(),
	                                body,
	                                status,
	                                statusCode,
	                                getServerIp()
	                        );

	                if (inserted) {
	                    log.info("✅ TPIN Transaction Stored Successfully: {}", referenceNumber);
	                } else {
	                    log.error("❌ TPIN Transaction NOT Stored");
	                }
	            }

	        } catch (Exception logEx) {

	            // NEVER break API because logging failed
	            log.error("Error while inserting TPIN transaction log", logEx);
	        }

	        log.info("========== ValidateTPIN API END ==========");
	    }
	}


	// ------------------------ NRMNidentificationAPI ------------------------
	public static NrmnValidationResponse NRMNidentificationAPI(String ucid, String AccNum, String AccType, String requesttime)
	        throws Exception {

	    String referenceNumber = UUID.randomUUID().toString();
	    String apiUrl = requireProperty("NRMN_IDENTIFICATION");

	    log.info("========== NRMNidentificationAPI START ==========");
	    log.info("ReferenceNumber: {}", referenceNumber);
	    log.info("UCID: {}", ucid);
	    log.info("AccNum: ****{}", AccNum != null && AccNum.length() >= 4 ? AccNum.substring(AccNum.length() - 4) : AccNum);
	    log.info("AccType: {}", AccType);
	    log.info("API URL: {}", apiUrl);

	    CloseableHttpClient client = Load.getClient();
	    if (client == null) {
	        createSecureHttpClient();
	        client = Load.getClient();
	        log.debug("Secure HttpClient Created");
	    }

	    String requestId = UUID.randomUUID().toString();

	    JSONObject json = new JSONObject();
	    json.put("accNum", AccNum);
	    json.put("accType", AccType);
	    json.put("ucid", ucid);
	    json.put("requestTime", requesttime);

	    String token = getOAuthToken();
	    log.debug("OAuth Token Generated Successfully");
	    log.debug("Request Payload Prepared (Account Number masked)");

	    String body = "";
	    int statusCode = 500;
	    String status = "FAILED";
	    Timestamp startTimestamp = new Timestamp(System.currentTimeMillis());
	    long startTime = System.currentTimeMillis();

	    try {
	        HttpPost post = new HttpPost(apiUrl);
	        post.setHeader("Authorization", "Bearer " + token);
	        post.setHeader("Accept", "application/json");
	        post.setHeader("Content-Type", "application/json");
	        post.setEntity(new StringEntity(json.toString(), ContentType.APPLICATION_JSON));

	        log.info("Calling NRMNidentification API...");

	        try (CloseableHttpResponse response = client.execute(post)) {
	            statusCode = response.getStatusLine().getStatusCode();
	            body = EntityUtils.toString(response.getEntity());

	            log.info("API Response Status Code: {}", statusCode);
	            log.debug("API Response Body: {}", body);

	            // Retry if token expired
	            if (statusCode == 401 || statusCode == 403) {
	                log.warn("Token expired or unauthorized. Retrying with new token...");

	                invalidateToken();
	                token = getOAuthToken();
	                post.setHeader("Authorization", "Bearer " + token);

	                try (CloseableHttpResponse retryResponse = client.execute(post)) {
	                    statusCode = retryResponse.getStatusLine().getStatusCode();
	                    body = EntityUtils.toString(retryResponse.getEntity());
	                    log.info("Retry Response Status Code: {}", statusCode);
	                    log.debug("Retry Response Body: {}", body);
	                }
	            }
	        }

	        NrmnValidationResponse responseDto = mapper.readValue(body, NrmnValidationResponse.class);
	        responseDto.setToken(token);
	        responseDto.setUrl(apiUrl);
	        responseDto.setHttpStatus(statusCode);

	        log.info("HasTPIN: {}", responseDto.getData().getHasTPIN());
	        log.info("IsRMN: {}", responseDto.getData().getIsRMN());

	        if ("0000".equals(responseDto.getResponseCode())) {
	            status = "SUCCESS";
	        }

	        long endTime = System.currentTimeMillis();
	        log.info("Execution Time: {} ms", (endTime - startTime));
	        log.info("NRMNidentificationAPI Completed with Status: {}", status);

	        return responseDto;

	    } finally {
	        Timestamp endTimestamp = new Timestamp(System.currentTimeMillis());

	        //-----------------------------------
	        // INSERT TRANSACTION
	        //-----------------------------------
	        try {
	            if (transactionDao != null) {
	                boolean inserted = transactionDao.insertTransaction(
	                        ucid,
	                        referenceNumber,
	                        startTimestamp,
	                        endTimestamp,
	                        "NRMN_IDENTIFICATION",
	                        apiUrl,
	                        json.toString(),
	                        body,
	                        status,
	                        statusCode,
	                        getServerIp()
	                );
	                if (inserted) {
	                    log.info("✅ NRMN Transaction Stored Successfully: {}", referenceNumber);
	                } else {
	                    log.error("❌ NRMN Transaction NOT Stored");
	                }
	            }
	        } catch (Exception logEx) {
	            log.error("Error while inserting NRMN transaction log", logEx); // never break API due to logging
	        }

	        log.info("========== NRMNidentificationAPI END ==========");
	    }
	}

	


	// ------------------------ GenerateandChangeTPIN ------------------------
	public static TpinResponse GenerateandChangeTPIN(String RelationshipID, String action, String tpin,
	        String isEncrypted, String ucid, String requesttime) throws Exception {

		 String referenceNumber = UUID.randomUUID().toString();
	    String apiUrl = requireProperty("TPIN");

	    CloseableHttpClient client = Load.getClient();
	    if (client == null) {
	        createSecureHttpClient();
	        client = Load.getClient();
	    }

	    String requestId = UUID.randomUUID().toString();

	    JSONObject json = new JSONObject();
	    json.put("relationshipID", RelationshipID);
	    json.put("action", action);
	    json.put("tpin", tpin);
	    json.put("isEncrypted", isEncrypted);
	    json.put("ucid", ucid);
	    json.put("requestTime", requesttime);

	    String token = getOAuthToken();
	    String body = "";
	    int statusCode = 500;
	    String status = "FAILED";
	    Timestamp startTimestamp = new Timestamp(System.currentTimeMillis());

	    try {
	        HttpPost post = new HttpPost(apiUrl);
	        post.setHeader("Authorization", "Bearer " + token);
	        post.setHeader("Accept", "application/json");
	        post.setHeader("Content-Type", "application/json");
	        post.setEntity(new StringEntity(json.toString(), ContentType.APPLICATION_JSON));

	        try (CloseableHttpResponse response = client.execute(post)) {
	            statusCode = response.getStatusLine().getStatusCode();
	            body = EntityUtils.toString(response.getEntity());

	            // Retry if token expired
	            if (statusCode == 401 || statusCode == 403) {
	                invalidateToken();
	                token = getOAuthToken();
	                post.setHeader("Authorization", "Bearer " + token);
	                try (CloseableHttpResponse retryResponse = client.execute(post)) {
	                    statusCode = retryResponse.getStatusLine().getStatusCode();
	                    body = EntityUtils.toString(retryResponse.getEntity());
	                }
	            }
	        }

	        TpinResponse responseDto = mapper.readValue(body, TpinResponse.class);
	        responseDto.setToken(token);
	        responseDto.setUrl(apiUrl);
	        responseDto.setHttpStatus(statusCode);

	        if ("0000".equals(responseDto.getResponseCode())) {
	            status = "SUCCESS";
	        }

	        return responseDto;

	    } finally {
	        Timestamp endTimestamp = new Timestamp(System.currentTimeMillis());
	        
	        try {
	            if (transactionDao != null) {
	                boolean inserted = transactionDao.insertTransaction(
	                        ucid,
	                        referenceNumber,
	                        startTimestamp,
	                        endTimestamp,
	                        "GENERATE_CHANGE_TPIN",
	                        apiUrl,
	                        json.toString(),
	                       body,
	                        status,
	                        statusCode,
	                        getServerIp()
	                );
	                if (inserted) {
	                    System.out.println("✅ Generate/Change TPIN Transaction Stored: " + referenceNumber);
	                } else {
	                    System.out.println("❌ Generate/Change TPIN Transaction NOT Stored");
	                }
	            }
	        } catch (Exception logEx) {
	            logEx.printStackTrace();
	        }
	    }
	}


	public static void main(String[] args) {

		try {
			// Step 1: Create a secure HttpClient
			

			// Step 3: Call APIs using the token
		
//
		SetPreferredLanguageResponse setPreferredLanguageResponse = SetPreferredLanguageAPI("00012345678",
				"REL000001", "EN", "2026-01-14T10:35:00Z");
		System.out.println("Prefered Response: " + setPreferredLanguageResponse);
////
		AccountListResponse accListResponse = AccountList("00012345678", "REL000001", "2026-01-14T10:35:00Z");
//			System.out.println("Account List Response: " + accListResponse);
//
	List<AccountListResponse.Account> accounts = accListResponse.getData().getAccounts();
	for (AccountListResponse.Account acc : accounts) {
			System.out.println("Account Number: " + acc.getAccountNumber());
		}
//
//			GenerateOTPResponse otpResponse = GenerateOTP("00012345678", "REL000001", "9998887777", "SMS",
//					"2026-02-11T10:00:00Z");
//			System.out.println("OTP Reference: " + otpResponse.getOtpReference());
//			System.out.println("Message: " + otpResponse.getMessage());
//			System.out.println("Expiry: " + otpResponse.getExpiry());
////
////			// Example: Validate OTP
			ValidateOTPResponse otpResp = ValidateOTP("00012345678", "REL000001", "OTPREF001", "N", "9998887777",
					"123456", "2026-02-11T10:00:00Z");

			System.out.println("Response Code: " + otpResp.getResponseCode());
			System.out.println("Message: " + otpResp.getMessage());
////
////			
			TpinResponse tpinResp = tpinResp = ValidateTPIN("Y", "1234", "00012345678", "REL000002", "2026-02-11T10:00:00Z");
            System.out.println("TPIN Response Code: " + tpinResp.getResponseCode());
       		System.out.println("TPIN Message: " + tpinResp.getMessage());
//
//			
//			// Example: Credit Card List
		//CreditCardResponse ccResp = CreditCardList("00012345678", "REL000002", "2026-02-11T10:00:00Z");
//////
//			System.out.println("Response Code: " + ccResp.getResponseCode());
//		   System.out.println("Message: " + ccResp.getMessage());
////
//			for (CustomerCardDetails card : ccResp.getCreditCards()) {
//				System.out.println("Card Number: " + card.getCreditCardNumber());
//				System.out.println("Card Type: " + card.getCreditCardType());
//			}
////
////			// Example: Validate TPIN
			TpinResponse tpinResponse = ValidateTPIN("N", "1234", "00012345678", "REL000001", "2026-01-14T10:35:00Z");
			System.out.println("TPIN Response Code: " + tpinResponse.getResponseCode());
			System.out.println("TPIN Message: " + tpinResponse.getMessage());
//
//			
			NrmnValidationResponse response = NRMNidentificationAPI("00012345678", "12345678901", "ACCNO", "2026-02-11T10:00:00Z");
//
			System.out.println("Response Code: " + response);
			System.out.println("Message: " + response.getMessage());
			System.out.println("Mobile Number: " + response.getData().getMobileNumber());
 System.out.println("HasTpin : " + response.getData().getHasTPIN());
//			
//			for (NrmnValidationResponse.Product product : response.getData().getProducts()) {
//			    System.out.println("Product Number: " + product.getNumber());
//			    System.out.println("Product Type: " + product.getType());
//			    System.out.println("Account Type: " + product.getAccountType());
//			}
//			System.out.println("RMN Response: ");
//			
//			
//			IdentifyCustomerResponse rmnResponse = RMNIdentificationAPI("00012345678", "9998887778",
//					"2026-01-14T10:35:00Z");
//			System.out.println("RMN Response: " + rmnResponse);
////			
//			TpinResponse genChangeTpinResponse = GenerateandChangeTPIN("REL000001", "CHANGE", "8090", "N",
//					"12789011", "2026-01-14T10:35:00Z");
//			System.out.println("Generate/Change TPIN Response: " + genChangeTpinResponse);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}