package org.fz.nettyx.endpoint.serial.jsc.support;


import io.netty.channel.ChannelOption;

/**
 * Option for configuring a serial port connection.
 *
 * @param <T>
 * @author fengbinbin
 * @version 1.0
 * @since 2024/3/2 13:29
 */
public final class JscChannelOption<T> extends ChannelOption<T> {

    public static final ChannelOption<Integer>   BAUD_RATE    = valueOf(JscChannelOption.class, "BAUD_RATE");
    public static final ChannelOption<Boolean>   DTR          = valueOf(JscChannelOption.class, "DTR");
    public static final ChannelOption<Boolean>                    RTS        = valueOf(JscChannelOption.class, "RTS");
    public static final ChannelOption<JscChannelConfig.StopBits>  STOP_BITS  = valueOf(JscChannelOption.class, "STOP_BITS");
    public static final ChannelOption<JscChannelConfig.DataBits>  DATA_BITS  = valueOf(JscChannelOption.class, "DATA_BITS");
    public static final ChannelOption<JscChannelConfig.ParityBit> PARITY_BIT = valueOf(JscChannelOption.class, "PARITY_BIT");
    public static final ChannelOption<Integer>                    WAIT_TIME  = valueOf(JscChannelOption.class, "WAIT_TIME");
    public static final ChannelOption<Integer>   READ_TIMEOUT = valueOf(JscChannelOption.class, "READ_TIMEOUT");

    @SuppressWarnings("deprecation")
    private JscChannelOption() {
        super("jsc-config");
    }
}