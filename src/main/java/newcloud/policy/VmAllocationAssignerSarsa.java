package newcloud.policy;

import newcloud.GenExcel;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static newcloud.Constants.NUMBER_OF_HOSTS;

/**
 * SARSA (State-Action-Reward-State-Action) VM allocation strategy.
 * <p>
 * On-policy TD control: uses the actual next action for the update,
 * unlike Q-Learning which uses the max over next-state actions.
 * </p>
 */
public class VmAllocationAssignerSarsa {

    private GenExcel genExcel;
    private double gamma;
    private double alpha;
    private double epsilon;

    public static Map<String, Map<Integer, Double>> QList = new HashMap<>();

    public VmAllocationAssignerSarsa(double gamma, double alpha, double epsilon, GenExcel genExcel) {
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

    public String createLastState_idx(String lastcpulist) {
        return lastcpulist;
    }

    public String createState_idx(String cpulist) {
        return cpulist;
    }

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
     * SARSA update: Q(s,a) = Q(s,a) + alpha * (reward + gamma * Q(s',a') - Q(s,a))
     * where a' is the actual next action chosen by the policy.
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

        // SARSA: use actual next action (not max)
        int next_action = createAction(next_state_idx);
        double oldQ = QList.get(state_idx).get(action_idx);
        double newQ = oldQ + alpha * (reward + gamma * QList.get(next_state_idx).get(next_action) - oldQ);
        QList.get(state_idx).put(action_idx, newQ);
    }
}
