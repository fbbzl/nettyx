package org.fz.nettyx.channel.serial;

import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelConfig;
import io.netty.channel.MessageSizeEstimator;
import io.netty.channel.RecvByteBufAllocator;
import io.netty.channel.WriteBufferWaterMark;

/**
 * Common configuration for serial device connections.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2024/3/1 14:44
 */
public interface SerialChannelConfig extends ChannelConfig {

    int getBaudRate();

    SerialChannelConfig setBaudRate(int baudRate);

    SerialStopBits getStopBits();

    SerialChannelConfig setStopBits(SerialStopBits stopBits);

    SerialDataBits getDataBits();

    SerialChannelConfig setDataBits(SerialDataBits dataBits);

    SerialParityBit getParityBit();

    SerialChannelConfig setParityBit(SerialParityBit parityBit);

    boolean getDtr();

    SerialChannelConfig setDtr(boolean dtr);

    boolean getRts();

    SerialChannelConfig setRts(boolean rts);

    int getReadTimeout();

    SerialChannelConfig setReadTimeout(int readTimeout);

    @Override
    SerialChannelConfig setConnectTimeoutMillis(int connectTimeoutMillis);

    @Override
    SerialChannelConfig setMaxMessagesPerRead(int maxMessagesPerRead);

    @Override
    SerialChannelConfig setWriteSpinCount(int writeSpinCount);

    @Override
    SerialChannelConfig setAllocator(ByteBufAllocator allocator);

    @Override
    SerialChannelConfig setRecvByteBufAllocator(RecvByteBufAllocator allocator);

    @Override
    SerialChannelConfig setAutoRead(boolean autoRead);

    @Override
    SerialChannelConfig setAutoClose(boolean autoClose);

    @Override
    SerialChannelConfig setWriteBufferHighWaterMark(int writeBufferHighWaterMark);

    @Override
    SerialChannelConfig setWriteBufferLowWaterMark(int writeBufferLowWaterMark);

    @Override
    SerialChannelConfig setWriteBufferWaterMark(WriteBufferWaterMark writeBufferWaterMark);

    @Override
    SerialChannelConfig setMessageSizeEstimator(MessageSizeEstimator estimator);
}
