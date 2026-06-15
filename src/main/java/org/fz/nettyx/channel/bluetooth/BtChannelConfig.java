package org.fz.nettyx.channel.bluetooth;

import io.netty.channel.ChannelConfig;
import io.netty.channel.DefaultChannelConfig;
import lombok.Getter;
import lombok.Setter;
import org.fz.nettyx.channel.bluetooth.client.BtChannel;

import static io.netty.util.internal.ObjectUtil.checkPositiveOrZero;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/5/15 17:29
 */

public interface BtChannelConfig extends ChannelConfig {

    String getAddress();

    public void setAddress(String address);

    int getChannelNum();

    void setChannelNum(int channel);

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
    @Setter
    @Getter
    class DefaultBtChannelConfig extends DefaultChannelConfig implements BtChannelConfig {

        private static final int DEFAULT_CONNECT_TIMEOUT = 5 * 1000;

        private int connectTimeoutMillis = DEFAULT_CONNECT_TIMEOUT;

        private String  address;
        private int     channelNum;
        private boolean authenticate;
        private boolean encrypt;
        private boolean master;

        public DefaultBtChannelConfig(BtChannel channel)
        {
            super(channel);
        }

        @Override
        public int getConnectTimeoutMillis()
        {
            return connectTimeoutMillis;
        }

        @Override
        public DefaultBtChannelConfig setConnectTimeoutMillis(int connectTimeoutMillis)
        {
            checkPositiveOrZero(connectTimeoutMillis, "connectTimeoutMillis");
            this.connectTimeoutMillis = connectTimeoutMillis;
            return this;
        }
    }
}
