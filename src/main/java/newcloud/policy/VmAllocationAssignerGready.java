package newcloud.policy;

import newcloud.GenExcel;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicy;
import org.cloudbus.cloudsim.power.PowerHost;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Greedy VM allocation strategy.
 * <p>
 * Evaluates all hosts and places the VM on the host that results in the
 * minimum incremental energy consumption.
 * </p>
 */
public class VmAllocationAssignerGready {

    private GenExcel genExcel;
    private VmAllocationPolicy vmAllocationPolicy;

    public VmAllocationAssignerGready(VmAllocationPolicy vmAllocationPolicy, GenExcel genExcel) {
        this.vmAllocationPolicy = vmAllocationPolicy;
        this.genExcel = genExcel;
        this.genExcel.init();
    }

    /**
     * Select the host with the minimum energy increase for the given VM.
     * (Method name fixed from original "getVmAllcaotionHost")
     *
     * @param hostList the list of available hosts
     * @param vm       the VM to place
     * @return the selected host
     */
    public Host getVmAllocationHost(List<PowerHost> hostList, Vm vm) {
        Host targetHost = null;
        List<Double> totalPowerList = new ArrayList<>();
        for (int i = 0; i < hostList.size(); i++) {
            PowerHost host = hostList.get(i);
            double previousUtilizationOfCpu = (host.getTotalMips() - host.getAvailableMips()) / host.getTotalMips();
            vmAllocationPolicy.allocateHostForVm(vm, host);
            double utilizationOfCpu = (host.getTotalMips() - host.getAvailableMips()) / host.getTotalMips();
            double timeFrameHostEnergy = host.getEnergyLinearInterpolation(
                    previousUtilizationOfCpu, utilizationOfCpu, 100);
            totalPowerList.add(i, timeFrameHostEnergy);
            vmAllocationPolicy.deallocateHostForVm(vm);
        }
        double minValue = Collections.min(totalPowerList);
        int index = totalPowerList.indexOf(minValue);
        targetHost = hostList.get(index);
        return targetHost;
    }

    /**
     * @deprecated Use {@link #getVmAllocationHost(List, Vm)} instead.
     */
    @Deprecated
    public Host getVmAllcaotionHost(List<PowerHost> hostList, Vm vm) {
        return getVmAllocationHost(hostList, vm);
    }
}
