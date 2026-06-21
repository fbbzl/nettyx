package org.fz.nettyx.channel.serial.jsc;


import io.netty.channel.ChannelOption;
import org.fz.nettyx.channel.serial.*;

import java.util.Map;

/**
 * java serial comm channel config
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2024/3/2 13:29
 */
public interface JscChannelConfig extends SerialChannelConfig {

    @Override
    JscChannelConfig setBaudRate(int baudRate);

    @Override
    JscChannelConfig setStopBits(SerialStopBits stopBits);

    @Override
    JscChannelConfig setDataBits(SerialDataBits dataBits);

    @Override
    JscChannelConfig setParityBit(SerialParityBit parityBit);

    @Override
    JscChannelConfig setDtr(boolean dtr);

    @Override
    JscChannelConfig setRts(boolean rts);

    @Override
    JscChannelConfig setReadTimeout(int readTimeout);

    @Override
    JscChannelConfig setConnectTimeoutMillis(int connectTimeoutMillis);

    @Override
    JscChannelConfig setMaxMessagesPerRead(int maxMessagesPerRead);

    @Override
    JscChannelConfig setWriteSpinCount(int writeSpinCount);

    @Override
    JscChannelConfig setAllocator(io.netty.buffer.ByteBufAllocator allocator);

    @Override
    JscChannelConfig setRecvByteBufAllocator(io.netty.channel.RecvByteBufAllocator allocator);

    @Override
    JscChannelConfig setAutoRead(boolean autoRead);

    @Override
    JscChannelConfig setAutoClose(boolean autoClose);

    @Override
    JscChannelConfig setWriteBufferHighWaterMark(int writeBufferHighWaterMark);

    @Override
    JscChannelConfig setWriteBufferLowWaterMark(int writeBufferLowWaterMark);

    @Override
    JscChannelConfig setWriteBufferWaterMark(io.netty.channel.WriteBufferWaterMark writeBufferWaterMark);

    @Override
    JscChannelConfig setMessageSizeEstimator(io.netty.channel.MessageSizeEstimator estimator);

    /**
     * @author fengbinbin
     * @version 1.0
     * @since 2024/3/2 13:29
     */
    @SuppressWarnings("deprecation")
    final class DefaultJscChannelConfig extends AbstractSerialChannelConfig implements JscChannelConfig {

        DefaultJscChannelConfig(JscChannel channel) {
            super(channel);
        }

        @Override
        public Map<ChannelOption<?>, Object> getOptions() {
            return getOptions(super.getOptions(), JscChannelOption.BAUD_RATE, JscChannelOption.DTR, JscChannelOption.RTS, JscChannelOption.STOP_BITS, JscChannelOption.DATA_BITS, JscChannelOption.PARITY_BIT, JscChannelOption.READ_TIMEOUT);
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> T getOption(ChannelOption<T> option) {
            if (option == JscChannelOption.BAUD_RATE) {
                return (T) Integer.valueOf(getBaudRate());
            }
            if (option == JscChannelOption.DTR) {
                return (T) Boolean.valueOf(getDtr());
            }
            if (option == JscChannelOption.RTS) {
                return (T) Boolean.valueOf(getRts());
            }
            if (option == JscChannelOption.STOP_BITS) {
                return (T) getStopBits();
            }
            if (option == JscChannelOption.DATA_BITS) {
                return (T) getDataBits();
            }
            if (option == JscChannelOption.PARITY_BIT) {
                return (T) getParityBit();
            }
            if (option == JscChannelOption.READ_TIMEOUT) {
                return (T) Integer.valueOf(getReadTimeout());
            }
            return super.getOption(option);
        }

        @Override
        public <T> boolean setOption(ChannelOption<T> option, T value) {
            validate(option, value);

            if (option == JscChannelOption.BAUD_RATE) {
                setBaudRate((Integer) value);
            } else if (option == JscChannelOption.DTR) {
                setDtr((Boolean) value);
            } else if (option == JscChannelOption.RTS) {
                setRts((Boolean) value);
            } else if (option == JscChannelOption.STOP_BITS) {
                setStopBits((SerialStopBits) value);
            } else if (option == JscChannelOption.DATA_BITS) {
                setDataBits((SerialDataBits) value);
            } else if (option == JscChannelOption.PARITY_BIT) {
                setParityBit((SerialParityBit) value);
            } else if (option == JscChannelOption.READ_TIMEOUT) {
                setReadTimeout((Integer) value);
            } else {
                return super.setOption(option, value);
            }
            return true;
        }

        @Override
        public JscChannelConfig setBaudRate(int baudRate) {
            super.setBaudRate(baudRate);
            return this;
        }

        @Override
        public JscChannelConfig setStopBits(SerialStopBits stopBits) {
            super.setStopBits(stopBits);
            return this;
        }

        @Override
        public JscChannelConfig setDataBits(SerialDataBits dataBits) {
            super.setDataBits(dataBits);
            return this;
        }

        @Override
        public JscChannelConfig setParityBit(SerialParityBit parityBit) {
            super.setParityBit(parityBit);
            return this;
        }

        @Override
        public JscChannelConfig setDtr(boolean dtr) {
            super.setDtr(dtr);
            return this;
        }

        @Override
        public JscChannelConfig setRts(boolean rts) {
            super.setRts(rts);
            return this;
        }

        @Override
        public JscChannelConfig setReadTimeout(int readTimeout) {
            super.setReadTimeout(readTimeout);
            return this;
        }

        @Override
        public JscChannelConfig setConnectTimeoutMillis(int connectTimeoutMillis) {
            super.setConnectTimeoutMillis(connectTimeoutMillis);
            return this;
        }

        @Override
        public JscChannelConfig setMaxMessagesPerRead(int maxMessagesPerRead) {
            super.setMaxMessagesPerRead(maxMessagesPerRead);
            return this;
        }

        @Override
        public JscChannelConfig setWriteSpinCount(int writeSpinCount) {
            super.setWriteSpinCount(writeSpinCount);
            return this;
        }

        @Override
        public JscChannelConfig setAllocator(io.netty.buffer.ByteBufAllocator allocator) {
            super.setAllocator(allocator);
            return this;
        }

        @Override
        public JscChannelConfig setRecvByteBufAllocator(io.netty.channel.RecvByteBufAllocator allocator) {
            super.setRecvByteBufAllocator(allocator);
            return this;
        }

        @Override
        public JscChannelConfig setAutoRead(boolean autoRead) {
            super.setAutoRead(autoRead);
            return this;
        }

        @Override
        public JscChannelConfig setAutoClose(boolean autoClose) {
            super.setAutoClose(autoClose);
            return this;
        }

        @Override
        public JscChannelConfig setWriteBufferHighWaterMark(int writeBufferHighWaterMark) {
            super.setWriteBufferHighWaterMark(writeBufferHighWaterMark);
            return this;
        }

        @Override
        public JscChannelConfig setWriteBufferLowWaterMark(int writeBufferLowWaterMark) {
            super.setWriteBufferLowWaterMark(writeBufferLowWaterMark);
            return this;
        }

        @Override
        public JscChannelConfig setWriteBufferWaterMark(io.netty.channel.WriteBufferWaterMark writeBufferWaterMark) {
            super.setWriteBufferWaterMark(writeBufferWaterMark);
            return this;
        }

        @Override
        public JscChannelConfig setMessageSizeEstimator(io.netty.channel.MessageSizeEstimator estimator) {
            super.setMessageSizeEstimator(estimator);
            return this;
        }
    }
}
