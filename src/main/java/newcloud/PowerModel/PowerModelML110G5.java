package newcloud.PowerModel;

import org.cloudbus.cloudsim.power.models.PowerModelSpecPower;

/**
 * Power model for the HP ProLiant ML110 G5 server.
 * <p>
 * Power consumption data (in Watts) at 0%, 10%, ..., 100% CPU utilization.
 * </p>
 */
public class PowerModelML110G5 extends PowerModelSpecPower {

    private final double[] power = new double[]{
            93.7D, 97.0D, 101.0D, 105.0D, 110.0D,
            116.0D, 121.0D, 125.0D, 129.0D, 133.0D, 135.0D
    };

    public String getName() {
        return "G5";
    }

    public PowerModelML110G5() {
    }

    @Override
    protected double getPowerData(int index) {
        return this.power[index];
    }
}
