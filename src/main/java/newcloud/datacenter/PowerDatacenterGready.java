package newcloud.datacenter;

import newcloud.policy.VmAllocationAssignerGready;
import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.power.PowerHost;

import java.util.List;

/**
 * Datacenter variant using a greedy (minimum energy increase) VM placement strategy.
 * <p>
 * For each incoming VM, this strategy evaluates all hosts and selects the one
 * that results in the minimum additional energy consumption.
 * </p>
 * <p>
 * Note: The class name preserves the original "Gready" spelling for backward
 * compatibility with existing test harnesses.
 * </p>
 *
 * @see PowerDatacenterRL
 */
public class PowerDatacenterGready extends PowerDatacenterRL {

    private final VmAllocationAssignerGready assigner;

    public PowerDatacenterGready(
            String name,
            DatacenterCharacteristics characteristics,
            VmAllocationPolicy vmAllocationPolicy,
            List<Storage> storageList,
            double schedulingInterval,
            VmAllocationAssignerGready assigner,
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
        // The greedy assigner returns a Host object, so we find its index
        Host selectedHost = assigner.getVmAllocationHost(this.<PowerHost>getHostList(), vm);
        if (selectedHost != null) {
            return selectedHost.getId();
        }
        return 0; // fallback to first host
    }

    @Override
    protected void computeRewardAndUpdate(Vm vm) {
        // No RL update for greedy strategy
    }
}
