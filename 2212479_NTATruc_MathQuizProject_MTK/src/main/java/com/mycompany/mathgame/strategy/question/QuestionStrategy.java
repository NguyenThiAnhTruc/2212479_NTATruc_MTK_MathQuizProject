package com.mycompany.mathgame.strategy.question;
import com.mycompany.mathgame.core.Question;
import java.util.Random;
public interface QuestionStrategy { Question next(Random rnd, int level); }
