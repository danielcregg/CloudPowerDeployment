package newcloud.Test;

import newcloud.Constants;
import newcloud.executedata.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Evaluates performance with varying numbers of VM placement requests.
 * Tests with different input folder sizes (50, 100, 150, 200, 250, 300 VMs).
 */
public class TaskCompare {
    public static void main(String[] args) {
        String baseFolder = "src/main/resources/datas/";
        String[] sizes = {"50", "100", "150", "200", "250", "300"};

        try {
            for (String size : sizes) {
                System.out.println("\n=== Task size: " + size + " ===");
                Constants.inputFolder = baseFolder + size;

                LearningScheduleTest learningTest = new LearningScheduleTest();
                List<Double> learningPower = learningTest.execute();
                System.out.println("Q-Learning min power: " +
                        (learningPower.isEmpty() ? "N/A" : Collections.min(learningPower)));

                LearningLamdaScheduleTest lamdaTest = new LearningLamdaScheduleTest();
                List<Double> lamdaPower = lamdaTest.execute();
                System.out.println("Q-Learning(Lambda) min power: " +
                        (lamdaPower.isEmpty() ? "N/A" : Collections.min(lamdaPower)));

                GreedyScheduleTest greedyTest = new GreedyScheduleTest();
                List<Double> greedyPower = greedyTest.execute();
                System.out.println("Greedy min power: " +
                        (greedyPower.isEmpty() ? "N/A" : Collections.min(greedyPower)));
            }
        } catch (Exception e) {
            System.out.println("Exception: " + e.toString());
            e.printStackTrace();
        }
    }
}
