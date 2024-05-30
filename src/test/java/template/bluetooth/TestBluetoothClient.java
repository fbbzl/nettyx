package template.bluetooth;

import io.netty.channel.ChannelInitializer;
import org.fz.nettyx.channel.bluetooth.client.BluetoothChannel;
import org.fz.nettyx.template.bluetooth.client.SingleBtChannelTemplate;
import org.fz.nettyx.util.BluetoothFinder;
import template.DebugChannelListener;
import template.TestChannelInitializer;

import javax.bluetooth.RemoteDevice;
import java.util.List;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/5/30 19:57
 */
public class TestBluetoothClient extends SingleBtChannelTemplate {

    public TestBluetoothClient(String commAddress) {
        super(commAddress);
    }

    @Override
    protected ChannelInitializer<BluetoothChannel> channelInitializer() {
        return new TestChannelInitializer<>();
    }

    public static void main(String[] args) throws Exception {
        List<RemoteDevice> devices = new BluetoothFinder.DeviceFinder().getDevices();
        for (RemoteDevice device : devices) {
            System.err.println(device.getFriendlyName(false));
            System.err.println(device.getBluetoothAddress());
        }

        RemoteDevice iphone = devices.get(0);
        TestBluetoothClient client = new TestBluetoothClient(iphone.getBluetoothAddress());

        client.connect().addListener(new DebugChannelListener());
    }

}
