package com.mycompany.mathgame.strategy.score;
import com.mycompany.mathgame.core.GameState;
public class FixedScoreStrategy implements ScoreStrategy {
    private final int perCorrect, penalty;// điểm thưởng khi trả lời đúng, sai
    public FixedScoreStrategy(int perCorrect, int penalty){
        this.perCorrect = perCorrect;
        this.penalty = penalty;
    }
    @Override public int score(boolean correct, long elapsed, int level, GameState s){
        return correct ? perCorrect : -penalty;
    }
}
