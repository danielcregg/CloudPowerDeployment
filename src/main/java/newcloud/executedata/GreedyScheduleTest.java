package newcloud.executedata;

import newcloud.*;
import newcloud.datacenter.PowerDatacenterGready;
import newcloud.policy.VmAllocationAssignerGready;
import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.examples.power.planetlab.PlanetLabHelper;
import org.cloudbus.cloudsim.power.PowerHost;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import static newcloud.Constants.*;

/**
 * Test harness for the greedy VM placement strategy.
 */
public class GreedyScheduleTest {

    private static List<Cloudlet> cloudletList;
    public static List<Vm> vmList;
    private static List<PowerHost> hostList;
    private static DatacenterBroker broker;
    public static int brokerId;

    public List<Double> execute() throws Exception {
        for (int i = 0; i < Iteration; i++) {
            CloudSim.init(1, Calendar.getInstance(), false);
            broker = createBroker();
            brokerId = broker.getId();

            cloudletList = PlanetLabHelper.createCloudletListPlanetLab(brokerId, inputFolder);
            vmList = newHelper.createVmList(brokerId, cloudletList.size());
            hostList = newHelper.createHostList(NUMBER_OF_HOSTS);
            VmAllocationPolicy vmAllocationPolicy = new NewPowerAllocatePolicy(hostList);
            VmAllocationAssignerGready assigner = new VmAllocationAssignerGready(vmAllocationPolicy, GenExcel.getInstance());

            DatacenterCharacteristics chars = new DatacenterCharacteristics(
                    "x86", "Linux", "Xen", hostList, 10.0, 3.0, 0.05, 0.001, 0.0);
            PowerDatacenterGready datacenter = new PowerDatacenterGready(
                    "Datacenter", chars, vmAllocationPolicy,
                    new LinkedList<Storage>(), 300, assigner, brokerId);
            datacenter.setDisableMigrations(false);

            broker.submitVmList(vmList);
            broker.submitCloudletList(cloudletList);
            CloudSim.terminateSimulation(terminateTime);
            CloudSim.startSimulation();
            CloudSim.stopSimulation();
            System.out.println(i + "----------------------------------");
        }
        return PowerDatacenterGready.allpower;
    }

    public List<Integer> getNumByType() {
        List<Integer> resultNum = new ArrayList<>();
        int[] highType = new int[3], lowType = new int[3], zeroType = new int[3];
        int high = 0, low = 0, zero = 0;
        for (PowerHost host : hostList) {
            double usedCpu = 1 - (double) Math.round((host.getAvailableMips() / host.getTotalMips()) * 100) / 100;
            int typeIdx = -1;
            for (int i = 0; i < HOST_MIPS.length; i++) {
                if (host.getTotalMips() == HOST_MIPS[i]) typeIdx = i;
            }
            if (usedCpu >= 0.8) { high++; if (typeIdx >= 0) highType[typeIdx]++; }
            else if (usedCpu > 0.2) { low++; if (typeIdx >= 0) lowType[typeIdx]++; }
            else { zero++; if (typeIdx >= 0) zeroType[typeIdx]++; }
        }
        resultNum.add(high); for (int v : highType) resultNum.add(v);
        resultNum.add(low); for (int v : lowType) resultNum.add(v);
        resultNum.add(zero); for (int v : zeroType) resultNum.add(v);
        return resultNum;
    }

    private DatacenterBroker createBroker() {
        try { return new NewPowerDatacenterBroker("Broker"); }
        catch (Exception e) { e.printStackTrace(); System.exit(0); return null; }
    }
}
