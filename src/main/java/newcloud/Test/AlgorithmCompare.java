package newcloud.Test;

import newcloud.executedata.*;

import java.util.ArrayList;
import java.util.List;

import static newcloud.Constants.Iteration;

/**
 * Compares energy consumption across Q-Learning(Lambda), Q-Learning,
 * Greedy, and Q-Learning with initialization strategies.
 * <p>
 * Results are printed to stdout. MATLAB plotting has been removed;
 * use the raw data with any plotting tool (matplotlib, gnuplot, etc.).
 * </p>
 */
public class AlgorithmCompare {

    public static void main(String[] args) {
        try {
            System.out.println("=== Algorithm Comparison ===");
            System.out.println("Iterations: " + Iteration);

            LearningScheduleTest learningTest = new LearningScheduleTest();
            List<Double> learningPower = learningTest.execute();
            System.out.println("\nQ-Learning power data: " + learningPower.size() + " points");

            LearningLamdaScheduleTest lamdaTest = new LearningLamdaScheduleTest();
            List<Double> lamdaPower = lamdaTest.execute();
            System.out.println("\nQ-Learning(Lambda) power data: " + lamdaPower.size() + " points");

            GreedyScheduleTest greedyTest = new GreedyScheduleTest();
            List<Double> greedyPower = greedyTest.execute();
            System.out.println("\nGreedy power data: " + greedyPower.size() + " points");

            LearningAndInitScheduleTest initTest = new LearningAndInitScheduleTest();
            List<Double> initPower = initTest.execute();
            System.out.println("\nQ-Learning(Init) power data: " + initPower.size() + " points");

            System.out.println("\n=== Average Results ===");
            System.out.println("Q-Learning:        " + getAverageResult(learningPower, Math.max(1, learningPower.size() / 10)));
            System.out.println("Q-Learning(Lambda): " + getAverageResult(lamdaPower, Math.max(1, lamdaPower.size() / 10)));
            System.out.println("Greedy:            " + getAverageResult(greedyPower, Math.max(1, greedyPower.size() / 10)));
            System.out.println("Q-Learning(Init):  " + getAverageResult(initPower, Math.max(1, initPower.size() / 10)));

        } catch (Exception e) {
            System.out.println("Exception: " + e.toString());
            e.printStackTrace();
        }
    }

    /**
     * Compute windowed averages of the power data.
     */
    public static List<Double> getAverageResult(List<Double> datas, int step) {
        List<Double> temp = new ArrayList<>();
        List<Double> copy = new ArrayList<>(datas);
        int num = copy.size() / step;
        for (int i = 0; i < num; i++) {
            double total = 0;
            for (int j = 0; j < step; j++) {
                total += copy.get(0);
                copy.remove(0);
            }
            temp.add(total / step);
        }
        return temp;
    }
}
