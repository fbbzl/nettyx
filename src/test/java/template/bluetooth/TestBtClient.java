package template.bluetooth;

import cn.hutool.core.util.StrUtil;
import io.netty.channel.ChannelInitializer;
import org.fz.nettyx.channel.bluetooth.client.BtChannel;
import org.fz.nettyx.template.bluetooth.client.SingleBtChannelTemplate;
import org.fz.nettyx.util.BtFinder;
import template.DebugChannelListener;
import template.TestChannelInitializer;

import javax.bluetooth.RemoteDevice;
import java.util.List;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/5/30 19:57
 */
public class TestBtClient extends SingleBtChannelTemplate {

    public TestBtClient(String address) {
        super(address);
    }

    @Override
    protected ChannelInitializer<BtChannel> channelInitializer() {
        return new TestChannelInitializer<>();
    }

    public static void main(String[] args) throws Exception {
        List<RemoteDevice> devices = new BtFinder.DeviceFinder().getDevices();
        for (RemoteDevice device : devices) {
            String       url = StrUtil.format("btspp://{}:1;authenticate=false;encrypt=false;master=false", device.getBluetoothAddress());

            TestBtClient client = new TestBtClient(url);

            client.connect().addListener(new DebugChannelListener());
        }

    }

}
