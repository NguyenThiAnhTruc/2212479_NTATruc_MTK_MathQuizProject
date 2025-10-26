package com.mycompany.mathgame.strategy.question;
import com.mycompany.mathgame.core.Question;
import java.util.Random;
public class AdditionStrategy implements QuestionStrategy {

    /**
     * Sinh câu hỏi toán cộng mới dựa trên cấp độ.
     *
     * @param rnd   Đối tượng Random dùng để tạo số ngẫu nhiên.
     * @param level Cấp độ hiện tại (ảnh hưởng đến độ lớn của số).
     * @return Đối tượng Question chứa đề bài và đáp án đúng.
     */
    @Override
    public Question next(Random rnd, int level) {
        // Sinh hai số ngẫu nhiên trong khoảng [1, 10 * level]
        int a = rnd.nextInt(10 * level) + 1;
        int b = rnd.nextInt(10 * level) + 1;

        // Tạo câu hỏi dạng “a + b = ?”
        return new Question(a + " + " + b + " = ?", a + b);
    }
}
