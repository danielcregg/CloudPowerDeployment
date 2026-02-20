/*
 * Title:        CloudPowerDeployment
 * Description:  Base class for power-aware RL datacenter simulation.
 * Licence:      MIT
 */
package newcloud.datacenter;

import newcloud.NewPowerAllocatePolicy;
import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.core.predicates.PredicateType;
import org.cloudbus.cloudsim.power.PowerDatacenter;
import org.cloudbus.cloudsim.power.PowerHost;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static newcloud.Constants.*;

/**
 * Abstract base class for all power-aware datacenter variants.
 * <p>
 * This class consolidates the shared logic (cloudlet processing, energy tracking,
 * VM migration, etc.) that was previously duplicated across 8+ concrete datacenter
 * classes. Subclasses only need to implement the VM placement and reward logic.
 * </p>
 *
 * <h3>Reward Function Design</h3>
 * <p>
 * The original code used {@code Math.pow(lastPower/currentPower, 10000)} which causes
 * numerical overflow for any ratio != 1.0. The fixed reward function uses a normalized
 * negative power signal: {@code reward = -currentPower / maxPower}, where maxPower is
 * the maximum observed power so far. This keeps the reward bounded in [-1, 0] and
 * provides a smooth gradient that encourages power reduction.
 * </p>
 *
 * @author CloudPowerDeployment contributors
 */
public abstract class PowerDatacenterRL extends PowerDatacenter {

    /** Accumulated datacenter energy consumption. */
    private double power;

    /** Whether VM migrations are disabled. */
    private boolean disableMigrations;

    /** Last time cloudlets were submitted. */
    private double cloudletSubmitted;

    /** Count of VM migrations. */
    private int migrationCount;

    /** Current simulation clock time. */
    protected double currentTime;

    /** The host selected for the most recent VM placement. */
    protected Host targetHost;

    /** Per-host power history (most recent first). Used for reward calculation. */
    public static List<List<Double>> everyhosthistorypower = new ArrayList<>();

    /** Running log of total datacenter power at each output interval. */
    public static List<Double> allpower = new ArrayList<>();

    /** Maximum observed total power; used for reward normalization. */
    protected static double maxObservedPower = 1.0;

    /** The broker ID that this datacenter communicates with. Set by subclasses. */
    protected int brokerId;

    /**
     * Constructs a new PowerDatacenterRL.
     *
     * @param name               datacenter name
     * @param characteristics    datacenter characteristics
     * @param vmAllocationPolicy the VM allocation policy
     * @param storageList        list of storage devices
     * @param schedulingInterval scheduling interval in seconds
     * @param brokerId           the broker entity ID for restart events
     * @throws Exception if the parent constructor fails
     */
    public PowerDatacenterRL(
            String name,
            DatacenterCharacteristics characteristics,
            VmAllocationPolicy vmAllocationPolicy,
            List<Storage> storageList,
            double schedulingInterval,
            int brokerId) throws Exception {
        super(name, characteristics, vmAllocationPolicy, storageList, schedulingInterval);
        setPower(0.0);
        setDisableMigrations(false);
        setCloudletSubmitted(-1);
        setMigrationCount(0);
        this.brokerId = brokerId;
    }

    // -----------------------------------------------------------------------
    // Abstract methods for subclass-specific behavior
    // -----------------------------------------------------------------------

    /**
     * Select a host for the given VM. Called during VM creation.
     *
     * @param vm the VM to place
     * @return the ID of the selected host
     */
    protected abstract int selectHostForVm(Vm vm);

    /**
     * Called after cloudlet submission to compute the reward and update the
     * RL model (Q-table, etc.). Non-RL subclasses can implement this as a no-op.
     *
     * @param vm the VM associated with the submitted cloudlet (may be null for
     *           non-RL variants)
     */
    protected abstract void computeRewardAndUpdate(Vm vm);

    /**
     * Whether this datacenter variant uses RL and needs reward computation.
     *
     * @return true if this variant uses reinforcement learning
     */
    protected boolean usesReinforcementLearning() {
        return true;
    }

    // -----------------------------------------------------------------------
    // State representation (feature-based, replacing the intractable string state)
    // -----------------------------------------------------------------------

