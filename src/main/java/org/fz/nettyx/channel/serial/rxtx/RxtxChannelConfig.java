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
package org.fz.nettyx.channel.serial.rxtx;

import io.netty.channel.ChannelOption;
import org.fz.nettyx.channel.serial.*;

import java.util.Map;

/**
 * A configuration class for RXTX device connections.
 *
 * <h3>Available options</h3>
 * <p>
 * In addition to the options provided by {@link io.netty.channel.ChannelConfig},
 * {@link DefaultRxtxChannelConfig} allows the following options in the option map:
 *
 * <table border="1" cellspacing="0" cellpadding="6">
 * <tr>
 * <th>Name</th><th>Associated setter method</th>
 * </tr><tr>
 * <td>{@link RxtxChannelOption#BAUD_RATE}</td><td>{@link #setBaudRate(int)}</td>
 * </tr><tr>
 * <td>{@link RxtxChannelOption#DTR}</td><td>{@link #setDtr(boolean)}</td>
 * </tr><tr>
 * <td>{@link RxtxChannelOption#RTS}</td><td>{@link #setRts(boolean)}</td>
 * </tr><tr>
 * <td>{@link RxtxChannelOption#STOP_BITS}</td><td>{@link #setStopBits(SerialStopBits)}</td>
 * </tr><tr>
 * <td>{@link RxtxChannelOption#DATA_BITS}</td><td>{@link #setDataBits(SerialDataBits)}</td>
 * </tr><tr>
 * <td>{@link RxtxChannelOption#PARITY_BIT}</td><td>{@link #setParityBit(SerialParityBit)}</td>
 * </tr>
 * </table>
 *
 * rxtx channel config
 * @author fengbinbin
 * @version 1.0
 * @since 2024/3/1 14:44
 */
public interface RxtxChannelConfig extends SerialChannelConfig {

    @Override
    RxtxChannelConfig setBaudRate(int baudrate);

    @Override
    RxtxChannelConfig setStopBits(SerialStopBits stopbits);

    @Override
    RxtxChannelConfig setDataBits(SerialDataBits databits);

    @Override
    RxtxChannelConfig setParityBit(SerialParityBit paritybit);

    @Override
    RxtxChannelConfig setDtr(boolean dtr);

    @Override
    RxtxChannelConfig setRts(boolean rts);

    @Override
    RxtxChannelConfig setReadTimeout(int readTimeout);

    @Override
    RxtxChannelConfig setConnectTimeoutMillis(int connectTimeoutMillis);

    @Override
    RxtxChannelConfig setMaxMessagesPerRead(int maxMessagesPerRead);

    @Override
    RxtxChannelConfig setWriteSpinCount(int writeSpinCount);

    @Override
    RxtxChannelConfig setAllocator(io.netty.buffer.ByteBufAllocator allocator);

    @Override
    RxtxChannelConfig setRecvByteBufAllocator(io.netty.channel.RecvByteBufAllocator allocator);

    @Override
    RxtxChannelConfig setAutoRead(boolean autoRead);

    @Override
    RxtxChannelConfig setAutoClose(boolean autoClose);

    @Override
    RxtxChannelConfig setWriteBufferHighWaterMark(int writeBufferHighWaterMark);

    @Override
    RxtxChannelConfig setWriteBufferLowWaterMark(int writeBufferLowWaterMark);

    @Override
    RxtxChannelConfig setWriteBufferWaterMark(io.netty.channel.WriteBufferWaterMark writeBufferWaterMark);

    @Override
    RxtxChannelConfig setMessageSizeEstimator(io.netty.channel.MessageSizeEstimator estimator);

    /**
     * Default configuration class for RXTX device connections.
     *
     * @author fengbinbin
     * @version 1.0
     * @since 2024/3/1 14:44
     */
    @SuppressWarnings("deprecation")
    final class DefaultRxtxChannelConfig extends AbstractSerialChannelConfig implements RxtxChannelConfig {

        DefaultRxtxChannelConfig(RxtxChannel channel) {
            super(channel);
        }

        @Override
        public Map<ChannelOption<?>, Object> getOptions() {
            return getOptions(super.getOptions(), RxtxChannelOption.BAUD_RATE, RxtxChannelOption.DTR, RxtxChannelOption.RTS, RxtxChannelOption.STOP_BITS, RxtxChannelOption.DATA_BITS, RxtxChannelOption.PARITY_BIT, RxtxChannelOption.READ_TIMEOUT);
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> T getOption(ChannelOption<T> option) {
            if (option == RxtxChannelOption.BAUD_RATE) {
                return (T) Integer.valueOf(getBaudRate());
            }
            if (option == RxtxChannelOption.DTR) {
                return (T) Boolean.valueOf(getDtr());
            }
            if (option == RxtxChannelOption.RTS) {
                return (T) Boolean.valueOf(getRts());
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
                setStopBits((SerialStopBits) value);
            } else if (option == RxtxChannelOption.DATA_BITS) {
                setDataBits((SerialDataBits) value);
            } else if (option == RxtxChannelOption.PARITY_BIT) {
                setParityBit((SerialParityBit) value);
            } else if (option == RxtxChannelOption.READ_TIMEOUT) {
                setReadTimeout((Integer) value);
            } else {
                return super.setOption(option, value);
            }
            return true;
        }

        @Override
        public RxtxChannelConfig setBaudRate(int baudRate) {
            super.setBaudRate(baudRate);
            return this;
        }

        @Override
        public RxtxChannelConfig setStopBits(SerialStopBits stopBits) {
            super.setStopBits(stopBits);
            return this;
        }

        @Override
        public RxtxChannelConfig setDataBits(SerialDataBits dataBits) {
            super.setDataBits(dataBits);
            return this;
        }

        @Override
        public RxtxChannelConfig setParityBit(SerialParityBit parityBit) {
            super.setParityBit(parityBit);
            return this;
        }

        @Override
        public RxtxChannelConfig setDtr(boolean dtr) {
            super.setDtr(dtr);
            return this;
        }

        @Override
        public RxtxChannelConfig setRts(boolean rts) {
            super.setRts(rts);
            return this;
        }

        @Override
        public RxtxChannelConfig setReadTimeout(int readTimeout) {
            super.setReadTimeout(readTimeout);
            return this;
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
        public RxtxChannelConfig setAllocator(io.netty.buffer.ByteBufAllocator allocator) {
            super.setAllocator(allocator);
            return this;
        }

        @Override
        public RxtxChannelConfig setRecvByteBufAllocator(io.netty.channel.RecvByteBufAllocator allocator) {
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
        public RxtxChannelConfig setWriteBufferWaterMark(io.netty.channel.WriteBufferWaterMark writeBufferWaterMark) {
            super.setWriteBufferWaterMark(writeBufferWaterMark);
            return this;
        }

        @Override
        public RxtxChannelConfig setMessageSizeEstimator(io.netty.channel.MessageSizeEstimator estimator) {
            super.setMessageSizeEstimator(estimator);
            return this;
        }
    }
}
