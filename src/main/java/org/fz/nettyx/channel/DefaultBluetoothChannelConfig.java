package org.fz.nettyx.channel;

import io.netty.channel.DefaultChannelConfig;
import org.fz.nettyx.endpoint.bluetooth.BluetoothChannelConfig;

import static io.netty.util.internal.ObjectUtil.checkPositiveOrZero;

public class DefaultBluetoothChannelConfig extends DefaultChannelConfig implements BluetoothChannelConfig {

    private static final int DEFAULT_CONNECT_TIMEOUT = 5 * 1000;

    private int connectTimeoutMillis = DEFAULT_CONNECT_TIMEOUT;

    private String address;
    private int channel;
    private boolean authenticate;
    private boolean encrypt;
    private boolean master;

    public DefaultBluetoothChannelConfig(OioBluetoothChannel channel) {

        super(channel);
    }

    @Override
    public String getAddress() {

        return address;
    }

    @Override
    public void setAddress(String address) {

        this.address = address;
    }

    @Override
    public int getChannel() {

        return channel;
    }

    @Override
    public void setChannel(int channel) {

        this.channel = channel;
    }

    @Override
    public boolean isAuthenticate() {

        return authenticate;
    }

    @Override
    public void setAuthenticate(boolean authenticate) {

        this.authenticate = authenticate;
    }

    @Override
    public boolean isEncrypt() {

        return encrypt;
    }

    @Override
    public void setEncrypt(boolean encrypt) {

        this.encrypt = encrypt;
    }

    @Override
    public boolean isMaster() {

        return master;
    }

    @Override
    public void setMaster(boolean master) {

        this.master = master;
    }

    @Override
    public int getConnectTimeoutMillis() {

        return connectTimeoutMillis;
    }

    @Override
    public DefaultBluetoothChannelConfig setConnectTimeoutMillis(int connectTimeoutMillis) {

        checkPositiveOrZero(connectTimeoutMillis, "connectTimeoutMillis");
        this.connectTimeoutMillis = connectTimeoutMillis;
        return this;
    }
}