    /**
     * Compute a compact, feature-based state representation.
     * <p>
     * Instead of the original per-host utilization string (10^300 states for 300 hosts),
     * this method extracts aggregate features that capture the essential state of the
     * datacenter:
     * <ul>
     *   <li>Average CPU utilization across all hosts</li>
     *   <li>Number of active hosts (with at least one VM)</li>
     *   <li>Current total power consumption</li>
     *   <li>Total number of VMs deployed</li>
     * </ul>
     * These continuous features are discretized into bins to form a manageable state key.
     * </p>
     *
     * @return a string key representing the current state
     */
    protected String computeFeatureState() {
        double totalCpu = 0.0;
        int activeHosts = 0;
        double totalPower = 0.0;
        int totalVms = 0;

        for (PowerHost host : this.<PowerHost>getHostList()) {
            double cpu = host.getUtilizationOfCpu();
            totalCpu += cpu;
            if (host.getVmList().size() > 0) {
                activeHosts++;
            }
            totalVms += host.getVmList().size();
        }

        int numHosts = getHostList().size();
        double avgCpu = numHosts > 0 ? totalCpu / numHosts : 0.0;

        // Discretize features into bins
        int cpuBin = discretize(avgCpu, 10);                  // 0-9
        int activeBin = discretize((double) activeHosts / Math.max(numHosts, 1), 10); // 0-9
        int vmBin = Math.min(totalVms / 10, 9);               // 0-9 (capped)

        return cpuBin + "_" + activeBin + "_" + vmBin;
    }

    /**
     * Discretize a value in [0, 1] into a given number of bins.
     *
     * @param value   the value to discretize (expected in [0, 1])
     * @param numBins the number of discrete bins
     * @return the bin index [0, numBins-1]
     */
    protected static int discretize(double value, int numBins) {
        int bin = (int) (value * numBins);
        return Math.max(0, Math.min(bin, numBins - 1));
    }

    // -----------------------------------------------------------------------
    // Numerically stable reward function
    // -----------------------------------------------------------------------

    /**
     * Compute a numerically stable reward from the power history.
     * <p>
     * <strong>Design:</strong> The reward is the negative of the normalized current power.
     * This gives a reward in [-1, 0], where 0 means no power consumption (ideal)
     * and -1 means maximum observed power (worst case).
     * </p>
     * <p>
     * <strong>Why not the original?</strong> The original formula
     * {@code Math.pow(lastPower/currentPower, 10000)} overflows to Infinity or
     * underflows to 0 for any ratio that deviates even slightly from 1.0,
     * making learning impossible.
     * </p>
     *
     * @return the computed reward signal
     */
    protected double computeStableReward() {
        if (everyhosthistorypower.size() < 1) {
            return 0.0;
        }

        List<Double> currentPowers = everyhosthistorypower.get(0);
        double totalCurrentPower = 0.0;
        for (Double p : currentPowers) {
            totalCurrentPower += p;
        }

        // Track maximum observed power for normalization
        if (totalCurrentPower > maxObservedPower) {
            maxObservedPower = totalCurrentPower;
        }

        // Normalized negative power: bounded in [-1, 0]
        // Lower power => higher (less negative) reward
        return -totalCurrentPower / maxObservedPower;
    }

    // -----------------------------------------------------------------------
    // Event processing (shared across all variants)
    // -----------------------------------------------------------------------

    @Override
    protected void processOtherEvent(SimEvent ev) {
        switch (ev.getTag()) {
            case CREATE_VM_ACK:
                processVmCreate(ev, true);
                break;
            default:
                if (ev == null) {
                    Log.printConcatLine(getName(),
                            ".processOtherEvent(): Error - an event is null in Datacenter.");
                }
                break;
        }
    }

    @Override
    protected void processVmCreate(SimEvent ev, boolean ack) {
        Vm vm = (Vm) ev.getData();

        int hostId = selectHostForVm(vm);
        targetHost = getHostList().get(hostId);
        boolean result = getVmAllocationPolicy().allocateHostForVm(vm, targetHost);

        if (!result) {
            // Fallback: find any suitable host
            NewPowerAllocatePolicy policy = (NewPowerAllocatePolicy) getVmAllocationPolicy();
            targetHost = policy.findHostForVm(vm);
            result = policy.allocateHostForVm(vm);
        }

        if (ack) {
            int[] data = new int[3];
            data[0] = getId();
            data[1] = vm.getId();
            data[2] = result ? CloudSimTags.TRUE : CloudSimTags.FALSE;
            send(vm.getUserId(), CloudSim.getMinTimeBetweenEvents(), CREATE_VM_ACK, data);
        }

        if (result) {
            getVmList().add(vm);
            if (vm.isBeingInstantiated()) {
                vm.setBeingInstantiated(false);
            }
            vm.updateVmProcessing(CloudSim.clock(),
                    getVmAllocationPolicy().getHost(vm).getVmScheduler().getAllocatedMipsForVm(vm));
        }
    }

