package newcloud.datacenter;

import newcloud.policy.VmAllocationAssignerFair;
import org.cloudbus.cloudsim.*;

import java.util.List;

/**
 * Datacenter variant using a fair (most-available-MIPS) VM placement strategy.
 * <p>
 * Always places the VM on the host with the most available MIPS capacity,
 * distributing load evenly across hosts.
 * </p>
 *
 * @see PowerDatacenterRL
 */
public class PowerDatacenterFair extends PowerDatacenterRL {

    private final VmAllocationAssignerFair assigner;

    public PowerDatacenterFair(
            String name,
            DatacenterCharacteristics characteristics,
            VmAllocationPolicy vmAllocationPolicy,
            List<Storage> storageList,
            double schedulingInterval,
            VmAllocationAssignerFair assigner,
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
        Host selectedHost = assigner.getVmAllocationHost(getHostList());
        if (selectedHost != null) {
            return selectedHost.getId();
        }
        return 0;
    }

    @Override
    protected void computeRewardAndUpdate(Vm vm) {
        // No RL update for fair strategy
    }
}
