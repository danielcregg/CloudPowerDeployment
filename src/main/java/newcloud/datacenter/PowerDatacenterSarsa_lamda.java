package newcloud.datacenter;

import newcloud.policy.VmAllocationAssignerSarsa_lamda;
import org.cloudbus.cloudsim.*;

import java.util.List;

/**
 * Datacenter variant using SARSA(Lambda) with eligibility traces.
 *
 * @see PowerDatacenterRL
 */
public class PowerDatacenterSarsa_lamda extends PowerDatacenterRL {

    private final VmAllocationAssignerSarsa_lamda assigner;

    public PowerDatacenterSarsa_lamda(
            String name,
            DatacenterCharacteristics characteristics,
            VmAllocationPolicy vmAllocationPolicy,
            List<Storage> storageList,
            double schedulingInterval,
            VmAllocationAssignerSarsa_lamda assigner,
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
