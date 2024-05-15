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

import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.*;

import java.util.Map;

import static io.netty.util.internal.ObjectUtil.checkPositiveOrZero;

/**
 * Default configuration class for RXTX device connections.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2024/3/1 14:44
 */

@SuppressWarnings("deprecation")
final class DefaultRxtxChannelConfig extends DefaultChannelConfig implements RxtxChannelConfig {

    private volatile int       baudrate    = 115200;
    private volatile boolean   dtr;
    private volatile boolean  rts;
    private volatile StopBits stopbits = StopBits.STOPBITS_1;
    private volatile DataBits databits = DataBits.DATABITS_8;
    private volatile ParityBit paritybit = ParityBit.NONE;
    private volatile int       waitTime;
    private volatile int       readTimeout = 1000;

    DefaultRxtxChannelConfig(RxtxChannel channel) {
        super(channel);
        setAllocator(new PreferHeapByteBufAllocator(getAllocator()));
    }

    @Override
    public Map<ChannelOption<?>, Object> getOptions() {
        return getOptions(super.getOptions(), RxtxChannelOption.BAUD_RATE, RxtxChannelOption.DTR, RxtxChannelOption.RTS, RxtxChannelOption.STOP_BITS, RxtxChannelOption.DATA_BITS, RxtxChannelOption.PARITY_BIT, RxtxChannelOption.WAIT_TIME);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getOption(ChannelOption<T> option) {
        if (option == RxtxChannelOption.BAUD_RATE) {
            return (T) Integer.valueOf(getBaudRate());
        }
        if (option == RxtxChannelOption.DTR) {
            return (T) Boolean.valueOf(isDtr());
        }
        if (option == RxtxChannelOption.RTS) {
            return (T) Boolean.valueOf(isRts());
        }
        if (option == RxtxChannelOption.STOP_BITS) {
            return (T) getStopBits();
        }
        if (option == RxtxChannelOption.DATA_BITS) {
            return (T) getDataBits();
        }
        if (option == RxtxChannelOption.PARITY_BIT) {
            return (T) getParityBit();
        }
        if (option == RxtxChannelOption.WAIT_TIME) {
            return (T) Integer.valueOf(getWaitTimeMillis());
        }
        if (option == RxtxChannelOption.READ_TIMEOUT) {
            return (T) Integer.valueOf(getReadTimeout());
        }
        return super.getOption(option);
    }

    @Override
    public <T> boolean setOption(ChannelOption<T> option, T value) {
        validate(option, value);

        if (option == RxtxChannelOption.BAUD_RATE) {
            setBaudRate((Integer) value);
        } else if (option == RxtxChannelOption.DTR) {
            setDtr((Boolean) value);
        } else if (option == RxtxChannelOption.RTS) {
            setRts((Boolean) value);
        } else if (option == RxtxChannelOption.STOP_BITS) {
            setStopBits((StopBits) value);
        } else if (option == RxtxChannelOption.DATA_BITS) {
            setDataBits((DataBits) value);
        } else if (option == RxtxChannelOption.PARITY_BIT) {
            setParityBit((ParityBit) value);
        } else if (option == RxtxChannelOption.WAIT_TIME) {
            setWaitTimeMillis((Integer) value);
        } else if (option == RxtxChannelOption.READ_TIMEOUT) {
            setReadTimeout((Integer) value);
        } else {
            return super.setOption(option, value);
        }
        return true;
    }

    @Override
    public RxtxChannelConfig setBaudRate(final int baudrate) {
        this.baudrate = baudrate;
        return this;
    }

    @Override
    public RxtxChannelConfig setStopBits(final StopBits stopbits) {
        this.stopbits = stopbits;
        return this;
    }

    @Override
    public RxtxChannelConfig setDataBits(final DataBits databits) {
        this.databits = databits;
        return this;
    }

    @Override
    public RxtxChannelConfig setParityBit(final ParityBit paritybit) {
        this.paritybit = paritybit;
        return this;
    }

    @Override
    public int getBaudRate() {
        return baudrate;
    }

    @Override
    public StopBits getStopBits() {
        return stopbits;
    }

    @Override
    public DataBits getDataBits() {
        return databits;
    }

    @Override
    public ParityBit getParityBit() {
        return paritybit;
    }

    @Override
    public boolean isDtr() {
        return dtr;
    }

    @Override
    public RxtxChannelConfig setDtr(final boolean dtr) {
        this.dtr = dtr;
        return this;
    }

    @Override
    public boolean isRts() {
        return rts;
    }

    @Override
    public RxtxChannelConfig setRts(final boolean rts) {
        this.rts = rts;
        return this;
    }

    @Override
    public int getWaitTimeMillis() {
        return waitTime;
    }

    @Override
    public RxtxChannelConfig setWaitTimeMillis(final int waitTimeMillis) {
        this.waitTime = checkPositiveOrZero(waitTimeMillis, "waitTimeMillis");
        return this;
    }

    @Override
    public RxtxChannelConfig setReadTimeout(int readTimeout) {
        this.readTimeout = checkPositiveOrZero(readTimeout, "readTimeout");
        return this;
    }

    @Override
    public int getReadTimeout() {
        return readTimeout;
    }

    @Override
    public RxtxChannelConfig setConnectTimeoutMillis(int connectTimeoutMillis) {
        super.setConnectTimeoutMillis(connectTimeoutMillis);
        return this;
    }

    @Override
    public RxtxChannelConfig setMaxMessagesPerRead(int maxMessagesPerRead) {
        super.setMaxMessagesPerRead(maxMessagesPerRead);
        return this;
    }

    @Override
    public RxtxChannelConfig setWriteSpinCount(int writeSpinCount) {
        super.setWriteSpinCount(writeSpinCount);
        return this;
    }

    @Override
    public RxtxChannelConfig setAllocator(ByteBufAllocator allocator) {
        super.setAllocator(allocator);
        return this;
    }

    @Override
    public RxtxChannelConfig setRecvByteBufAllocator(RecvByteBufAllocator allocator) {
        super.setRecvByteBufAllocator(allocator);
        return this;
    }

    @Override
    public RxtxChannelConfig setAutoRead(boolean autoRead) {
        super.setAutoRead(autoRead);
        return this;
    }

    @Override
    public RxtxChannelConfig setAutoClose(boolean autoClose) {
        super.setAutoClose(autoClose);
        return this;
    }

    @Override
    public RxtxChannelConfig setWriteBufferHighWaterMark(int writeBufferHighWaterMark) {
        super.setWriteBufferHighWaterMark(writeBufferHighWaterMark);
        return this;
    }

    @Override
    public RxtxChannelConfig setWriteBufferLowWaterMark(int writeBufferLowWaterMark) {
        super.setWriteBufferLowWaterMark(writeBufferLowWaterMark);
        return this;
    }

    @Override
    public RxtxChannelConfig setWriteBufferWaterMark(WriteBufferWaterMark writeBufferWaterMark) {
        super.setWriteBufferWaterMark(writeBufferWaterMark);
        return this;
    }

    @Override
    public RxtxChannelConfig setMessageSizeEstimator(MessageSizeEstimator estimator) {
        super.setMessageSizeEstimator(estimator);
        return this;
    }
}
