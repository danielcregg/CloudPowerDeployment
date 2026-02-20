package newcloud.datacenter;

import newcloud.policy.VmAllocationAssignerLearningLamda;
import org.cloudbus.cloudsim.*;

import java.util.List;

/**
 * Datacenter variant using Q-Learning(Lambda) with eligibility traces.
 * <p>
 * Extends standard Q-Learning by maintaining an eligibility trace (E-table)
 * that allows temporal credit assignment across multiple time steps.
 * </p>
 *
 * @see PowerDatacenterRL
 */
public class PowerDatacenterLearningLamda extends PowerDatacenterRL {

    private final VmAllocationAssignerLearningLamda assigner;

    public PowerDatacenterLearningLamda(
            String name,
            DatacenterCharacteristics characteristics,
            VmAllocationPolicy vmAllocationPolicy,
            List<Storage> storageList,
            double schedulingInterval,
            VmAllocationAssignerLearningLamda assigner,
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
