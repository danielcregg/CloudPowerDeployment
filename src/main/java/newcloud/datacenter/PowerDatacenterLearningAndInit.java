package newcloud.datacenter;

import newcloud.policy.VmAllocationAssignerLearningAndInit;
import org.cloudbus.cloudsim.*;

import java.util.List;

/**
 * Datacenter variant using Q-Learning with initialized Q-values.
 * <p>
 * Similar to standard Q-Learning but initializes the Q-table row for a new
 * state with a reward-based value for the current VM's preferred host, rather
 * than all zeros. This can accelerate early convergence.
 * </p>
 *
 * @see PowerDatacenterRL
 */
public class PowerDatacenterLearningAndInit extends PowerDatacenterRL {

    private final VmAllocationAssignerLearningAndInit assigner;

    public PowerDatacenterLearningAndInit(
            String name,
            DatacenterCharacteristics characteristics,
            VmAllocationPolicy vmAllocationPolicy,
            List<Storage> storageList,
            double schedulingInterval,
            VmAllocationAssignerLearningAndInit assigner,
            int brokerId) throws Exception {
        super(name, characteristics, vmAllocationPolicy, storageList, schedulingInterval, brokerId);
        this.assigner = assigner;
    }

    @Override
    protected int selectHostForVm(Vm vm) {
        String state = computeFeatureState();
        return assigner.createAction(state, vm);
    }

    @Override
    protected void computeRewardAndUpdate(Vm vm) {
        double reward = computeStableReward();
        String currentState = computeFeatureState();
        assigner.createLastState_idx(currentState);
        assigner.createState_idx(currentState);
        assigner.updateQList(targetHost.getId(), reward, currentState, currentState, vm);
    }
}
