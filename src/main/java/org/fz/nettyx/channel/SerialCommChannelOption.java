package org.fz.nettyx.channel;


import io.netty.channel.ChannelOption;
import org.fz.nettyx.endpoint.client.jsc.support.JscChannelConfig.ParityBit;
import org.fz.nettyx.endpoint.client.jsc.support.JscChannelConfig.StopBits;


/**
 * Option for configuring a serial port connection.
 *
 * @param <T>
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2024/3/2 13:29
 */
public final class SerialCommChannelOption<T> extends ChannelOption<T> {

    public static final ChannelOption<Integer>   BAUD_RATE    = valueOf(SerialCommChannelOption.class, "BAUD_RATE");
    public static final ChannelOption<Boolean>   DTR          = valueOf(SerialCommChannelOption.class, "DTR");
    public static final ChannelOption<Boolean>   RTS          = valueOf(SerialCommChannelOption.class, "RTS");
    public static final ChannelOption<StopBits>  STOP_BITS    = valueOf(SerialCommChannelOption.class, "STOP_BITS");
    public static final ChannelOption<Integer>   DATA_BITS    = valueOf(SerialCommChannelOption.class, "DATA_BITS");
    public static final ChannelOption<ParityBit> PARITY_BIT   = valueOf(SerialCommChannelOption.class, "PARITY_BIT");
    public static final ChannelOption<Integer>   WAIT_TIME    = valueOf(SerialCommChannelOption.class, "WAIT_TIME");
    public static final ChannelOption<Integer>   READ_TIMEOUT = valueOf(SerialCommChannelOption.class, "READ_TIMEOUT");

    @SuppressWarnings("deprecation")
    private SerialCommChannelOption() {
        super("jsc-config");
    }
}