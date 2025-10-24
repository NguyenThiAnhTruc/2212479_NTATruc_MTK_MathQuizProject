package com.mycompany.mathgame.strategy.difficulty;
import com.mycompany.mathgame.core.GameState;
public class StreakBasedDifficulty implements DifficultyStrategy {

    // Cấp độ bắt đầu và cấp độ tối đa
    private final int baseLevel, maxLevel;

    /** Khởi tạo chiến lược độ khó dựa theo chuỗi đúng. */
    public StreakBasedDifficulty(int baseLevel, int maxLevel) {
        this.baseLevel = baseLevel;
        this.maxLevel = maxLevel;
    }
    @Override
    public int levelFor(GameState s, int currentLevel, int totalScore) {
        // Cứ mỗi 7 câu đúng liên tiếp thì tăng 1 cấp
        int bonus = s.streak() / 7;

        // Giới hạn trong phạm vi cho phép
        int target = Math.min(maxLevel, baseLevel + bonus);

        // Điều chỉnh cấp độ: tăng, giảm hoặc giữ nguyên
        if (target > currentLevel) return currentLevel + 1;
        if (target < currentLevel) return currentLevel - 1;
        return currentLevel;
    }
}
