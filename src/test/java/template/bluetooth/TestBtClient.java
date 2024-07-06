package template.bluetooth;

import cn.hutool.core.lang.Console;
import cn.hutool.core.util.StrUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import org.fz.nettyx.channel.bluetooth.client.BtChannel;
import org.fz.nettyx.listener.ActionChannelFutureListener;
import org.fz.nettyx.template.bluetooth.client.SingleBtChannelTemplate;
import org.fz.nettyx.util.BtFinder;
import template.TestChannelInitializer;

import javax.bluetooth.RemoteDevice;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/5/30 19:57
 */
public class TestBtClient extends SingleBtChannelTemplate {
    static ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
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
            System.err.println(device.getBluetoothAddress());
            System.err.println(device.getFriendlyName(false));
        }

        String       url = StrUtil.format("btspp://{}:1;authenticate=false;encrypt=false;master=false", devices.get(0).getBluetoothAddress());

        TestBtClient client = new TestBtClient(url);

        ChannelFutureListener listener = new ActionChannelFutureListener()
                .whenSuccess((l, cf) -> {
                    executor.scheduleAtFixedRate(() -> {
                        byte[] msg = new byte[300];
                        Arrays.fill(msg, (byte) 67);
                        client.writeAndFlush(Unpooled.wrappedBuffer(msg));
                    }, 2, 30, TimeUnit.MILLISECONDS);
                })
                .whenCancelled((l, cf) -> Console.log("cancel"))
                .whenDone((l, cf) -> Console.log("done"));

        client.connect().addListener(listener);

    }

}
