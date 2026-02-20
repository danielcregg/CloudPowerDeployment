package newcloud.policy;

import newcloud.GenExcel;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static newcloud.Constants.NUMBER_OF_HOSTS;

/**
 * Q-Learning without state aggregation (used for convergence comparison).
 * <p>
 * Identical algorithm to standard Q-Learning but tested against the
 * aggregated-state variant to demonstrate convergence benefits of state aggregation.
 * </p>
 */
public class VmAllocationAssignerLearningAndNoConverge {

    private GenExcel genExcel;
    private double gamma;
    private double alpha;
    private double epsilon;

    public static Map<String, Map<Integer, Double>> QList = new HashMap<>();

    public VmAllocationAssignerLearningAndNoConverge(double gamma, double alpha, double epsilon, GenExcel genExcel) {
        this.gamma = gamma;
        this.alpha = alpha;
        this.epsilon = epsilon;
        this.genExcel = genExcel;
        this.genExcel.init();
    }

    public void initRowOfQList(String state_idx) {
        QList.put(state_idx, new HashMap<Integer, Double>());
        for (int i = 0; i < NUMBER_OF_HOSTS; i++) {
            QList.get(state_idx).put(i, 0.0);
        }
    }

    public int randomInt(int min, int max) {
        if (min == max) return min;
        Random random = new Random();
        return random.nextInt(max - min + 1) + min;
    }

    public String createLastState_idx(String lastcpulist) { return lastcpulist; }
    public String createState_idx(String cpulist) { return cpulist; }

    public int createAction(String cpulist) {
        int current_action;
        int x = randomInt(0, 100);
        String state_idx = createState_idx(cpulist);

        if (!QList.containsKey(state_idx)) {
            initRowOfQList(state_idx);
        }

        if (((double) x / 100) < (1 - epsilon)) {
            int bestAction = 0;
            double bestValue = Double.NEGATIVE_INFINITY;
            for (int i = 0; i < NUMBER_OF_HOSTS; i++) {
                if (bestValue < QList.get(state_idx).get(i)) {
                    bestValue = QList.get(state_idx).get(i);
                    bestAction = i;
                }
            }
            current_action = bestAction;
        } else {
            current_action = randomInt(0, NUMBER_OF_HOSTS - 1);
        }
        return current_action;
    }

    public void updateQList(int action_idx, double reward, String lastcpulist, String cpulist) {
        String state_idx = createLastState_idx(lastcpulist);
        String next_state_idx = createState_idx(cpulist);

        if (!QList.containsKey(state_idx)) {
            initRowOfQList(state_idx);
        }
        if (!QList.containsKey(next_state_idx)) {
            initRowOfQList(next_state_idx);
        }

        double qMaxNextState = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < NUMBER_OF_HOSTS; i++) {
            if (qMaxNextState < QList.get(next_state_idx).get(i)) {
                qMaxNextState = QList.get(next_state_idx).get(i);
            }
        }

        double oldQ = QList.get(state_idx).get(action_idx);
        double newQ = oldQ + alpha * (reward + gamma * qMaxNextState - oldQ);
        QList.get(state_idx).put(action_idx, newQ);
    }
}
