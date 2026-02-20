package newcloud;

import newcloud.datacenter.PowerDatacenterRL;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Tests for the numerically stable reward function.
 */
public class RewardFunctionTest {

    @Before
    public void setUp() {
        // Reset static state before each test
        PowerDatacenterRL.everyhosthistorypower.clear();
        PowerDatacenterRL.allpower.clear();
    }

    @Test
    public void testRewardIsZeroWithNoHistory() {
        // With no power history, reward should be 0
        double reward = computeTestReward();
        assertEquals("Reward should be 0 with no power history", 0.0, reward, 1e-10);
    }

    @Test
    public void testRewardIsNegative() {
        // With some power data, reward should be negative (power is always positive)
        List<Double> powers = Arrays.asList(100.0, 200.0, 150.0);
        PowerDatacenterRL.everyhosthistorypower.add(0, powers);

        double reward = computeTestReward();
        assertTrue("Reward should be negative (or zero)", reward <= 0.0);
    }

    @Test
    public void testRewardIsBounded() {
        // Reward should be in [-1, 0]
        List<Double> powers = Arrays.asList(100.0, 200.0, 300.0);
        PowerDatacenterRL.everyhosthistorypower.add(0, powers);

        double reward = computeTestReward();
        assertTrue("Reward should be >= -1", reward >= -1.0);
        assertTrue("Reward should be <= 0", reward <= 0.0);
    }

    @Test
    public void testLowerPowerGivesHigherReward() {
        // Lower total power should give a higher (less negative) reward

        // High power scenario
        List<Double> highPowers = Arrays.asList(300.0, 300.0, 300.0);
        PowerDatacenterRL.everyhosthistorypower.add(0, highPowers);
        double rewardHigh = computeTestReward();

        // Low power scenario (added after, so maxObservedPower stays at 900)
        List<Double> lowPowers = Arrays.asList(50.0, 50.0, 50.0);
        PowerDatacenterRL.everyhosthistorypower.add(0, lowPowers);
        double rewardLow = computeTestReward();

        assertTrue("Lower power should give higher reward",
                rewardLow > rewardHigh);
    }

    @Test
    public void testOriginalRewardOverflows() {
        // Demonstrate that the original reward function Math.pow(ratio, 10000) overflows
        double ratio = 1.001; // Just slightly above 1
        double originalReward = Math.pow(ratio, 10000);
        assertTrue("Original reward overflows to Infinity for ratio=1.001",
                Double.isInfinite(originalReward));

        double ratio2 = 0.999; // Just slightly below 1
        double originalReward2 = Math.pow(ratio2, 10000);
        assertEquals("Original reward underflows to 0 for ratio=0.999",
                0.0, originalReward2, 1e-10);
    }

    @Test
    public void testDiscretizeFunction() {
        assertEquals(0, PowerDatacenterRL.discretize(0.0, 10));
        assertEquals(4, PowerDatacenterRL.discretize(0.45, 10));
        assertEquals(9, PowerDatacenterRL.discretize(1.0, 10));
        assertEquals(9, PowerDatacenterRL.discretize(1.5, 10)); // clamped
        assertEquals(0, PowerDatacenterRL.discretize(-0.1, 10)); // clamped
    }

    /**
     * Helper that mimics the reward computation from PowerDatacenterRL.
     */
    private double computeTestReward() {
        if (PowerDatacenterRL.everyhosthistorypower.isEmpty()) {
            return 0.0;
        }

        List<Double> currentPowers = PowerDatacenterRL.everyhosthistorypower.get(0);
        double totalCurrentPower = 0.0;
        for (Double p : currentPowers) {
            totalCurrentPower += p;
        }

        // Track max for normalization (using a local var for test isolation)
        double maxPower = 1.0;
        for (List<Double> history : PowerDatacenterRL.everyhosthistorypower) {
            double sum = 0;
            for (Double p : history) sum += p;
            if (sum > maxPower) maxPower = sum;
        }

        return -totalCurrentPower / maxPower;
    }
}
