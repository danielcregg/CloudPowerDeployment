package newcloud.executedata;

import newcloud.*;
import newcloud.datacenter.PowerDatacenterFair;
import newcloud.policy.VmAllocationAssignerFair;
import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.examples.power.planetlab.PlanetLabHelper;
import org.cloudbus.cloudsim.power.PowerHost;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import static newcloud.Constants.*;

/**
 * Test harness for the fair (most-available-MIPS) VM placement strategy.
 */
public class FairScheduleTest {

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
            VmAllocationAssignerFair assigner = new VmAllocationAssignerFair(GenExcel.getInstance());

            DatacenterCharacteristics chars = new DatacenterCharacteristics(
                    "x86", "Linux", "Xen", hostList, 10.0, 3.0, 0.05, 0.001, 0.0);
            PowerDatacenterFair datacenter = new PowerDatacenterFair(
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
        return PowerDatacenterFair.allpower;
    }

    private DatacenterBroker createBroker() {
        try {
            return new NewPowerDatacenterBroker("Broker");
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
            return null;
        }
    }
}
