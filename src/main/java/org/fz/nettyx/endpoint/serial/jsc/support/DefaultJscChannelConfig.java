package org.fz.nettyx.endpoint.serial.jsc.support;

import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.*;

import java.util.Map;


/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/3/2 13:29
 */

@SuppressWarnings("deprecation")
final class DefaultJscChannelConfig extends DefaultChannelConfig implements JscChannelConfig {

    private volatile int       baudRate    = 115200;
    private volatile DataBits  dataBits    = DataBits.DATABITS_8;
    private volatile StopBits  stopbits    = StopBits.ONE_STOP_BIT;
    private volatile ParityBit paritybit   = ParityBit.NO_PARITY;
    private volatile boolean   dtr;
    private volatile boolean   rts;
    private volatile int       waitTime;
    private volatile int       readTimeout = 1000;

    DefaultJscChannelConfig(JscChannel channel) {
        super(channel);
        setAllocator(new PreferHeapByteBufAllocator(getAllocator()));
    }

    @Override
    public Map<ChannelOption<?>, Object> getOptions() {
        return getOptions(super.getOptions(), JscChannelOption.BAUD_RATE, JscChannelOption.DTR, JscChannelOption.RTS, JscChannelOption.STOP_BITS, JscChannelOption.DATA_BITS, JscChannelOption.PARITY_BIT, JscChannelOption.WAIT_TIME);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getOption(ChannelOption<T> option) {
        if (option == JscChannelOption.BAUD_RATE) {
            return (T) Integer.valueOf(getBaudRate());
        }
        if (option == JscChannelOption.DTR) {
            return (T) Boolean.valueOf(isDtr());
        }
        if (option == JscChannelOption.RTS) {
            return (T) Boolean.valueOf(isRts());
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
        if (option == JscChannelOption.WAIT_TIME) {
            return (T) Integer.valueOf(getWaitTimeMillis());
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
            setStopBits((StopBits) value);
        } else if (option == JscChannelOption.DATA_BITS) {
            setDataBits((DataBits) value);
        } else if (option == JscChannelOption.PARITY_BIT) {
            setParityBit((ParityBit) value);
        } else if (option == JscChannelOption.WAIT_TIME) {
            setWaitTimeMillis((Integer) value);
        } else if (option == JscChannelOption.READ_TIMEOUT) {
            setReadTimeout((Integer) value);
        } else {
            return super.setOption(option, value);
        }
        return true;
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
    public JscChannelConfig setAllocator(ByteBufAllocator allocator) {
        super.setAllocator(allocator);
        return this;
    }

    @Override
    public JscChannelConfig setRecvByteBufAllocator(RecvByteBufAllocator allocator) {
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
    public JscChannelConfig setWriteBufferWaterMark(WriteBufferWaterMark writeBufferWaterMark) {
        super.setWriteBufferWaterMark(writeBufferWaterMark);
        return this;
    }

    @Override
    public JscChannelConfig setMessageSizeEstimator(MessageSizeEstimator estimator) {
        super.setMessageSizeEstimator(estimator);
        return this;
    }

    @Override
    public int getBaudRate() {
        return baudRate;
    }

    @Override
    public JscChannelConfig setBaudRate(final int baudRate) {
        this.baudRate = baudRate;
        return this;
    }

    @Override
    public StopBits getStopBits() {
        return stopbits;
    }

    @Override
    public JscChannelConfig setStopBits(final StopBits stopBits) {
        this.stopbits = stopBits;
        return this;
    }

    @Override
    public DataBits getDataBits() {
        return dataBits;
    }

    @Override
    public JscChannelConfig setDataBits(final DataBits dataBits) {
        this.dataBits = dataBits;
        return this;
    }

    @Override
    public ParityBit getParityBit() {
        return paritybit;
    }

    @Override
    public JscChannelConfig setParityBit(final ParityBit parityBit) {
        this.paritybit = parityBit;
        return this;
    }

    @Override
    public boolean isDtr() {
        return dtr;
    }

    @Override
    public JscChannelConfig setDtr(final boolean dtr) {
        this.dtr = dtr;
        return this;
    }

    @Override
    public boolean isRts() {
        return rts;
    }

    @Override
    public JscChannelConfig setRts(final boolean rts) {
        this.rts = rts;
        return this;
    }

    @Override
    public int getWaitTimeMillis() {
        return waitTime;
    }

    @Override
    public JscChannelConfig setWaitTimeMillis(final int waitTimeMillis) {
        if (waitTimeMillis < 0) {
            throw new IllegalArgumentException("Wait time must be >= 0");
        }
        waitTime = waitTimeMillis;
        return this;
    }

    @Override
    public int getReadTimeout() {
        return readTimeout;
    }

    @Override
    public JscChannelConfig setReadTimeout(int readTimeout) {
        if (readTimeout < 0) {
            throw new IllegalArgumentException("readTimeout must be >= 0");
        }
        this.readTimeout = readTimeout;
        return this;
    }
}