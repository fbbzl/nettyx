package org.fz.nettyx.channel.serial.jsc;


import io.netty.channel.ChannelOption;

/**
 * java serial comm channel options
 *
 * @param <T>
 * @author fengbinbin
 * @version 1.0
 * @since 2024/3/2 13:29
 */
public final class JscChannelOption<T> extends ChannelOption<T> {

    public static final ChannelOption<Integer>                    BAUD_RATE    = option("BAUD_RATE");
    public static final ChannelOption<Boolean>                    DTR          = option("DTR");
    public static final ChannelOption<Boolean>                    RTS          = option("RTS");
    public static final ChannelOption<JscChannelConfig.StopBits>  STOP_BITS    = option("STOP_BITS");
    public static final ChannelOption<JscChannelConfig.DataBits>  DATA_BITS    = option("DATA_BITS");
    public static final ChannelOption<JscChannelConfig.ParityBit> PARITY_BIT   = option("PARITY_BIT");
    public static final ChannelOption<Integer>                    READ_TIMEOUT = option("READ_TIMEOUT");

    private static <O> ChannelOption<O> option(String secondNameComponent)
    {
        return valueOf(JscChannelOption.class, secondNameComponent);
    }

    @SuppressWarnings("deprecation")
    private JscChannelOption()
    {
        super("jsc-config");
    }
}