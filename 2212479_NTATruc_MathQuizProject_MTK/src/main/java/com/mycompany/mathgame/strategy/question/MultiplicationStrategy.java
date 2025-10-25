package com.mycompany.mathgame.strategy.question;
import com.mycompany.mathgame.core.Question;
import java.util.Random;
public class MultiplicationStrategy implements QuestionStrategy {
    @Override public Question next(Random rnd, int level) {
        int a = rnd.nextInt(5 * level) + 1;
        int b = rnd.nextInt(5 * level) + 1;
        return new Question(a + " x " + b + " = ?", a * b);
    }
}
