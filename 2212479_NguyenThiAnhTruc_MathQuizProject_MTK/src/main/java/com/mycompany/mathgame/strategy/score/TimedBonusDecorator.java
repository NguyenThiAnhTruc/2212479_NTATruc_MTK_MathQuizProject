package com.mycompany.mathgame.strategy.score;
import com.mycompany.mathgame.core.GameState;
public class TimedBonusDecorator implements ScoreStrategy {
    private final ScoreStrategy inner;
    private final long fastMillis;// giới hạn thời gian tính thưởng (ms)
    private final int bonus;
    public TimedBonusDecorator(ScoreStrategy inner, long fastMillis, int bonus){
        this.inner = inner; this.fastMillis = fastMillis; this.bonus = bonus;
    }
    @Override public int score(boolean correct, long elapsed, int level, GameState s){
        int base = inner.score(correct, elapsed, level, s);
        if (correct && elapsed <= fastMillis) base += bonus;
        return base;
    }
}
