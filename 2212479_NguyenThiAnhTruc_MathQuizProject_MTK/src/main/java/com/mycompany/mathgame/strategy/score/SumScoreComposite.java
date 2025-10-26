package com.mycompany.mathgame.strategy.score;
import com.mycompany.mathgame.core.GameState;
import java.util.List;
public class SumScoreComposite implements ScoreStrategy {
    private final List<ScoreStrategy> children;
    public SumScoreComposite(List<ScoreStrategy> children){ this.children = children; }
    @Override public int score(boolean correct, long elapsed, int level, GameState s) {
        int sum = 0; for (var c: children) sum += c.score(correct, elapsed, level, s); return sum;
    }
}
