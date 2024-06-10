package org.fz.nettyx.channel;

import io.netty.channel.ChannelFuture;
import lombok.Data;

/**
 * to save connect state history
 *
 * @author fengbinbin
 * @since 2021 -12-29 18:46
 */
@Data
public class ChannelState {

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

    public void increase(ChannelFuture cf) {
        this.connectTimes++;

        if (cf.isSuccess())   connectSuccessTimes++;
        if (!cf.isSuccess())  connectFailureTimes++;
        if (cf.isDone())      connectDoneTimes++;
        if (cf.isCancelled()) connectCancelledTimes++;
    }

    public void reset() {
        this.connectTimes          = 0;

        this.connectSuccessTimes   = 0;
        this.connectFailureTimes   = 0;
        this.connectDoneTimes      = 0;
        this.connectCancelledTimes = 0;
    }

}