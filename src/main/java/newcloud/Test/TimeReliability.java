package newcloud.Test;

import newcloud.executedata.*;

import java.util.List;

/**
 * Evaluates temporal credit assignment reliability by comparing
 * Q-Learning with Q-Learning(Lambda).
 */
public class TimeReliability {
    public static void main(String[] args) throws Exception {
        System.out.println("=== Time Reliability Test ===");

        LearningScheduleTest learningTest = new LearningScheduleTest();
        List<Double> learningPower = learningTest.execute();
        System.out.println("Q-Learning: " + learningPower.size() + " data points");

        LearningLamdaScheduleTest lamdaTest = new LearningLamdaScheduleTest();
        List<Double> lamdaPower = lamdaTest.execute();
        System.out.println("Q-Learning(Lambda): " + lamdaPower.size() + " data points");

        System.out.println("\n=== Comparative Results ===");
        for (int i = 0; i < Math.min(learningPower.size(), lamdaPower.size()); i++) {
            System.out.printf("Step %d: Q-Learning=%.2f, Q-Learning(Lambda)=%.2f%n",
                    i, learningPower.get(i), lamdaPower.get(i));
        }
    }
}
