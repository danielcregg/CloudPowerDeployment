package newcloud.executedata;

import newcloud.*;
import newcloud.datacenter.PowerDatacenterLearning;
import newcloud.policy.VmAllocationAssignerLearning;
import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.examples.power.planetlab.PlanetLabHelper;
import org.cloudbus.cloudsim.power.PowerHost;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import static newcloud.Constants.*;
import static newcloud.policy.VmAllocationAssignerLearning.QList;

/**
 * Test harness for the Q-Learning VM placement strategy.
 * Runs multiple iterations and collects power consumption data.
 */
public class LearningScheduleTest {

    private static List<Cloudlet> cloudletList;
    public static List<Vm> vmList;
    private static List<PowerHost> hostList;
    private static DatacenterBroker broker;
    public static int brokerId;
    private static VmAllocationAssignerLearning vmAllocationAssignerLearning;
    private static double smallestdata = Double.MAX_VALUE;

    double LEARNING_GAMMA = 0.9;
    double LEARNING_ALPHA = 0.8;
    double LEARNING_EPSILON = 0.2;

    public double getLEARNING_GAMMA() { return LEARNING_GAMMA; }
    public void setLEARNING_GAMMA(double v) { this.LEARNING_GAMMA = v; }
    public double getLEARNING_ALPHA() { return LEARNING_ALPHA; }
    public void setLEARNING_ALPHA(double v) { this.LEARNING_ALPHA = v; }
    public double getLEARNING_EPSILON() { return LEARNING_EPSILON; }
    public void setLEARNING_EPSILON(double v) { this.LEARNING_EPSILON = v; }

    public List<Double> execute() throws Exception {
        for (int i = 0; i < Iteration; i++) {
            LEARNING_EPSILON = 1.0 / (i + 1);
            vmAllocationAssignerLearning = new VmAllocationAssignerLearning(
                    LEARNING_GAMMA, LEARNING_ALPHA, LEARNING_EPSILON, GenExcel.getInstance());

            CloudSim.init(1, Calendar.getInstance(), false);
            broker = createBroker();
            brokerId = broker.getId();

            cloudletList = PlanetLabHelper.createCloudletListPlanetLab(brokerId, inputFolder);
            vmList = newHelper.createVmList(brokerId, cloudletList.size());
            hostList = newHelper.createHostList(Constants.NUMBER_OF_HOSTS);
            VmAllocationPolicy vmAllocationPolicy = new NewPowerAllocatePolicy(hostList);

            PowerDatacenterLearning datacenter = new PowerDatacenterLearning(
                    "Datacenter",
                    createCharacteristics(hostList),
                    vmAllocationPolicy,
                    new LinkedList<Storage>(),
                    300,
                    vmAllocationAssignerLearning,
                    brokerId);
            datacenter.setDisableMigrations(false);

            broker.submitVmList(vmList);
            broker.submitCloudletList(cloudletList);
            CloudSim.terminateSimulation(terminateTime);

            double lastClock = CloudSim.startSimulation();
            List<Cloudlet> newList = broker.getCloudletReceivedList();
            CloudSim.stopSimulation();
            System.out.println(i + "----------------------------------");
        }

        for (String s : QList.keySet()) {
            System.out.println(s + ":" + QList.get(s));
        }
        System.out.println(QList.size());

        for (int i = 0; i < PowerDatacenterLearning.allpower.size(); i++) {
            System.out.println(PowerDatacenterLearning.allpower.get(i));
            if (PowerDatacenterLearning.allpower.get(i) < smallestdata) {
                smallestdata = PowerDatacenterLearning.allpower.get(i);
            }
        }
        System.out.println("Minimum power: " + smallestdata);
        return PowerDatacenterLearning.allpower;
    }

    private DatacenterCharacteristics createCharacteristics(List<PowerHost> hostList) {
        return new DatacenterCharacteristics("x86", "Linux", "Xen",
                hostList, 10.0, 3.0, 0.05, 0.001, 0.0);
    }

    public DatacenterBroker createBroker() {
        NewPowerDatacenterBroker broker = null;
        try {
            broker = new NewPowerDatacenterBroker("Broker");
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
        return broker;
    }

    public List<Integer> getNumByType() {
        List<Integer> resultNum = new ArrayList<>();
        int[] highCPUHostType = new int[]{0, 0, 0};
        int[] lowCPUHostType = new int[]{0, 0, 0};
        int[] zeroCPUHostType = new int[]{0, 0, 0};
        List<PowerHost> highCPUHost = new ArrayList<>();
        List<PowerHost> lowCPUHost = new ArrayList<>();
        List<PowerHost> zeroCPUHost = new ArrayList<>();

        for (PowerHost host : hostList) {
            double usedCpu = 1 - (double) Math.round((host.getAvailableMips() / host.getTotalMips()) * 100) / 100;
            if (usedCpu >= 0.8) {
                highCPUHost.add(host);
            } else if (usedCpu > 0.2) {
                lowCPUHost.add(host);
            } else {
                zeroCPUHost.add(host);
            }
        }
        categorizeByType(highCPUHost, highCPUHostType);
        categorizeByType(lowCPUHost, lowCPUHostType);
        categorizeByType(zeroCPUHost, zeroCPUHostType);

        resultNum.add(highCPUHost.size());
        for (int v : highCPUHostType) resultNum.add(v);
        resultNum.add(lowCPUHost.size());
        for (int v : lowCPUHostType) resultNum.add(v);
        resultNum.add(zeroCPUHost.size());
        for (int v : zeroCPUHostType) resultNum.add(v);
        return resultNum;
    }

    private void categorizeByType(List<PowerHost> hosts, int[] typeCount) {
        for (PowerHost host : hosts) {
            for (int i = 0; i < HOST_MIPS.length; i++) {
                if (host.getTotalMips() == HOST_MIPS[i]) {
                    typeCount[i]++;
                }
            }
        }
    }
}
