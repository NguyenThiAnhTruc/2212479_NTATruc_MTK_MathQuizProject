
package com.mycompany.mathgame.core;

public class GameState {
    private int streak = 0;
    private int maxStreak = 0;

    public void resetStreak(){ streak = 0; }

    public void incStreak(){
        streak++;
        if (streak > maxStreak) maxStreak = streak;
    }

    public int streak(){ return streak; }
    public int maxStreak(){ return maxStreak; }

    public void resetAll(){
        streak = 0;
        maxStreak = 0;
    }
}
