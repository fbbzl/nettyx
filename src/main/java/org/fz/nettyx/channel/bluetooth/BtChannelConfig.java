package org.fz.nettyx.channel.bluetooth;

import io.netty.channel.ChannelConfig;
import io.netty.channel.DefaultChannelConfig;
import org.fz.nettyx.channel.bluetooth.client.BtChannel;

import static io.netty.util.internal.ObjectUtil.checkPositiveOrZero;

public interface BtChannelConfig extends ChannelConfig {

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

    /**
     * @author fengbinbin
     * @version 1.0
     * @since 2024/5/16 23:06
     */
    class DefaultBluetoothChannelConfig extends DefaultChannelConfig implements BtChannelConfig {

        private static final int DEFAULT_CONNECT_TIMEOUT = 5 * 1000;

        private int connectTimeoutMillis = DEFAULT_CONNECT_TIMEOUT;

        private String  address;
        private int     channel;
        private boolean authenticate;
        private boolean encrypt;
        private boolean master;

        public DefaultBluetoothChannelConfig(BtChannel channel) {
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
}
