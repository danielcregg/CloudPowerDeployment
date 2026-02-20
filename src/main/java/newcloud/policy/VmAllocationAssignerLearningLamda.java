package newcloud.policy;

import newcloud.GenExcel;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static newcloud.Constants.NUMBER_OF_HOSTS;

/**
 * Q-Learning(Lambda) VM allocation strategy with eligibility traces.
 * <p>
 * Extends Q-Learning by maintaining an eligibility trace table (E-table)
 * that distributes temporal credit across multiple state-action pairs.
 * </p>
 */
public class VmAllocationAssignerLearningLamda {

    private GenExcel genExcel;
    private double gamma;   // Discount factor
    private double alpha;   // Learning rate
    private double epsilon; // Exploration rate
    private double lamda;   // Eligibility trace decay rate

    /** Q-value table: state -> (action -> Q-value) */
    public static Map<String, Map<Integer, Double>> QList = new HashMap<>();

    /** Eligibility trace table: state -> (action -> trace value) */
    public static Map<String, Map<Integer, Double>> EList = new HashMap<>();

    public VmAllocationAssignerLearningLamda(double gamma, double alpha, double epsilon, double lamda, GenExcel genExcel) {
        this.gamma = gamma;
        this.alpha = alpha;
        this.epsilon = epsilon;
        this.lamda = lamda;
        this.genExcel = genExcel;
        this.genExcel.init();
    }

    /** Initialize Q-table and E-table rows for the given state. */
    public void initRowOfQList(String state_idx) {
        QList.put(state_idx, new HashMap<Integer, Double>());
        EList.put(state_idx, new HashMap<Integer, Double>());
        for (int i = 0; i < NUMBER_OF_HOSTS; i++) {
            QList.get(state_idx).put(i, 0.0);
            EList.get(state_idx).put(i, 0.0);
        }
    }

    public int randomInt(int min, int max) {
        if (min == max) return min;
        Random random = new Random();
        return random.nextInt(max - min + 1) + min;
    }

    public String createLastState_idx(String lastcpulist) {
        return lastcpulist;
    }

    public String createState_idx(String cpulist) {
        return cpulist;
    }

    /**
     * Select an action using epsilon-greedy policy.
     */
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

    /**
     * Update Q-table and E-table using Q(Lambda) update rule.
     * <p>
     * delta = reward + gamma * max_a' Q(s', a') - Q(s, a)
     * E(s, a) = E(s, a) + 1
     * For all (s, a): Q(s,a) += alpha * delta * E(s,a); E(s,a) *= gamma * lambda
     * </p>
     */
    public void updateQList(int action_idx, double reward, String lastcpulist, String cpulist) {
        String state_idx = createLastState_idx(lastcpulist);
        String next_state_idx = createState_idx(cpulist);

        if (!QList.containsKey(state_idx)) {
            initRowOfQList(state_idx);
        }
        if (!QList.containsKey(next_state_idx)) {
            initRowOfQList(next_state_idx);
        }

        // Find max Q-value in next state
        double qMaxNextState = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < NUMBER_OF_HOSTS; i++) {
            if (qMaxNextState < QList.get(next_state_idx).get(i)) {
                qMaxNextState = QList.get(next_state_idx).get(i);
            }
        }

        // Compute TD error
        double delta = reward + gamma * qMaxNextState - QList.get(state_idx).get(action_idx);

        // Increment eligibility trace for current state-action
        EList.get(state_idx).put(action_idx, EList.get(state_idx).get(action_idx) + 1);

        // Update all state-action pairs using eligibility traces
        for (String key : EList.keySet()) {
            for (int j = 0; j < NUMBER_OF_HOSTS; j++) {
                QList.get(key).put(j, QList.get(key).get(j) + alpha * delta * EList.get(key).get(j));
                EList.get(key).put(j, EList.get(key).get(j) * gamma * lamda);
            }
        }
    }
}
