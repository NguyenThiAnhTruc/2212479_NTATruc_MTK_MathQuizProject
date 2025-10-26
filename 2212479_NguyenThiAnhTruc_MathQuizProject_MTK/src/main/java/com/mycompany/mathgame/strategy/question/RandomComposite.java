package com.mycompany.mathgame.strategy.question;
import com.mycompany.mathgame.core.Question;
import java.util.List; import java.util.Random;
public class RandomComposite implements QuestionStrategy {
    private final List<QuestionStrategy> strategies;
    public RandomComposite(List<QuestionStrategy> list){ this.strategies = list; }
    @Override public Question next(Random rnd, int level) {
        return strategies.get(rnd.nextInt(strategies.size())).next(rnd, level);
    }
}