    @Override
    protected void updateCloudletProcessing() {
        currentTime = CloudSim.clock();

        if (currentTime > getLastProcessTime()) {
            System.out.print(currentTime + " ");

            double minTime = updateCloudletProcessingWithoutSchedulingFutureEventsForce();

            if (!isDisableMigrations()) {
                List<Map<String, Object>> migrationMap =
                        getVmAllocationPolicy().optimizeAllocation(getVmList());

                if (migrationMap != null) {
                    for (Map<String, Object> migrate : migrationMap) {
                        Vm vm = (Vm) migrate.get("vm");
                        PowerHost target = (PowerHost) migrate.get("host");
                        PowerHost oldHost = (PowerHost) vm.getHost();

                        if (oldHost == null) {
                            Log.formatLine("%.2f: Migration of VM #%d to Host #%d is started",
                                    currentTime, vm.getId(), target.getId());
                        } else {
                            Log.formatLine("%.2f: Migration of VM #%d from Host #%d to Host #%d is started",
                                    currentTime, vm.getId(), oldHost.getId(), target.getId());
                        }

                        target.addMigratingInVm(vm);
                        incrementMigrationCount();

                        // VM migration delay = RAM / (bandwidth / 2)
                        // Half of bandwidth reserved for migration, half for VM communication
                        send(getId(),
                                vm.getRam() / ((double) target.getBw() / (2 * 8000)),
                                CloudSimTags.VM_MIGRATE,
                                migrate);
                    }
                }
            }

            if (minTime != Double.MAX_VALUE) {
                CloudSim.cancelAll(getId(), new PredicateType(CloudSimTags.VM_DATACENTER_EVENT));
                send(getId(), getSchedulingInterval(), CloudSimTags.VM_DATACENTER_EVENT);
            }

            setLastProcessTime(currentTime);
        }
    }

    /**
     * Update cloudlet processing without scheduling future events.
     *
     * @return 0 if no update was needed, or the result of the forced update
     */
    protected double updateCloudletProcessingWithoutSchedulingFutureEvents() {
        if (CloudSim.clock() > getLastProcessTime()) {
            return updateCloudletProcessingWithoutSchedulingFutureEventsForce();
        }
        return 0;
    }

    /**
     * Force-update cloudlet processing, compute energy, and record power history.
     *
     * @return expected completion time of the next cloudlet, or Double.MAX_VALUE
     */
    protected double updateCloudletProcessingWithoutSchedulingFutureEventsForce() {
        double currentTime = CloudSim.clock();
        double minTime = Double.MAX_VALUE;
        double timeDiff = currentTime - getLastProcessTime();
        double timeFrameDatacenterEnergy = 0.0;
        List<Double> everyhostpower = new ArrayList<>();

        Log.printLine("\n\n--------------------------------------------------------------\n\n");
        Log.formatLine("New resource usage for the time frame starting at %.2f:", currentTime);

        for (PowerHost host : this.<PowerHost>getHostList()) {
            Log.printLine();
            double time = host.updateVmsProcessing(currentTime);
            if (time < minTime) {
                minTime = time;
            }
            Log.formatLine("%.2f: [Host #%d] utilization is %.2f%%",
                    currentTime, host.getId(), host.getUtilizationOfCpu() * 100);
        }

        if (timeDiff > 0) {
            Log.formatLine("\nEnergy consumption for the last time frame from %.2f to %.2f:",
                    getLastProcessTime(), currentTime);

            for (PowerHost host : this.<PowerHost>getHostList()) {
                double previousUtilizationOfCpu = host.getPreviousUtilizationOfCpu();
                double utilizationOfCpu = host.getUtilizationOfCpu();
                double timeFrameHostEnergy = host.getEnergyLinearInterpolation(
                        previousUtilizationOfCpu, utilizationOfCpu, timeDiff);
                timeFrameDatacenterEnergy += timeFrameHostEnergy;

                // Record per-host energy for reward computation
                everyhostpower.add(timeFrameHostEnergy);

                Log.printLine();
                Log.formatLine("%.2f: [Host #%d] utilization at %.2f was %.2f%%, now is %.2f%%",
                        currentTime, host.getId(), getLastProcessTime(),
                        previousUtilizationOfCpu * 100, utilizationOfCpu * 100);
                Log.formatLine("%.2f: [Host #%d] energy is %.2f W*sec",
                        currentTime, host.getId(), timeFrameHostEnergy);
            }

            Log.formatLine("\n%.2f: Data center's energy is %.2f W*sec\n",
                    currentTime, timeFrameDatacenterEnergy);
        }

        // Store power history (most recent first)
        everyhosthistorypower.add(0, everyhostpower);

        setPower(getPower() + timeFrameDatacenterEnergy);
        checkCloudletCompletion();

        Log.printLine();
        if (currentTime > outputTime) {
            allpower.add(getPower());
        }

        setLastProcessTime(currentTime);
        return minTime;
    }

