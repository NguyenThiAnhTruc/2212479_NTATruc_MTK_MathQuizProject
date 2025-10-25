package com.mycompany.mathgame.strategy.score;
import com.mycompany.mathgame.core.GameState;
public interface ScoreStrategy {
    int score(boolean correct, long elapsedMillis, int level, GameState state);
}
