package template.bluetooth;

import io.netty.channel.ChannelInitializer;
import org.fz.nettyx.channel.bluetooth.client.BluetoothChannel;
import org.fz.nettyx.template.bluetooth.client.SingleBluetoothChannelClient;
import template.TestChannelInitializer;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/5/30 19:57
 */
public class BluetoothClientTest extends SingleBluetoothChannelClient {
    
    public BluetoothClientTest(String commAddress) {
        super(commAddress);
    }

    @Override
    protected ChannelInitializer<BluetoothChannel> channelInitializer() {
        return new TestChannelInitializer<>();
    }

    public static void main(String[] args) {


    }

}
