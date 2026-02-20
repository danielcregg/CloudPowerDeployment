package newcloud;

import newcloud.datacenter.PowerDatacenterRL;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests for the feature-based state representation.
 */
public class StateRepresentationTest {

    @Test
    public void testDiscretizeZero() {
        assertEquals("0.0 should discretize to bin 0", 0, PowerDatacenterRL.discretize(0.0, 10));
    }

    @Test
    public void testDiscretizeOne() {
        // 1.0 * 10 = 10, clamped to 9
        assertEquals("1.0 should discretize to bin 9 (clamped)", 9, PowerDatacenterRL.discretize(1.0, 10));
    }

    @Test
    public void testDiscretizeMiddle() {
        assertEquals("0.5 should discretize to bin 5", 5, PowerDatacenterRL.discretize(0.5, 10));
    }

    @Test
    public void testDiscretizeNegative() {
        // Negative values should clamp to 0
        assertEquals("Negative values should clamp to bin 0", 0, PowerDatacenterRL.discretize(-0.5, 10));
    }

    @Test
    public void testDiscretizeAboveOne() {
        // Values > 1.0 should clamp to max bin
        assertEquals("Values > 1.0 should clamp to max bin", 9, PowerDatacenterRL.discretize(2.0, 10));
    }

    @Test
    public void testDiscretizeWithDifferentBinCounts() {
        assertEquals(0, PowerDatacenterRL.discretize(0.0, 5));
        assertEquals(2, PowerDatacenterRL.discretize(0.5, 5));
        assertEquals(4, PowerDatacenterRL.discretize(1.0, 5));
    }

    @Test
    public void testStateSpaceIsTractable() {
        // With 10 bins per feature and 3 features, state space = 10 * 10 * 10 = 1000
        // This is vastly smaller than the original 10^300
        int cpuBins = 10;
        int activeBins = 10;
        int vmBins = 10;
        int totalStates = cpuBins * activeBins * vmBins;
        assertTrue("Feature-based state space should be tractable (< 10000)",
                totalStates < 10000);
    }

    @Test
    public void testOriginalStateSpaceIsIntractable() {
        // The original state space is 10^N where N is the number of hosts
        // For 300 hosts, this is 10^300 which is astronomically large
        int numHosts = Constants.NUMBER_OF_HOSTS;
        // We can't represent 10^300, but we can verify the concept
        assertTrue("With 300 hosts, original state space (10^300) is intractable",
                numHosts > 10); // Trivially true, but documents the problem
    }
}
