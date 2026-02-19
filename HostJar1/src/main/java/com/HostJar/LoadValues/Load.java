package com.HostJar.LoadValues;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Properties;
import java.util.UUID;

import javax.sql.DataSource;

import org.apache.http.impl.client.CloseableHttpClient;
import org.json.JSONObject;

import DTO.DBConnectionPool;


public class Load {

	
	 DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
             .withZone(ZoneId.systemDefault());
	  private DataSource dataSource;
	  private final Properties config = new Properties();
    private volatile long configLastModified = -1;
    private volatile long configReloadTime = -1; // Stores when config was reloaded

    // JSON tracking
    private volatile long menuJsonLastModified = -1;
    private volatile long jsonReloadTime = -1;   
    private volatile JSONObject menuJson;

    // HTTP client
    private static CloseableHttpClient client;

    // Singleton instance
    private static final Load INSTANCE = new Load();

    // Shared Properties object
    public static final Properties CONFIG = new Properties();

  
    public static CloseableHttpClient getClient() {
        return client;
    }

    public static void setClient(CloseableHttpClient httpClient) {
        client = httpClient;
    }

    
    private Load() {}

    public static Load getInstance() {
        return INSTANCE;
    }

    // =====================================================
    // ✅ LOAD CONFIG (AUTO RELOAD WHEN FILE CHANGES)
    // =====================================================

    public synchronized void loadConfig(String configFilePath) {

        try {

            Path path = Paths.get(configFilePath);

            if (!Files.exists(path)) {
                throw new RuntimeException("CONFIG FILE NOT FOUND: " + path);
            }

            long currentModified =
                    Files.getLastModifiedTime(path).toMillis();

            // ✅ Skip reload if file not changed
            if (currentModified == configLastModified) {
                return;
            }

            try (InputStream in = new FileInputStream(configFilePath)) {

                config.clear();
                config.load(in);
            }

            configLastModified = currentModified;

            System.out.println("✅ CONFIG RELOADED at file time: "
                    + timeFormatter.format(
                    Instant.ofEpochMilli(configLastModified)));

            // 🔥 Debug once (remove later if needed)
            System.out.println("Loaded Keys -> " + config.keySet());

        } catch (Exception e) {

            throw new RuntimeException("CONFIG LOAD FAILED", e);
        }
    }

    // =====================================================
    // ✅ SAFE PROPERTY GETTERS
    // =====================================================

    public String getProperty(String key) {

        // 🔥 AUTO LOAD if config is empty
        if (config.isEmpty()) {

            synchronized (this) {

                if (config.isEmpty()) {

                    try {

                        String defaultPath =
                                "D:/Banking_Project/Config/Config.properties";

                        System.out.println(
                            "⚠️ Config was EMPTY. Auto loading...");

                        loadConfig(defaultPath);

                    } catch (Exception e) {

                        throw new RuntimeException(
                            "CONFIG NOT LOADED! Cannot read key: " + key, e);
                    }
                }
            }
        }

        String value = config.getProperty(key);

        if (value == null || value.trim().isEmpty()) {
            throw new RuntimeException("Missing config key: " + key);
        }

        return value.trim();
    }


    // ⭐ Recommended getter (prevents IVR crash)
    public String getProperty(String key, String defaultValue) {

        String value = config.getProperty(key);

        return (value == null || value.trim().isEmpty())
                ? defaultValue
                : value.trim();
    }

    public long getLong(String key, long defaultValue) {

        try {
            return Long.parseLong(
                    config.getProperty(key, String.valueOf(defaultValue)));
        }
        catch (Exception e) {
            return defaultValue;
        }
    }

    // =====================================================
    // ✅ LOAD MENU JSON (AUTO RELOAD)
    // =====================================================

    public JSONObject loadMenuJson() {

        try {

            String jsonPathStr = getProperty("MENU_DETAILS");

            Path jsonPath = Paths.get(jsonPathStr);

            if (!Files.exists(jsonPath)) {
                throw new RuntimeException(
                        "Menu JSON file NOT FOUND: " + jsonPathStr);
            }

            long currentModified =
                    Files.getLastModifiedTime(jsonPath).toMillis();

            if (menuJson != null &&
                    currentModified == menuJsonLastModified) {

                return menuJson;
            }

            synchronized (this) {

                if (menuJson != null &&
                        currentModified == menuJsonLastModified) {

                    return menuJson;
                }

                byte[] bytes = Files.readAllBytes(jsonPath);

                String jsonContent =
                        new String(bytes, StandardCharsets.UTF_8);

                menuJson = new JSONObject(jsonContent);

                menuJsonLastModified = currentModified;

                System.out.println("✅ MENU JSON RELOADED at file time: "
                        + timeFormatter.format(
                        Instant.ofEpochMilli(menuJsonLastModified)));
            }

            return menuJson;

        } catch (Exception e) {

            throw new RuntimeException("FAILED TO LOAD MENU JSON", e);
        }
    }

    
 
//
//    private void initDataSource() {
//
//        try {
//
//            String driver = getProperty("DB.DRIVERCLASSNAME");
//            String url    = getProperty("URL");
//            String user   = getProperty("USERNAME");
//            String pass   = getProperty("PASSWORD");
//
//            // 🔥 DEBUG (remove later)
//            System.out.println("Driver : " + driver);
//            System.out.println("URL    : " + url);
//            System.out.println("User   : " + user);
//
//            Class.forName(driver);
//
//            HikariConfig hikari = new HikariConfig();
//
//            hikari.setDriverClassName(driver);
//            hikari.setJdbcUrl(url);
//            hikari.setUsername(user);
//            hikari.setPassword(pass);
//
//            hikari.setMaximumPoolSize(
//                    Integer.parseInt(getProperty("DB_POOL_SIZE", "10")));
//
//            hikari.setMinimumIdle(2);
//            hikari.setConnectionTimeout(15000);
//            hikari.setIdleTimeout(60000);
//            hikari.setMaxLifetime(1800000);
//
//            this.dataSource = new HikariDataSource(hikari);
//
//            System.out.println("✅ SQL Server Pool Initialized");
//
//        } catch (Exception e) {
//
//            e.printStackTrace(); // VERY IMPORTANT
//            throw new RuntimeException("DB POOL INIT FAILED", e);
//        }
//    }


