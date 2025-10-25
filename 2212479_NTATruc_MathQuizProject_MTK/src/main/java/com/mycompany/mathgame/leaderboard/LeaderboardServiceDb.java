package com.mycompany.mathgame.leaderboard;

import com.mycompany.mathgame.db.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LeaderboardServiceDb implements LeaderboardPort {

    private final int keepTop;

    /** Lấy TOP n kết quả từ view (mặc định 1000) */
    public LeaderboardServiceDb() {
        this(1000);
    }

    public LeaderboardServiceDb(int keepTop) {
        this.keepTop = keepTop;
    }

    /**
     * Đọc leaderboard từ view:
     *  SELECT TOP keepTop PlayerName, TotalScore, TotalTimeMs, MaxStreak
     *  ORDER BY TotalScore DESC, TotalTimeMs ASC, SessionId DESC
     */
    @Override
    public synchronized List<LeaderboardEntry> load() {
        List<LeaderboardEntry> out = new ArrayList<>();
        String sql = """
            SELECT TOP (?)
                   PlayerName,
                   TotalScore,
                   TotalTimeMs,
                   MaxStreak
            FROM quiz.v_Leaderboard_AllTime
            ORDER BY TotalScore DESC, TotalTimeMs ASC, SessionId DESC
            """;
        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, keepTop);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String name   = rs.getNString("PlayerName");
                    int score     = rs.getInt("TotalScore");
                    long ms       = rs.getLong("TotalTimeMs");
                    int maxStreak = rs.getInt("MaxStreak");
                    out.add(new LeaderboardEntry(name, score, ms, maxStreak));
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return out;
    }

    /**
     * Thêm 1 phiên chơi kết thúc (session) vào DB:
     *  1) Upsert người chơi → lấy PlayerId
     *  2) Insert vào quiz.GameSession (điểm, thời gian). MaxStreak để 0 (nếu cần có thể sửa giao diện để truyền vào).
     */
    // com.mycompany.mathgame.leaderboard.LeaderboardServiceDb
    @Override
    public synchronized void add(String player, int score, long tookMillis, int maxStreak) {
        try (Connection cn = DatabaseConnection.getConnection()) {
            cn.setAutoCommit(false);
            try {
                int playerId = upsertPlayer(cn, player);

                try (PreparedStatement ps = cn.prepareStatement(
                    "INSERT INTO quiz.GameSession " +
                    "(PlayerId, StartedAt, EndedAt, TotalScore, TotalTimeMs, MaxStreak) " +
                    "VALUES (?, SYSUTCDATETIME(), SYSUTCDATETIME(), ?, ?, ?)")) {
                    ps.setInt(1, playerId);
                    ps.setInt(2, score);
                    ps.setLong(3, tookMillis);
                    ps.setInt(4, maxStreak);             // <-- dùng maxStreak người chơi đạt được
                    ps.executeUpdate();
                }

                cn.commit();
            } catch (Exception e) {
                cn.rollback();
                throw e;
            } finally {
                cn.setAutoCommit(true);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
}


    /** Xoá toàn bộ session/attempt (không xoá Players) bằng stored procedure */
    @Override
    public synchronized void clearAll() {
        String call = "{call quiz.sp_ClearLeaderboard}";
        try (Connection cn = DatabaseConnection.getConnection();
             CallableStatement cs = cn.prepareCall(call)) {
            cs.execute();
            System.out.println("🧹 Cleared all leaderboard data via sp_ClearLeaderboard.");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /** Xoá toàn bộ phiên chơi của một người chơi theo tên (gọi stored procedure) */
    @Override
    public synchronized void deleteByPlayer(String playerName) {
        String call = "{call quiz.sp_DeleteSessionsByPlayerName(?)}";
        try (Connection cn = DatabaseConnection.getConnection();
             CallableStatement cs = cn.prepareCall(call)) {
            cs.setNString(1, playerName);
            cs.execute();
            System.out.println("🗑 Deleted sessions of player: " + playerName);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /** Gọi SP để upsert & lấy PlayerId theo tên */
    private int upsertPlayer(Connection cn, String playerName) throws SQLException {
        try (CallableStatement cs = cn.prepareCall("{call quiz.sp_UpsertPlayer(?, ?)}")) {
            cs.setNString(1, playerName);
            cs.registerOutParameter(2, Types.INTEGER);
            cs.execute();
            return cs.getInt(2);
        }
    }
}