    @Override
    protected void processVmMigrate(SimEvent ev, boolean ack) {
        updateCloudletProcessingWithoutSchedulingFutureEvents();
        super.processVmMigrate(ev, ack);
        SimEvent event = CloudSim.findFirstDeferred(getId(),
                new PredicateType(CloudSimTags.VM_MIGRATE));
        if (event == null || event.eventTime() > CloudSim.clock()) {
            updateCloudletProcessingWithoutSchedulingFutureEventsForce();
        }
    }

    @Override
    protected void processCloudletSubmit(SimEvent ev, boolean ack) {
        updateCloudletProcessing();

        Vm vm = null;
        try {
            Cloudlet cl = (Cloudlet) ev.getData();

            if (cl.isFinished()) {
                String name = CloudSim.getEntityName(cl.getUserId());
                Log.printConcatLine(getName(), ": Warning - Cloudlet #", cl.getCloudletId(),
                        " owned by ", name, " is already completed/finished.");
                Log.printLine("Therefore, it is not being executed again");
                Log.printLine();

                if (ack) {
                    int[] data = new int[3];
                    data[0] = getId();
                    data[1] = cl.getCloudletId();
                    data[2] = CloudSimTags.FALSE;
                    sendNow(cl.getUserId(), CloudSimTags.CLOUDLET_SUBMIT_ACK, data);
                }
                sendNow(cl.getUserId(), CloudSimTags.CLOUDLET_RETURN, cl);
                return;
            }

            cl.setResourceParameter(getId(),
                    getCharacteristics().getCostPerSecond(),
                    getCharacteristics().getCostPerBw());

            int userId = cl.getUserId();
            int vmId = cl.getVmId();

            double fileTransferTime = predictFileTransferTime(cl.getRequiredFiles());

            Host host = getVmAllocationPolicy().getHost(vmId, userId);
            vm = host.getVm(vmId, userId);
            CloudletScheduler scheduler = vm.getCloudletScheduler();
            double estimatedFinishTime = scheduler.cloudletSubmit(cl, fileTransferTime);

            if (estimatedFinishTime > 0.0 && !Double.isInfinite(estimatedFinishTime)) {
                estimatedFinishTime += fileTransferTime;
                System.out.println("estimatedFinishTime:" + estimatedFinishTime);
                send(getId(), currentTime, CloudSimTags.VM_DATACENTER_EVENT);
            }

            if (ack) {
                int[] data = new int[3];
                data[0] = getId();
                data[1] = cl.getCloudletId();
                data[2] = CloudSimTags.TRUE;
                sendNow(cl.getUserId(), CloudSimTags.CLOUDLET_SUBMIT_ACK, data);
            }
        } catch (ClassCastException c) {
            Log.printLine(getName() + ".processCloudletSubmit(): ClassCastException error.");
            c.printStackTrace();
        } catch (Exception e) {
            Log.printLine(getName() + ".processCloudletSubmit(): Exception error.");
            e.printStackTrace();
        }

        checkCloudletCompletion();
        setCloudletSubmitted(CloudSim.clock());

        // Compute reward and update RL model (no-op for non-RL variants)
        if (usesReinforcementLearning()) {
            computeRewardAndUpdate(vm);
        }

        send(brokerId, 0, CLOUDSIM_RESTART);
    }

    // -----------------------------------------------------------------------
    // Getters and setters
    // -----------------------------------------------------------------------

    @Override
    public double getPower() {
        return power;
    }

    protected void setPower(double power) {
        this.power = power;
    }

    protected boolean isInMigration() {
        for (Vm vm : getVmList()) {
            if (vm.isInMigration()) {
                return true;
            }
        }
        return false;
    }

    public boolean isDisableMigrations() {
        return disableMigrations;
    }

    public void setDisableMigrations(boolean disableMigrations) {
        this.disableMigrations = disableMigrations;
    }

    protected double getCloudletSubmitted() {
        return cloudletSubmitted;
    }

    protected void setCloudletSubmitted(double cloudletSubmitted) {
        this.cloudletSubmitted = cloudletSubmitted;
    }

    public int getMigrationCount() {
        return migrationCount;
    }

    protected void setMigrationCount(int migrationCount) {
        this.migrationCount = migrationCount;
    }

    protected void incrementMigrationCount() {
        setMigrationCount(getMigrationCount() + 1);
    }
}
