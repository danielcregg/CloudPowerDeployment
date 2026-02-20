package newcloud.policy;

import newcloud.GenExcel;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Vm;

import java.util.List;
import java.util.Random;

/**
 * Random VM allocation strategy (baseline).
 * <p>
 * Places VMs on randomly selected hosts. Used as a performance baseline
 * for comparison with RL-based strategies.
 * </p>
 */
public class VmAllocationAssignerRandom {

    private GenExcel genExcel;

    public VmAllocationAssignerRandom(GenExcel genExcel) {
        this.genExcel = genExcel;
        this.genExcel.init();
    }

    /**
     * Select a random host for the given VM.
     * (Method name fixed from original "getVmAllcaotionHost")
     *
     * @param hostList the list of available hosts
     * @param vm       the VM to place
     * @return the randomly selected host
     */
    public Host getVmAllocationHost(List<Host> hostList, Vm vm) {
        int index = randomInt(0, hostList.size() - 1);
        return hostList.get(index);
    }

    /**
     * @deprecated Use {@link #getVmAllocationHost(List, Vm)} instead.
     */
    @Deprecated
    public Host getVmAllcaotionHost(List<Host> hostList, Vm vm) {
        return getVmAllocationHost(hostList, vm);
    }

    private int randomInt(int min, int max) {
        if (min == max) return min;
        Random random = new Random();
        return random.nextInt(max - min + 1) + min;
    }
}
