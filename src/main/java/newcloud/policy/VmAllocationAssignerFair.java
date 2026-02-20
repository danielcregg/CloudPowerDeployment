package newcloud.policy;

import newcloud.GenExcel;
import org.cloudbus.cloudsim.Host;

import java.util.List;

/**
 * Fair (round-robin by capacity) VM allocation strategy.
 * <p>
 * Places VMs on the host with the most available MIPS, distributing
 * load across hosts as evenly as possible.
 * </p>
 */
public class VmAllocationAssignerFair {

    private GenExcel genExcel;

    public VmAllocationAssignerFair(GenExcel genExcel) {
        this.genExcel = genExcel;
        this.genExcel.init();
    }

    /**
     * Select the host with the most available MIPS capacity.
     * (Method name fixed from original "getVmAllcaotionHost")
     *
     * @param hostList the list of available hosts
     * @return the selected host
     */
    public Host getVmAllocationHost(List<Host> hostList) {
        double availableMips = Double.MIN_VALUE;
        Host targetHost = null;
        for (Host host : hostList) {
            double mips = host.getAvailableMips();
            if (mips >= availableMips) {
                availableMips = mips;
                targetHost = host;
            }
        }
        return targetHost;
    }

    /**
     * @deprecated Use {@link #getVmAllocationHost(List)} instead.
     */
    @Deprecated
    public Host getVmAllcaotionHost(List<Host> hostList) {
        return getVmAllocationHost(hostList);
    }
}
