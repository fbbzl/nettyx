package org.fz.nettyx.channel;

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
     * total number of connections
     */
    private int connectTimes;
    /**
     * the number of successful connections
     */
    private int connectSuccessTimes;
    /**
     * the number of connection failures
     */
    private int connectFailureTimes;
    /**
     * the number of times the connection was completed
     */
    private int connectDoneTimes;
    /**
     * the number of times the connection was canceled
     */
    private int connectCancelTimes;
}
