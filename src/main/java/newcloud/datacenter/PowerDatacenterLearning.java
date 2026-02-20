package newcloud.datacenter;

import newcloud.policy.VmAllocationAssignerLearning;
import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.power.PowerHost;

import java.util.List;

/**
 * Datacenter variant using tabular Q-Learning for VM placement.
 * <p>
 * The agent selects hosts using an epsilon-greedy policy over the Q-table
 * and updates Q-values using the standard Q-Learning update rule:
 * Q(s,a) = Q(s,a) + alpha * (reward + gamma * max_a' Q(s',a') - Q(s,a))
 * </p>
 *
 * @see PowerDatacenterRL
 */
public class PowerDatacenterLearning extends PowerDatacenterRL {

    private final VmAllocationAssignerLearning assigner;

    public PowerDatacenterLearning(
            String name,
            DatacenterCharacteristics characteristics,
            VmAllocationPolicy vmAllocationPolicy,
            List<Storage> storageList,
            double schedulingInterval,
            VmAllocationAssignerLearning assigner,
            int brokerId) throws Exception {
        super(name, characteristics, vmAllocationPolicy, storageList, schedulingInterval, brokerId);
        this.assigner = assigner;
    }

    @Override
    protected int selectHostForVm(Vm vm) {
        String state = computeFeatureState();
        return assigner.createAction(state);
    }

    @Override
    protected void computeRewardAndUpdate(Vm vm) {
        double reward = computeStableReward();
        String currentState = computeFeatureState();
        // Use the same state as both "last" and "current" since we compute after placement
        assigner.createLastState_idx(currentState);
        assigner.createState_idx(currentState);
        assigner.updateQList(targetHost.getId(), reward, currentState, currentState);
    }
}
