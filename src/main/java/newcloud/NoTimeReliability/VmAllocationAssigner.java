package newcloud.NoTimeReliability;

import newcloud.GenExcel;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static newcloud.Constants.NUMBER_OF_HOSTS;

/**
 * Q-Learning VM allocation assigner for the no-time-reliability variant.
 * <p>
 * Uses a simple Q-Learning approach where the reward is the inverse of
 * the total power consumption.
 * </p>
 */
public class VmAllocationAssigner {

    private GenExcel genExcel;
    private double gamma;   // Discount factor
    private double alpha;   // Learning rate
    private double epsilon; // Exploration rate

    /** Q-value table: state -> (action/hostId -> Q-value) */
    public static Map<String, Map<Integer, Double>> QList = new HashMap<>();

    public VmAllocationAssigner(double gamma, double alpha, double epsilon, GenExcel genExcel) {
        this.gamma = gamma;
        this.alpha = alpha;
        this.epsilon = epsilon;
        this.genExcel = genExcel;
        this.genExcel.init();
    }

    /** Initialize a Q-table row for the given state with all zeros. */
    public void initRowOfQList(String state_idx) {
        QList.put(state_idx, new HashMap<Integer, Double>());
        for (int i = 0; i < NUMBER_OF_HOSTS; i++) {
            QList.get(state_idx).put(i, 0.0);
        }
    }

    /** Generate a random integer in [min, max] inclusive. */
    public int randomInt(int min, int max) {
        if (min == max) return min;
        Random random = new Random();
        return random.nextInt(max - min + 1) + min;
    }

    /** Create or retrieve the state index for the previous state. */
    public String createLastState_idx(String lastcpulist) {
        return lastcpulist;
    }

    /** Create or retrieve the state index for the current state. */
    public String createState_idx(String cpulist) {
        return cpulist;
    }

    /**
     * Select an action (host ID) using epsilon-greedy policy.
     *
     * @param cpulist the current state representation
     * @return the selected host ID
     */
    public int createAction(String cpulist) {
        int current_action;
        int x = randomInt(0, 100);
        String state_idx = createState_idx(cpulist);

        if (!QList.containsKey(state_idx)) {
            initRowOfQList(state_idx);
        }

        // Epsilon-greedy: exploit with probability (1 - epsilon)
        if (((double) x / 100) < (1 - epsilon)) {
            // Exploit: choose action with highest Q-value
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
            // Explore: choose a random action
            current_action = randomInt(0, NUMBER_OF_HOSTS - 1);
        }
        return current_action;
    }

    /**
     * Update Q-table. Reward is computed as 1/power (lower power = higher reward).
     *
     * @param action_idx  the action taken (host ID)
     * @param reward      the observed total power consumption
     * @param lastcpulist the previous state
     * @param cpulist     the current state
     */
    public void updateQList(int action_idx, double reward, String lastcpulist, String cpulist) {
        // Convert power to reward: lower power = higher reward
        double finalreward;
        if (reward == 0.0) {
            finalreward = 1.0; // Zero power is ideal
        } else {
            finalreward = 1.0 / reward;
        }

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

        // Q-Learning update
        double oldQ = QList.get(state_idx).get(action_idx);
        double newQ = oldQ + alpha * (finalreward + gamma * qMaxNextState - oldQ);
        QList.get(state_idx).put(action_idx, newQ);
    }
}
