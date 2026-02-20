package newcloud.Test;

import newcloud.executedata.*;

import java.util.List;

/**
 * Tests state aggregation convergence behavior by comparing
 * standard Q-Learning with Q-Learning without state aggregation.
 */
public class StateConverge {
    public static void main(String[] args) throws Exception {
        System.out.println("=== State Convergence Test ===");

        LearningScheduleTest learningTest = new LearningScheduleTest();
        List<Double> learningPower = learningTest.execute();
        System.out.println("Q-Learning (with aggregation): " + learningPower.size() + " data points");

        LearningAndNoConvergeScheduleTest noConvergeTest = new LearningAndNoConvergeScheduleTest();
        List<Double> noConvergePower = noConvergeTest.execute();
        System.out.println("Q-Learning (without aggregation): " + noConvergePower.size() + " data points");

        // Print comparative results
        System.out.println("\n=== Results ===");
        for (int i = 0; i < Math.min(learningPower.size(), noConvergePower.size()); i++) {
            System.out.printf("Step %d: aggregated=%.2f, non-aggregated=%.2f%n",
                    i, learningPower.get(i), noConvergePower.get(i));
        }
    }
}
