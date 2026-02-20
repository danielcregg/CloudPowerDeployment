package newcloud;

import newcloud.PowerModel.PowerModelDL360G7;
import newcloud.PowerModel.PowerModelDL360Gen9;
import newcloud.PowerModel.PowerModelML110G5;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests for the power model classes.
 */
public class PowerModelTest {

    @Test
    public void testML110G5IdlePower() {
        PowerModelML110G5 model = new PowerModelML110G5();
        // At 0% utilization, power should be the idle power
        try {
            double idlePower = model.getPower(0.0);
            assertTrue("Idle power should be positive", idlePower > 0);
            // ML110G5 idle power is 93.7W
            assertEquals(93.7, idlePower, 1.0);
        } catch (Exception e) {
            fail("getPower should not throw: " + e.getMessage());
        }
    }

    @Test
    public void testDL360G7IdlePower() {
        PowerModelDL360G7 model = new PowerModelDL360G7();
        try {
            double idlePower = model.getPower(0.0);
            assertTrue("Idle power should be positive", idlePower > 0);
            assertEquals(54.6, idlePower, 1.0);
        } catch (Exception e) {
            fail("getPower should not throw: " + e.getMessage());
        }
    }

    @Test
    public void testDL360Gen9IdlePower() {
        PowerModelDL360Gen9 model = new PowerModelDL360Gen9();
        try {
            double idlePower = model.getPower(0.0);
            assertTrue("Idle power should be positive", idlePower > 0);
            assertEquals(45.0, idlePower, 1.0);
        } catch (Exception e) {
            fail("getPower should not throw: " + e.getMessage());
        }
    }

    @Test
    public void testPowerIncreasesWithUtilization() {
        PowerModelDL360G7 model = new PowerModelDL360G7();
        try {
            double lowPower = model.getPower(0.1);
            double highPower = model.getPower(0.9);
            assertTrue("Power at 90% should be greater than at 10%",
                    highPower > lowPower);
        } catch (Exception e) {
            fail("getPower should not throw: " + e.getMessage());
        }
    }

    @Test
    public void testModelNames() {
        assertEquals("G5", new PowerModelML110G5().getName());
        assertEquals("G7", new PowerModelDL360G7().getName());
        assertEquals("G9", new PowerModelDL360Gen9().getName());
    }
}
