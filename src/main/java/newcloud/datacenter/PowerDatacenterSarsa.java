package newcloud.datacenter;

import newcloud.policy.VmAllocationAssignerSarsa;
import org.cloudbus.cloudsim.*;

import java.util.List;

/**
 * Datacenter variant using SARSA (State-Action-Reward-State-Action) for VM placement.
 * <p>
 * Unlike Q-Learning which is off-policy and uses max over next-state actions,
 * SARSA is on-policy and uses the actual next action taken.
 * </p>
 *
 * @see PowerDatacenterRL
 */
public class PowerDatacenterSarsa extends PowerDatacenterRL {

    private final VmAllocationAssignerSarsa assigner;

    public PowerDatacenterSarsa(
            String name,
            DatacenterCharacteristics characteristics,
            VmAllocationPolicy vmAllocationPolicy,
            List<Storage> storageList,
            double schedulingInterval,
            VmAllocationAssignerSarsa assigner,
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
