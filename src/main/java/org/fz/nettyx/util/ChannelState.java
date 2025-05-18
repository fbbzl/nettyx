package org.fz.nettyx.util;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.util.AttributeKey;
import lombok.Data;

/**
 * to save connect state history
 *
 * @author fengbinbin
 * @since 2021 -12-29 18:46
 */
@Data
public class ChannelState {

    public static final AttributeKey<ChannelState> CHANNEL_STATE_KEY = AttributeKey.valueOf("__$channel_state_key$");

    /**
     * total number of connection times
     */
    private long connectTimes;
    /**
     * the number of successful connection times
     */
    private long connectSuccessTimes;
    /**
     * the number of connection failure times
     */
    private long connectFailureTimes;
    /**
     * the number of times the connection was done
     */
    private long connectDoneTimes;
    /**
     * the number of times the connection was canceled
     */
    private long connectCancelledTimes;

    public static ChannelState getChannelState(ChannelFuture cf)
    {
        return getChannelState(cf.channel());
    }

    public static ChannelState getChannelState(Channel chl)
    {
        return chl.hasAttr(CHANNEL_STATE_KEY) ? chl.attr(CHANNEL_STATE_KEY).get() : null;
    }

    public void increase(ChannelFuture cf)
    {
        this.connectTimes++;

        if (cf.isSuccess())     connectSuccessTimes++;
        if (cf.cause() != null) connectFailureTimes++;
        if (cf.isDone())        connectDoneTimes++;
        if (cf.isCancelled())   connectCancelledTimes++;
    }

    public void reset()
    {
        this.connectTimes          = 0;

        this.connectSuccessTimes   = 0;
        this.connectFailureTimes   = 0;
        this.connectDoneTimes      = 0;
        this.connectCancelledTimes = 0;
    }

}