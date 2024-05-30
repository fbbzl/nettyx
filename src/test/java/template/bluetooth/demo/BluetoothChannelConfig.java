package template.bluetooth.demo;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/5/30 21:20
 */
import io.netty.channel.ChannelConfig;

interface BluetoothChannelConfig extends ChannelConfig {

    String getAddress();

    public void setAddress(String address);

    int getChannel();

    void setChannel(int channel);

    boolean isAuthenticate();

    void setAuthenticate(boolean authenticate);

    boolean isEncrypt();

    void setEncrypt(boolean encrypt);

    boolean isMaster();

    void setMaster(boolean master);
}