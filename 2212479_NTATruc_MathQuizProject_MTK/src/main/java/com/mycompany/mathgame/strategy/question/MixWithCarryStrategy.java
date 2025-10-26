package com.mycompany.mathgame.strategy.question;
import com.mycompany.mathgame.core.Question;
import java.util.Random;
public class MixWithCarryStrategy implements QuestionStrategy {
    @Override public Question next(Random rnd, int level) {
        int a = rnd.nextInt(90 * level) + 10; // >= 2 chữ số
        int b = rnd.nextInt(90 * level) + 10;
        return new Question(a + " + " + b + " = ?", a + b);
    }
}
