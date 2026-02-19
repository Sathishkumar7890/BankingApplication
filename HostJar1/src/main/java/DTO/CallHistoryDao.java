package DTO;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import javax.sql.DataSource;

public class CallHistoryDao {

    private final DataSource dataSource;

    public CallHistoryDao() {
        this.dataSource = DBConnectionPool.getDataSource();
    }

    public boolean insertCallHistory(
            String ucid,
            String clid,
            String dnis,
            String language,
            Timestamp startDate,
            Timestamp endDate,
            int callDuration,
            String menuDescription,
            String exitLocation,
            String ip,
            String sessionId,
            boolean rmnFlag,
            String transferVDN,
            String dispositionType
    ) {

        // ✅ Now only 14 parameters
        String sql = "{call InsertCallHistory(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}";

        try (Connection conn = dataSource.getConnection();
             CallableStatement cs = conn.prepareCall(sql)) {

            cs.setString(1, ucid);
            cs.setString(2, clid);
            cs.setString(3, dnis);
            cs.setString(4, language);
            cs.setTimestamp(5, startDate);
            cs.setTimestamp(6, endDate);
            cs.setInt(7, callDuration);
            cs.setString(8, menuDescription);
            cs.setString(9, exitLocation); // ✅ FIXED (was missing)
            cs.setString(10, ip);
            cs.setString(11, sessionId);
            cs.setBoolean(12, rmnFlag);
            cs.setString(13, transferVDN);
            cs.setString(14, dispositionType);

            cs.execute();

            System.out.println("Call history inserted successfully via SP!");
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void main(String[] args) {

        CallHistoryDao dao = new CallHistoryDao();

        boolean inserted = dao.insertCallHistory(
                "UC12331332",
                "CL001",
                "9876543210",
                "EN",
                new Timestamp(System.currentTimeMillis()),
                new Timestamp(System.currentTimeMillis()),
                120,
                "Main Menu",
                "Exit",
                "192.168.0.1",
                "SESSION123",
                true,
                "VDN001",
                "Completed"
        );
    }
}


