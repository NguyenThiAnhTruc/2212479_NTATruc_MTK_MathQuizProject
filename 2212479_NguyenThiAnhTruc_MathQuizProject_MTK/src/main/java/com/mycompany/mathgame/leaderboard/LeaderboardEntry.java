package com.mycompany.mathgame.leaderboard;

public record LeaderboardEntry(String playerName, int score, long millis, int maxStreak)
        implements Comparable<LeaderboardEntry> {
    @Override public int compareTo(LeaderboardEntry o) {
        int byScore = Integer.compare(o.score, this.score);
        if (byScore != 0) return byScore;
        int byStreak = Integer.compare(o.maxStreak, this.maxStreak);
        if (byStreak != 0) return byStreak;
        return Long.compare(this.millis, o.millis);
    }
}