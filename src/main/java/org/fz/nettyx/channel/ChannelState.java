package org.fz.nettyx.channel;

/**
 * to save connect state history
 */
public class ChannelState {
    private int connectTimes;
    private int connectSuccessTimes;
    private int connectFailureTimes;
    private int connectDoneTimes;
    private int connectCancelTimes;
}
