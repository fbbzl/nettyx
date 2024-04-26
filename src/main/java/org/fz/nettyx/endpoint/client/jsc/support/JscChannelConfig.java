package org.fz.nettyx.endpoint.client.jsc.support;


import com.fazecast.jSerialComm.SerialPort;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelConfig;
import io.netty.channel.MessageSizeEstimator;
import io.netty.channel.RecvByteBufAllocator;
import io.netty.channel.WriteBufferWaterMark;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/3/2 13:29
 */
public interface JscChannelConfig extends ChannelConfig {

    /**
     * @return The configured baud rate, defaulting to 115200 if unset
     */
    int getBaudRate();

    /**
     * Sets the baud rate (ie. bits per second) for communication with the serial device. The baud rate will include
     * bits for framing (in the form of stop bits and parity), such that the effective data rate will be lower than this
     * value.
     *
     * @param baudRate The baud rate (in bits per second)
     */
    JscChannelConfig setBaudRate(int baudRate);

    /**
     * @return The configured stop bits, defaulting to {@link StopBits#ONE_STOP_BIT} if unset
     */
    StopBits getStopBits();

    /**
     * Sets the number of stop bits to include at the end of every character to aid the serial device in synchronising
     * with the data.
     *
     * @param stopBits The number of stop bits to use
     */
    JscChannelConfig setStopBits(StopBits stopBits);

    /**
     * @return The configured data bits, defaulting to 8 if unset
     */
    DataBits getDataBits();

    /**
     * Sets the number of data bits to use to make up each character sent to the serial device.
     *
     * @param dataBits The number of data bits to use
     */
    JscChannelConfig setDataBits(DataBits dataBits);

    /**
     * @return The configured parity bit, defaulting to {@link ParityBit#NO_PARITY} if unset
     */
    ParityBit getParityBit();

    /**
     * Sets the type of parity bit to be used when communicating with the serial device.
     *
     * @param parityBit The type of parity bit to be used
     */
    JscChannelConfig setParityBit(ParityBit parityBit);

    /**
     * @return true if the serial device should support the Data Terminal Ready signal
     */
    boolean isDtr();

    /**
     * Sets whether the serial device supports the Data Terminal Ready signal, used for flow control
     *
     * @param dtr true if DTR is supported, false otherwise
     */
    JscChannelConfig setDtr(boolean dtr);

    /**
     * @return true if the serial device should support the Ready to Send signal
     */
    boolean isRts();

    /**
     * Sets whether the serial device supports the Request To Send signal, used for flow control
     *
     * @param rts true if RTS is supported, false otherwise
     */
    JscChannelConfig setRts(boolean rts);

    /**
     * @return The number of milliseconds to wait between opening the serial port and initialising.
     */
    int getWaitTimeMillis();

    /**
     * Sets the time to wait after opening the serial port and before sending it any configuration information or data.
     * A value of 0 indicates that no waiting should occur.
     *
     * @param waitTimeMillis The number of milliseconds to wait, defaulting to 0 (no wait) if unset
     * @throws IllegalArgumentException if the supplied value is &lt; 0
     */
    JscChannelConfig setWaitTimeMillis(int waitTimeMillis);

    /**
     * Return the maximal time (in ms) to block and wait for something to be ready to read.
     */
    int getReadTimeout();

    /**
     * Sets the maximal time (in ms) to block while try to read from the serial port. Default is 1000ms
     */
    JscChannelConfig setReadTimeout(int readTimeout);

    @Override
    JscChannelConfig setConnectTimeoutMillis(int connectTimeoutMillis);

    @Override
    JscChannelConfig setMaxMessagesPerRead(int maxMessagesPerRead);

    @Override
    JscChannelConfig setWriteSpinCount(int writeSpinCount);

    @Override
    JscChannelConfig setAllocator(ByteBufAllocator allocator);

    @Override
    JscChannelConfig setRecvByteBufAllocator(RecvByteBufAllocator allocator);

    @Override
    JscChannelConfig setAutoRead(boolean autoRead);

    @Override
    JscChannelConfig setAutoClose(boolean autoClose);

    @Override
    JscChannelConfig setWriteBufferHighWaterMark(int writeBufferHighWaterMark);

    @Override
    JscChannelConfig setWriteBufferLowWaterMark(int writeBufferLowWaterMark);

    @Override
    JscChannelConfig setMessageSizeEstimator(MessageSizeEstimator estimator);

    @Override
    JscChannelConfig setWriteBufferWaterMark(WriteBufferWaterMark writeBufferWaterMark);

    @RequiredArgsConstructor
    enum StopBits {
        /**
         * 1 stop bit will be sent at the end of every character
         */
        ONE_STOP_BIT(SerialPort.ONE_STOP_BIT),
        /**
         * 2 stop bits will be sent at the end of every character
         */
        TWO_STOP_BITS(SerialPort.TWO_STOP_BITS),
        /**
         * 1.5 stop bits will be sent at the end of every character
         */
        ONE_POINT_FIVE_STOP_BITS(SerialPort.ONE_POINT_FIVE_STOP_BITS);

        private final int value;

        public static StopBits valueOf(int value) {
            return Arrays.stream(StopBits.values())
                         .filter(stopBit -> stopBit.value == value)
                         .findFirst()
                         .orElseThrow(() -> new IllegalArgumentException(
                                 "unknown " + StopBits.class.getSimpleName() + " value: " + value));
        }

        public int value() {
            return value;
        }
    }

    @RequiredArgsConstructor
    enum DataBits {
        /**
         * 5 data bits will be used for each character (ie. Baudot code)
         */
        DATABITS_5(5),
        /**
         * 6 data bits will be used for each character
         */
        DATABITS_6(6),
        /**
         * 7 data bits will be used for each character (ie. ASCII)
         */
        DATABITS_7(7),
        /**
         * 8 data bits will be used for each character (ie. binary data)
         */
        DATABITS_8(8);

        private final int value;

        public int value() {
            return value;
        }

        public static DataBits valueOf(int value) {
            for (DataBits databit : DataBits.values()) {
                if (databit.value == value) {
                    return databit;
                }
            }
            throw new IllegalArgumentException("unknown " + DataBits.class.getSimpleName() + " value: " + value);
        }
    }

    @RequiredArgsConstructor
    enum ParityBit {
        /**
         * No parity bit will be sent with each data character at all
         */
        NO_PARITY(SerialPort.NO_PARITY),
        /**
         * An odd parity bit will be sent with each data character, ie. will be set to 1 if the data character contains
         * an even number of bits set to 1.
         */
        ODD_PARITY(SerialPort.ODD_PARITY),
        /**
         * An even parity bit will be sent with each data character, ie. will be set to 1 if the data character contains
         * an odd number of bits set to 1.
         */
        EVEN_PARITY(SerialPort.EVEN_PARITY),
        /**
         * A mark parity bit (ie. always 1) will be sent with each data character
         */
        MARK_PARITY(SerialPort.MARK_PARITY),
        /**
         * A space parity bit (ie. always 0) will be sent with each data character
         */
        SPACE_PARITY(SerialPort.SPACE_PARITY);

        private final int value;

        public static ParityBit valueOf(int value) {
            for (ParityBit paritybit : ParityBit.values()) {
                if (paritybit.value == value) {
                    return paritybit;
                }
            }
            throw new IllegalArgumentException("unknown " + ParityBit.class.getSimpleName() + " value: " + value);
        }

        public int value() {
            return value;
        }
    }
}