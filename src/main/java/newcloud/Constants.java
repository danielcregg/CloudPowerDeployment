package newcloud;

import newcloud.PowerModel.PowerModelDL360G7;
import newcloud.PowerModel.PowerModelDL360Gen9;
import newcloud.PowerModel.PowerModelML110G5;
import org.cloudbus.cloudsim.power.models.PowerModel;

/**
 * Simulation constants for the CloudPowerDeployment project.
 * <p>
 * This class defines the configuration parameters for the CloudSim simulation
 * including host specifications, VM types, RL hyperparameters, and PSO settings.
 * </p>
 *
 * @author CloudPowerDeployment contributors
 */
public class Constants {

    // --- Simulation control ---

    /** Whether to enable CloudSim log output. */
    public static final boolean ENABLE_OUTPUT = true;

    /** Whether to output results in CSV format. */
    public static final boolean OUTPUT_CSV = false;

    /** Scheduling interval in seconds (5 minutes). */
    public static final double SCHEDULING_INTERVAL = 300.0D;

    /** Maximum simulation duration in seconds (24 hours). */
    public static final double SIMULATION_LIMIT = 86400.0D;

    // --- Cloudlet configuration ---

    /** Length of each cloudlet in MI (Million Instructions). */
    public static final int CLOUDLET_LENGTH = 216000000;

    /** Number of PEs (processing elements) required per cloudlet. */
    public static final int CLOUDLET_PES = 1;

    // --- VM configuration ---

    /** Number of different VM types. */
    public static final int VM_TYPES = 4;

    /** VM bandwidth in Mbps. */
    public static final int VM_BW = 100000;

    /** VM disk size in MB. */
    public static final int VM_SIZE = 2500;

    /** MIPS rating for each VM type. */
    public static final int[] VM_MIPS = new int[]{3500, 3000, 2500, 2000, 1500};

    /** Number of PEs for each VM type. */
    public static final int[] VM_PES = new int[]{1, 1, 1, 1, 1};

    /** RAM in MB for each VM type. */
    public static final int[] VM_RAM = new int[]{2048, 2048, 1024, 1024, 512};

    // --- Host configuration ---

    /** Host type labels for reporting. */
    public static final String[] HOST_TYPES = new String[]{"G5", "G7", "G9"};

    /** Host bandwidth in Mbps. */
    public static final int HOST_BW = 1000000;

    /** Host storage in MB. */
    public static final int HOST_STORAGE = 1000000;

    /** Power models for each host type (ML110 G5, DL360 G7, DL360 Gen9). */
    public static final PowerModel[] HOST_POWER = new PowerModel[]{
            new PowerModelML110G5(),
            new PowerModelDL360G7(),
            new PowerModelDL360Gen9()
    };

    /** MIPS rating for each host type. */
    public static final int[] HOST_MIPS = new int[]{300, 1800, 5400};

    /** Number of PEs for each host type. */
    public static final int[] HOST_PES = new int[]{1, 1, 1};

    /** RAM in MB for each host type. */
    public static final int[] HOST_RAM = new int[]{65536, 65536, 65536};

    // --- Custom CloudSim event tags ---

    /** Event tag for VM creation request. */
    public static final int CREATE_VM = 99;

    /** Event tag for VM creation acknowledgment. */
    public static final int CREATE_VM_ACK = 100;

    /** Event tag for cloudlet submission. */
    public static final int SUBMIT_CLOUDLET = 101;

    /** Event tag for simulation restart (next iteration). */
    public static final int CLOUDSIM_RESTART = 102;

    // --- Experiment parameters ---

    /** Number of RL training iterations. */
    public static final int Iteration = 100;

    /** Number of physical hosts in the data center. */
    public static final int NUMBER_OF_HOSTS = 300;

    /** Time (in simulation seconds) at which to terminate the simulation. */
    public static final int terminateTime = 700;

    /** Time (in simulation seconds) after which to start recording power data. */
    public static final int outputTime = 600;

    /**
     * Input folder for PlanetLab workload traces.
     * Uses a relative path resolved from the project root directory.
     * Can be overridden via the system property "cloudsim.input.folder".
     */
    public static String inputFolder = System.getProperty(
            "cloudsim.input.folder",
            "src/main/resources/datas/200"
    );

    // --- PSO (Particle Swarm Optimization) parameters ---

    /** Population size (number of particles) for PSO. */
    public static final int POPULATION_SIZE = 300;

    /** Inertia weight for PSO. */
    public static final double W = 0.9;

    /** Acceleration coefficient for PSO. */
    public static final double C = 2.0;

    private Constants() {
        // Utility class; prevent instantiation
    }
}
