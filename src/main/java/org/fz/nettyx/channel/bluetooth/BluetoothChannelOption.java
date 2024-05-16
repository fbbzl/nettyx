package org.fz.nettyx.channel.bluetooth;

import io.netty.channel.ChannelOption;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/5/15 15:18
 */
public class BluetoothChannelOption<T> extends ChannelOption<T> {




    private static <O> ChannelOption<O> option(String secondNameComponent) {
        return valueOf(BluetoothChannelOption.class, secondNameComponent);
    }

    @SuppressWarnings("deprecation")
    private BluetoothChannelOption() {
        super("bluetooth-config");
    }
}
