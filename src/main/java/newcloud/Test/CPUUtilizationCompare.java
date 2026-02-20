package newcloud.Test;

import newcloud.executedata.*;

import java.util.List;

/**
 * Compares CPU utilization patterns across different placement algorithms.
 * Results are printed to stdout.
 */
public class CPUUtilizationCompare {
    public static void main(String[] args) throws Exception {
        System.out.println("=== CPU Utilization Comparison ===");

        LearningScheduleTest learningTest = new LearningScheduleTest();
        List<Double> learningPower = learningTest.execute();
        List<Integer> result1 = learningTest.getNumByType();
        System.out.println("Q-Learning host types: " + result1);

        LearningLamdaScheduleTest lamdaTest = new LearningLamdaScheduleTest();
        List<Double> lamdaPower = lamdaTest.execute();
        System.out.println("Q-Learning(Lambda) complete");

        GreedyScheduleTest greedyTest = new GreedyScheduleTest();
        List<Double> greedyPower = greedyTest.execute();
        List<Integer> result3 = greedyTest.getNumByType();
        System.out.println("Greedy host types: " + result3);

        LearningAndInitScheduleTest initTest = new LearningAndInitScheduleTest();
        List<Double> initPower = initTest.execute();
        System.out.println("Q-Learning(Init) complete");
    }
}