    private static final String INSERT_SQL =
            "INSERT INTO Banking_Transaction_History (" +
                    "ucid, clid, dnis, reference_number, start_date, end_date, " +
                    "function_name, host_url, host_request, host_response, " +
                    "trans_status, http_code, server_ip) " +
                    "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";

    public static String insertTransaction(
            String ucid,
            String clid,
            String dnis,
            String functionName,
            String hostUrl,
            String hostRequest,
            String hostResponse,
            String transStatus,
            int httpCode,
            String serverIp) {

        String referenceNumber = UUID.randomUUID().toString();
        Timestamp now = new Timestamp(System.currentTimeMillis());

        try (Connection conn = DBConnectionPool.getDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT_SQL)) {

            ps.setString(1, ucid);
            ps.setString(2, clid);
            ps.setString(3, dnis);
            ps.setString(4, referenceNumber);
            ps.setTimestamp(5, now);
            ps.setTimestamp(6, now);
            ps.setString(7, functionName);
            ps.setString(8, hostUrl);
            ps.setString(9, hostRequest);
            ps.setString(10, hostResponse);
            ps.setString(11, transStatus);
            ps.setInt(12, httpCode);
            ps.setString(13, serverIp);

            ps.executeUpdate();

            System.out.println("✅ Transaction Inserted: " + referenceNumber);

        } catch (Exception e) {

            // ⭐ NEVER crash IVR call because DB failed
            System.err.println("Transaction Insert Failed: " + e.getMessage());
        }

        return referenceNumber;
    }

    
    public static void init(String configPath) {

        Load loader = getInstance();

        loader.loadConfig(configPath);

        System.out.println("Loaded CONFIG Keys -> " + loader.config.keySet());

      

        System.out.println("🚀 APPLICATION CONFIG INITIALIZED");
    }


  
    public JSONObject loadVDNJson() {
    	 
        try {
 
            String jsonPathStr = getProperty("VDN_LIST");
 
            Path jsonPath = Paths.get(jsonPathStr);
 
            if (!Files.exists(jsonPath)) {
                throw new RuntimeException(
                        "Menu JSON file NOT FOUND: " + jsonPathStr);
            }
 
            long currentModified =
                    Files.getLastModifiedTime(jsonPath).toMillis();
 
            if (menuJson != null &&
                    currentModified == menuJsonLastModified) {
 
                return menuJson;
            }
 
            synchronized (this) {
 
                if (menuJson != null &&
                        currentModified == menuJsonLastModified) {
 
                    return menuJson;
                }
 
                byte[] bytes = Files.readAllBytes(jsonPath);
 
                String jsonContent =
                        new String(bytes, StandardCharsets.UTF_8);
 
                menuJson = new JSONObject(jsonContent);
 
                menuJsonLastModified = currentModified;
 
                System.out.println("✅ MENU JSON RELOADED at file time: "
                        + timeFormatter.format(
                        Instant.ofEpochMilli(menuJsonLastModified)));
            }
 
            return menuJson;
 
        } catch (Exception e) {
 
            throw new RuntimeException("FAILED TO LOAD VND", e);
        }
    }

 
   public static void main(String[] args) {

       String configPath =
//                "D:/Banking_Project/Config/Config.properties";
//
//        

      // Load.getInstance().loadConfig(configPath);
//
        
//
    //  System.out.println("MENU_DETAILS -> " + menuPath);
//
//        JSONObject menu = loader.loadMenuJson();

      //  System.out.println(menu.toString(4));
       
        Load.insertTransaction(
                "UCID1001",
                "9876543210",
                "1800123456",
                "BALANCE_CHECK",
                "https://host/balance",
                "{\"acc\":\"1234\"}",
                "{\"bal\":\"5000\"}",
                "SUCCESS",
                200,
                "10.10.1.25"
        );
        
    }
}

    



