package DTO;



import java.net.InetAddress;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

import javax.sql.DataSource;

import org.springframework.stereotype.Repository;



@Repository
public class TransactionDao {

    private final DataSource dataSource;

    public TransactionDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    //---------------------------------------------------

    public boolean insertTransaction(
            String ucid,
            String referenceNumber,
            Timestamp startDate,
            Timestamp endDate,
            String functionName,
            String hostUrl,
            String hostRequest,
            String hostResponse,
            String transStatus,
            int httpCode,
            String serverIp) {

        String sql = "{call InsertTransactionHistory(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}";

        try (Connection conn = dataSource.getConnection();
             CallableStatement cs = conn.prepareCall(sql)) {

            cs.setString(1, ucid);
            cs.setString(2, referenceNumber);
            cs.setTimestamp(3, startDate);
            cs.setTimestamp(4, endDate);
            cs.setString(5, functionName);
            cs.setString(6, hostUrl);
            cs.setString(7, hostRequest);
            cs.setString(8, hostResponse);
            cs.setString(9, transStatus);
            cs.setInt(10, httpCode);
            cs.setString(11, serverIp);

            cs.execute(); // ✅ SP executed
            System.out.println("Transaction inserted successfully via SP!");
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }



    //---------------------------------------------------
    // SERVER IP (Banks always store this)
    //---------------------------------------------------

    private String getServerIp() {

        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            return "UNKNOWN";
        }
    
}

   
}
