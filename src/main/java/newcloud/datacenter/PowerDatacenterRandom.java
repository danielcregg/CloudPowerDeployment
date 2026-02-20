package newcloud.datacenter;

import newcloud.policy.VmAllocationAssignerRandom;
import org.cloudbus.cloudsim.*;

import java.util.List;

/**
 * Datacenter variant using random VM placement.
 * <p>
 * Serves as a baseline: VMs are placed on randomly selected hosts.
 * </p>
 *
 * @see PowerDatacenterRL
 */
public class PowerDatacenterRandom extends PowerDatacenterRL {

    private final VmAllocationAssignerRandom assigner;

    public PowerDatacenterRandom(
            String name,
            DatacenterCharacteristics characteristics,
            VmAllocationPolicy vmAllocationPolicy,
            List<Storage> storageList,
            double schedulingInterval,
            VmAllocationAssignerRandom assigner,
            int brokerId) throws Exception {
        super(name, characteristics, vmAllocationPolicy, storageList, schedulingInterval, brokerId);
        this.assigner = assigner;
    }

    @Override
    protected boolean usesReinforcementLearning() {
        return false;
    }

    @Override
    protected int selectHostForVm(Vm vm) {
        Host selectedHost = assigner.getVmAllocationHost(getHostList(), vm);
        if (selectedHost != null) {
            return selectedHost.getId();
        }
        return 0;
    }

    @Override
    protected void computeRewardAndUpdate(Vm vm) {
        // No RL update for random strategy
    }
}
