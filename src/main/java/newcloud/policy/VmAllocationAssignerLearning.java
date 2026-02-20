package newcloud.policy;

import newcloud.GenExcel;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static newcloud.Constants.NUMBER_OF_HOSTS;

/**
 * Q-Learning-based VM allocation strategy.
 * <p>
 * Uses an epsilon-greedy policy to select hosts for VM placement and
 * updates a Q-value table using the standard Q-Learning update rule.
 * </p>
 */
public class VmAllocationAssignerLearning {

    private GenExcel genExcel;
    private double gamma;   // Discount factor
    private double alpha;   // Learning rate
    private double epsilon; // Exploration rate

    /** Q-value table: state -> (action/hostId -> Q-value) */
    public static Map<String, Map<Integer, Double>> QList = new HashMap<>();

    public VmAllocationAssignerLearning(double gamma, double alpha, double epsilon, GenExcel genExcel) {
        this.gamma = gamma;
        this.alpha = alpha;
        this.epsilon = epsilon;
        this.genExcel = genExcel;
        this.genExcel.init();
    }

    /**
     * Initialize a new row in the Q-table for the given state.
     * All Q-values are initialized to 0.
     */
    public void initRowOfQList(String state_idx) {
        QList.put(state_idx, new HashMap<Integer, Double>());
        for (int i = 0; i < NUMBER_OF_HOSTS; i++) {
            QList.get(state_idx).put(i, 0.0);
        }
    }

    /** Generate a random integer in [min, max] inclusive. */
    public int randomInt(int min, int max) {
        if (min == max) {
            return min;
        }
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

        // Epsilon-greedy: exploit with probability (1 - epsilon), explore otherwise
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
     * Update Q-table using Q-Learning update rule:
     * Q(s,a) = Q(s,a) + alpha * (reward + gamma * max_a' Q(s',a') - Q(s,a))
     *
     * @param action_idx  the action taken (host ID)
     * @param reward      the observed reward
     * @param lastcpulist the previous state
     * @param cpulist     the current (next) state
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

        // Find max Q-value in the next state
        double qMaxNextState = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < NUMBER_OF_HOSTS; i++) {
            if (qMaxNextState < QList.get(next_state_idx).get(i)) {
                qMaxNextState = QList.get(next_state_idx).get(i);
            }
        }

        // Q-Learning update
        double oldQ = QList.get(state_idx).get(action_idx);
        double newQ = oldQ + alpha * (reward + gamma * qMaxNextState - oldQ);
        QList.get(state_idx).put(action_idx, newQ);
    }
}
