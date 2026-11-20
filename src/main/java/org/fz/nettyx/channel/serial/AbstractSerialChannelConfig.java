package org.fz.nettyx.channel.serial;

import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.*;
import lombok.Getter;

import static io.netty.util.internal.ObjectUtil.checkPositiveOrZero;

/**
 * Common default configuration for serial device connections.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2024/3/1 14:44
 */
@SuppressWarnings("deprecation")
public abstract class AbstractSerialChannelConfig extends DefaultChannelConfig implements SerialChannelConfig {

    @Getter(onMethod_ = @Override)
    volatile int             baudRate    = 115200;
    @Getter(onMethod_ = @Override)
    volatile SerialStopBits  stopBits    = SerialStopBits.STOP_BITS_1;
    @Getter(onMethod_ = @Override)
    volatile SerialDataBits  dataBits    = SerialDataBits.DATA_BITS_8;
    @Getter(onMethod_ = @Override)
    volatile SerialParityBit parityBit   = SerialParityBit.NO;
    @Getter(onMethod_ = @Override)
    volatile int             readTimeout = 1000;
    volatile boolean         dtr;
    volatile boolean         rts;

    protected AbstractSerialChannelConfig(Channel channel) {
        super(channel);
        setAllocator(new PreferHeapByteBufAllocator(getAllocator()));
    }

    @Override
    public SerialChannelConfig setBaudRate(int baudRate) {
        this.baudRate = baudRate;
        return this;
    }

    @Override
    public SerialChannelConfig setStopBits(SerialStopBits stopBits) {
        this.stopBits = stopBits;
        return this;
    }

    @Override
    public SerialChannelConfig setDataBits(SerialDataBits dataBits) {
        this.dataBits = dataBits;
        return this;
    }

    @Override
    public SerialChannelConfig setParityBit(SerialParityBit parityBit) {
        this.parityBit = parityBit;
        return this;
    }

    @Override
    public boolean getDtr() {
        return dtr;
    }

    @Override
    public SerialChannelConfig setDtr(boolean dtr) {
        this.dtr = dtr;
        return this;
    }

    @Override
    public boolean getRts() {
        return rts;
    }

    @Override
    public SerialChannelConfig setRts(boolean rts) {
        this.rts = rts;
        return this;
    }

    @Override
    public SerialChannelConfig setReadTimeout(int readTimeout) {
        this.readTimeout = checkPositiveOrZero(readTimeout, "readTimeout");
        return this;
    }

    @Override
    public SerialChannelConfig setConnectTimeoutMillis(int connectTimeoutMillis) {
        super.setConnectTimeoutMillis(connectTimeoutMillis);
        return this;
    }

    @Override
    public SerialChannelConfig setMaxMessagesPerRead(int maxMessagesPerRead) {
        super.setMaxMessagesPerRead(maxMessagesPerRead);
        return this;
    }

    @Override
    public SerialChannelConfig setWriteSpinCount(int writeSpinCount) {
        super.setWriteSpinCount(writeSpinCount);
        return this;
    }

    @Override
    public SerialChannelConfig setAllocator(ByteBufAllocator allocator) {
        super.setAllocator(allocator);
        return this;
    }

    @Override
    public SerialChannelConfig setRecvByteBufAllocator(RecvByteBufAllocator allocator) {
        super.setRecvByteBufAllocator(allocator);
        return this;
    }

    @Override
    public SerialChannelConfig setAutoRead(boolean autoRead) {
        super.setAutoRead(autoRead);
        return this;
    }

    @Override
    public SerialChannelConfig setAutoClose(boolean autoClose) {
        super.setAutoClose(autoClose);
        return this;
    }

    @Override
    public SerialChannelConfig setWriteBufferHighWaterMark(int writeBufferHighWaterMark) {
        super.setWriteBufferHighWaterMark(writeBufferHighWaterMark);
        return this;
    }

    @Override
    public SerialChannelConfig setWriteBufferLowWaterMark(int writeBufferLowWaterMark) {
        super.setWriteBufferLowWaterMark(writeBufferLowWaterMark);
        return this;
    }

    @Override
    public SerialChannelConfig setWriteBufferWaterMark(WriteBufferWaterMark writeBufferWaterMark) {
        super.setWriteBufferWaterMark(writeBufferWaterMark);
        return this;
    }

    @Override
    public SerialChannelConfig setMessageSizeEstimator(MessageSizeEstimator estimator) {
        super.setMessageSizeEstimator(estimator);
        return this;
    }
}
