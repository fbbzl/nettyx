/*
 * Copyright 2013 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package org.fz.nettyx.channel.rxtx;

import io.netty.channel.ChannelOption;
import org.fz.nettyx.channel.rxtx.RxtxChannelConfig.DataBits;
import org.fz.nettyx.channel.rxtx.RxtxChannelConfig.ParityBit;
import org.fz.nettyx.channel.rxtx.RxtxChannelConfig.StopBits;

/**
 * Option for configuring a serial port connection
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2024/3/1 14:44
 */
public final class RxtxChannelOption<T> extends ChannelOption<T> {

    public static final ChannelOption<Integer>   BAUD_RATE    = option("BAUD_RATE");
    public static final ChannelOption<Boolean>   DTR          = option("DTR");
    public static final ChannelOption<Boolean>   RTS          = option("RTS");
    public static final ChannelOption<StopBits>  STOP_BITS    = option("STOP_BITS");
    public static final ChannelOption<DataBits>  DATA_BITS    = option("DATA_BITS");
    public static final ChannelOption<ParityBit> PARITY_BIT   = option("PARITY_BIT");
    public static final ChannelOption<Integer>   READ_TIMEOUT = option("READ_TIMEOUT");

    private static <O> ChannelOption<O> option(String secondNameComponent) {
        return valueOf(RxtxChannelOption.class, secondNameComponent);
    }

    @SuppressWarnings("deprecation")
    private RxtxChannelOption() {
        super("rxtx-config");
    }
}
