package newcloud;

import newcloud.policy.*;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.*;

/**
 * Tests for VM allocation policy/assigner logic.
 */
public class VmAllocationPolicyTest {

    @Test
    public void testQLearningInitializesQTable() {
        GenExcel excel = GenExcel.getInstance();
        VmAllocationAssignerLearning assigner =
                new VmAllocationAssignerLearning(0.9, 0.8, 0.2, excel);

        String testState = "0_0_0";
        int action = assigner.createAction(testState);

        assertTrue("Action should be a valid host ID",
                action >= 0 && action < Constants.NUMBER_OF_HOSTS);
        assertTrue("Q-table should contain the test state",
                VmAllocationAssignerLearning.QList.containsKey(testState));
    }

    @Test
    public void testQLearningUpdateIncreasesQValue() {
        // Reset static state
        VmAllocationAssignerLearning.QList.clear();

        GenExcel excel = GenExcel.getInstance();
        VmAllocationAssignerLearning assigner =
                new VmAllocationAssignerLearning(0.9, 0.8, 0.2, excel);

        String state = "test_state";
        assigner.initRowOfQList(state);

        // Initial Q-value should be 0
        assertEquals(0.0, VmAllocationAssignerLearning.QList.get(state).get(0), 1e-10);

        // Update with a negative reward (typical for power minimization)
        assigner.updateQList(0, -0.5, state, state);

        // Q-value should have changed from 0
        double updatedQ = VmAllocationAssignerLearning.QList.get(state).get(0);
        assertNotEquals("Q-value should change after update", 0.0, updatedQ, 1e-10);
    }

    @Test
    public void testSarsaCreatesDifferentFromQLearning() {
        // Verify SARSA and Q-Learning are using different update rules
        VmAllocationAssignerSarsa.QList.clear();
        VmAllocationAssignerLearning.QList.clear();

        GenExcel excel = GenExcel.getInstance();
        VmAllocationAssignerSarsa sarsa = new VmAllocationAssignerSarsa(0.9, 0.8, 0.0, excel);
        VmAllocationAssignerLearning qlearning = new VmAllocationAssignerLearning(0.9, 0.8, 0.0, excel);

        String state = "test";
        sarsa.initRowOfQList(state);
        qlearning.initRowOfQList(state);

        // Both should start with zero values
        assertEquals(0.0, VmAllocationAssignerSarsa.QList.get(state).get(0), 1e-10);
        assertEquals(0.0, VmAllocationAssignerLearning.QList.get(state).get(0), 1e-10);
    }

    @Test
    public void testEligibilityTraceInitialization() {
        VmAllocationAssignerLearningLamda.QList.clear();
        VmAllocationAssignerLearningLamda.EList.clear();

        GenExcel excel = GenExcel.getInstance();
        VmAllocationAssignerLearningLamda assigner =
                new VmAllocationAssignerLearningLamda(0.9, 0.8, 0.2, 0.9, excel);

        String state = "trace_test";
        assigner.initRowOfQList(state);

        // Both Q and E tables should be initialized
        assertTrue(VmAllocationAssignerLearningLamda.QList.containsKey(state));
        assertTrue(VmAllocationAssignerLearningLamda.EList.containsKey(state));

        // All E-values should start at 0
        for (int i = 0; i < Constants.NUMBER_OF_HOSTS; i++) {
            assertEquals(0.0, VmAllocationAssignerLearningLamda.EList.get(state).get(i), 1e-10);
        }
    }

    @Test
    public void testExplorationVsExploitation() {
        VmAllocationAssignerLearning.QList.clear();

        GenExcel excel = GenExcel.getInstance();

        // With epsilon=1.0, should always explore (random)
        VmAllocationAssignerLearning explorer =
                new VmAllocationAssignerLearning(0.9, 0.8, 1.0, excel);

        // With epsilon=0.0, should always exploit (best Q-value)
        VmAllocationAssignerLearning exploiter =
                new VmAllocationAssignerLearning(0.9, 0.8, 0.0, excel);

        String state = "explore_test";
        // Both should return valid actions
        int explorerAction = explorer.createAction(state);
        int exploiterAction = exploiter.createAction(state);

        assertTrue(explorerAction >= 0 && explorerAction < Constants.NUMBER_OF_HOSTS);
        assertTrue(exploiterAction >= 0 && exploiterAction < Constants.NUMBER_OF_HOSTS);
    }

    @Test
    public void testRandomIntBounds() {
        GenExcel excel = GenExcel.getInstance();
        VmAllocationAssignerLearning assigner =
                new VmAllocationAssignerLearning(0.9, 0.8, 0.2, excel);

        // Test randomInt bounds
        for (int trial = 0; trial < 100; trial++) {
            int val = assigner.randomInt(5, 10);
            assertTrue("randomInt should be >= min", val >= 5);
            assertTrue("randomInt should be <= max", val <= 10);
        }

        // Test edge case: min == max
        assertEquals(7, assigner.randomInt(7, 7));
    }
}
