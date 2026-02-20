package newcloud.datacenter;

import newcloud.policy.VmAllocationAssignerLearningAndNoConverge;
import org.cloudbus.cloudsim.*;

import java.util.List;

/**
 * Datacenter variant using Q-Learning without state aggregation (convergence test).
 * <p>
 * Used to demonstrate the effect of state aggregation on convergence.
 * Without aggregation, the state space is much larger and convergence is slower.
 * </p>
 *
 * @see PowerDatacenterRL
 */
public class PowerDatacenterLearningAndNoConverge extends PowerDatacenterRL {

    private final VmAllocationAssignerLearningAndNoConverge assigner;

    public PowerDatacenterLearningAndNoConverge(
            String name,
            DatacenterCharacteristics characteristics,
            VmAllocationPolicy vmAllocationPolicy,
            List<Storage> storageList,
            double schedulingInterval,
            VmAllocationAssignerLearningAndNoConverge assigner,
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
        assigner.createLastState_idx(currentState);
        assigner.createState_idx(currentState);
        assigner.updateQList(targetHost.getId(), reward, currentState, currentState);
    }
}
