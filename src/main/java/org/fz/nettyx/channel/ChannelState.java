package org.fz.nettyx.channel;

/**
 * to save connect state history
 *
 * @author fengbinbin
 * @since 2021 -12-29 18:46
 */
public class ChannelState {
    private int connectTimes;
    private int connectSuccessTimes;
    private int connectFailureTimes;
    private int connectDoneTimes;
    private int connectCancelTimes;
}
