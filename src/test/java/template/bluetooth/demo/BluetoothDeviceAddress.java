package template.bluetooth.demo;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/5/30 21:19
 */
import java.net.SocketAddress;

public class BluetoothDeviceAddress extends SocketAddress {

    private final String value;

    public BluetoothDeviceAddress(String value) {

        this.value = value;
    }

    public String value() {
        return value;
    }

    @Override
    public String toString() {

        return value;
    }
}