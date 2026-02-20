package newcloud;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests for the Constants class to verify configuration values are sensible.
 */
public class ConstantsTest {

    @Test
    public void testHostMipsArrayLength() {
        assertEquals("HOST_MIPS should have 3 entries (one per host type)",
                3, Constants.HOST_MIPS.length);
    }

    @Test
    public void testHostPesArrayLength() {
        assertEquals("HOST_PES should have 3 entries",
                3, Constants.HOST_PES.length);
    }

    @Test
    public void testHostRamArrayLength() {
        assertEquals("HOST_RAM should have 3 entries",
                3, Constants.HOST_RAM.length);
    }

    @Test
    public void testVmMipsArrayLength() {
        assertEquals("VM_MIPS should have 5 entries (one per VM type)",
                5, Constants.VM_MIPS.length);
    }

    @Test
    public void testHostTypesMatchPowerModels() {
        assertEquals("HOST_TYPES and HOST_POWER should have same length",
                Constants.HOST_TYPES.length, Constants.HOST_POWER.length);
    }

    @Test
    public void testNumberOfHostsPositive() {
        assertTrue("NUMBER_OF_HOSTS should be positive",
                Constants.NUMBER_OF_HOSTS > 0);
    }

    @Test
    public void testIterationPositive() {
        assertTrue("Iteration count should be positive",
                Constants.Iteration > 0);
    }

    @Test
    public void testInputFolderNotNull() {
        assertNotNull("inputFolder should not be null", Constants.inputFolder);
    }

    @Test
    public void testInputFolderIsRelative() {
        assertFalse("inputFolder should not contain absolute path to /Users/",
                Constants.inputFolder.contains("/Users/"));
        assertFalse("inputFolder should not contain absolute path to C:\\",
                Constants.inputFolder.contains("C:\\"));
    }

    @Test
    public void testSchedulingIntervalPositive() {
        assertTrue("SCHEDULING_INTERVAL should be positive",
                Constants.SCHEDULING_INTERVAL > 0);
    }

    @Test
    public void testOutputTimeBeforeTerminateTime() {
        assertTrue("outputTime should be less than terminateTime",
                Constants.outputTime < Constants.terminateTime);
    }

    @Test
    public void testPsoParametersValid() {
        assertTrue("PSO population size should be positive", Constants.POPULATION_SIZE > 0);
        assertTrue("PSO inertia weight W should be positive", Constants.W > 0);
        assertTrue("PSO acceleration coefficient C should be positive", Constants.C > 0);
    }
}
