package DTO;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;

public class DBConnectionPool {

    private static HikariDataSource dataSource;

    static {

        HikariConfig config = new HikariConfig();

        config.setDriverClassName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        config.setJdbcUrl(
        	    "jdbc:sqlserver://192.168.168.12:1433;" +
        	    "databaseName=_Deepak2025;" +
        	    "encrypt=true;" +
        	    "trustServerCertificate=true;"
        	);
        config.setUsername("sa");
        config.setPassword("P@ssw0rd");

        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(15000);
        config.setIdleTimeout(60000);
        config.setMaxLifetime(1800000);

        dataSource = new HikariDataSource(config);

        System.out.println("✅ Connection Pool Created");
    }

    public static DataSource getDataSource() {
        return dataSource;
    }
}

