package com.mycompany.mathgame.leaderboard;

import java.util.List;

public interface LeaderboardPort {
    /** Tải danh sách leaderboard (top N) */
    List<LeaderboardEntry> load();

    /** Thêm một phiên chơi (ghi điểm) */
    void add(String playerName, int totalScore, long tookMillis, int maxStreak);

    /** Xóa toàn bộ phiên chơi (giữ nguyên danh sách người chơi) */
    void clearAll();

    /** Xóa tất cả phiên chơi của một người chơi theo tên */
    void deleteByPlayer(String playerName);
}
