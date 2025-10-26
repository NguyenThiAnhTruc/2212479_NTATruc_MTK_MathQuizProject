package com.mycompany.mathgame.strategy.score;
import com.mycompany.mathgame.core.GameState;
public class StreakBonusDecorator implements ScoreStrategy {
    private final ScoreStrategy inner;// chiến lược gốc được “bọc”
    private final int every;// // số câu cần đạt để nhận thưởng
    private final int bonus;// điểm thưởng thêm
    public StreakBonusDecorator(ScoreStrategy inner, int every, int bonus){
        this.inner = inner; this.every = every; this.bonus = bonus;
    }
    @Override public int score(boolean correct, long elapsed, int level, GameState s){
        int base = inner.score(correct, elapsed, level, s);
        if (correct && s.streak() > 0 && s.streak() % every == 0) base += bonus;
        return base;
    }
}
