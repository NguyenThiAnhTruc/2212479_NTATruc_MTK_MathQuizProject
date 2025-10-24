package com.mycompany.mathgame.strategy.question;
import com.mycompany.mathgame.core.Question;
import java.util.Random;
public class DivisionStrategy implements QuestionStrategy {
    @Override public Question next(Random rnd, int level) {
        int b = rnd.nextInt(Math.max(2, 3 * level)) + 1; // mẫu số
        int a = b * (rnd.nextInt(5 * level) + 1) + rnd.nextInt(b); // chia có dư
        return new Question(a + " / " + b + " = ? (phan nguyen)", a / b);
    }
}
