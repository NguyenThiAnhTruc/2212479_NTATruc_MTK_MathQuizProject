package com.mycompany.mathgame.strategy.difficulty;
import com.mycompany.mathgame.core.GameState;
public interface DifficultyStrategy {
    int levelFor(GameState state, int currentLevel, int totalScore);
}
