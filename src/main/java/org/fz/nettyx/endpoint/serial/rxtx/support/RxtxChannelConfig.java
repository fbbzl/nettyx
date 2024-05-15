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
package org.fz.nettyx.endpoint.serial.rxtx.support;

import gnu.io.SerialPort;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelConfig;
import io.netty.channel.MessageSizeEstimator;
import io.netty.channel.RecvByteBufAllocator;
import io.netty.channel.WriteBufferWaterMark;
import lombok.RequiredArgsConstructor;

/**
 * A configuration class for RXTX device connections.
 *
 * <h3>Available options</h3>
 * <p>
 * In addition to the options provided by {@link ChannelConfig},
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
 * <td>{@link RxtxChannelOption#STOP_BITS}</td><td>{@link #setStopBits(StopBits)}</td>
 * </tr><tr>
 * <td>{@link RxtxChannelOption#DATA_BITS}</td><td>{@link #setDataBits(DataBits)}</td>
 * </tr><tr>
 * <td>{@link RxtxChannelOption#PARITY_BIT}</td><td>{@link #setParityBit(ParityBit)}</td>
 * </tr><tr>
 * <td>{@link RxtxChannelOption#WAIT_TIME}</td><td>{@link #setWaitTimeMillis(int)}</td>
 * </tr>
 * </table>
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2024/3/1 14:44
 */
public interface RxtxChannelConfig extends ChannelConfig {

    /**
     * Sets the baud rate (ie. bits per second) for communication with the serial device.
     * The baud rate will include bits for framing (in the form of stop bits and parity),
     * such that the effective data rate will be lower than this value.
     *
     * @param baudrate The baud rate (in bits per second)
     */
    RxtxChannelConfig setBaudRate(int baudrate);

    /**
     * Sets the number of stop bits to include at the end of every character to aid the
     * serial device in synchronising with the data.
     *
     * @param stopbits The number of stop bits to use
     */
    RxtxChannelConfig setStopBits(StopBits stopbits);

    /**
     * Sets the number of data bits to use to make up each character sent to the serial
     * device.
     *
     * @param databits The number of data bits to use
     */
    RxtxChannelConfig setDataBits(DataBits databits);

    /**
     * Sets the type of parity bit to be used when communicating with the serial device.
     *
     * @param paritybit The type of parity bit to be used
     */
    RxtxChannelConfig setParityBit(ParityBit paritybit);

    /**
     * @return The configured baud rate, defaulting to 115200 if unset
     */
    int getBaudRate();

    /**
     * @return The configured stop bits, defaulting to {@link StopBits#STOPBITS_1} if unset
     */
    StopBits getStopBits();

    /**
     * @return The configured data bits, defaulting to {@link DataBits#DATABITS_8} if unset
     */
    DataBits getDataBits();

    /**
     * @return The configured parity bit, defaulting to {@link ParityBit#NONE} if unset
     */
    ParityBit getParityBit();

    /**
     * @return true if the serial device should support the Data Terminal Ready signal
     */
    boolean isDtr();

    /**
     * Sets whether the serial device supports the Data Terminal Ready signal, used for
     * flow control
     *
     * @param dtr true if DTR is supported, false otherwise
     */
    RxtxChannelConfig setDtr(boolean dtr);

    /**
     * @return true if the serial device should support the Ready to Send signal
     */
    boolean isRts();

    /**
     * Sets whether the serial device supports the Request To Send signal, used for flow
     * control
     *
     * @param rts true if RTS is supported, false otherwise
     */
    RxtxChannelConfig setRts(boolean rts);

    /**
     * @return The number of milliseconds to wait between opening the serial port and
     * initialising.
     */
    int getWaitTimeMillis();

    /**
     * Sets the time to wait after opening the serial port and before sending it any
     * configuration information or data. A value of 0 indicates that no waiting should
     * occur.
     *
     * @param waitTimeMillis The number of milliseconds to wait, defaulting to 0 (no
     *                       wait) if unset
     * @throws IllegalArgumentException if the supplied value is &lt; 0
     */
    RxtxChannelConfig setWaitTimeMillis(int waitTimeMillis);

    /**
     * Sets the maximal time (in ms) to block while try to read from the serial port. Default is 1000ms
     */
    RxtxChannelConfig setReadTimeout(int readTimeout);

    /**
     * Return the maximal time (in ms) to block and wait for something to be ready to read.
     */
    int getReadTimeout();

    @Override
    RxtxChannelConfig setConnectTimeoutMillis(int connectTimeoutMillis);

    @Override
    RxtxChannelConfig setMaxMessagesPerRead(int maxMessagesPerRead);

    @Override
    RxtxChannelConfig setWriteSpinCount(int writeSpinCount);

    @Override
    RxtxChannelConfig setAllocator(ByteBufAllocator allocator);

    @Override
    RxtxChannelConfig setRecvByteBufAllocator(RecvByteBufAllocator allocator);

    @Override
    RxtxChannelConfig setAutoRead(boolean autoRead);

    @Override
    RxtxChannelConfig setAutoClose(boolean autoClose);

    @Override
    RxtxChannelConfig setWriteBufferHighWaterMark(int writeBufferHighWaterMark);

    @Override
    RxtxChannelConfig setWriteBufferLowWaterMark(int writeBufferLowWaterMark);

    @Override
    RxtxChannelConfig setWriteBufferWaterMark(WriteBufferWaterMark writeBufferWaterMark);

    @Override
    RxtxChannelConfig setMessageSizeEstimator(MessageSizeEstimator estimator);

    @RequiredArgsConstructor
    enum StopBits {
        /**
         * 1 stop bit will be sent at the end of every character
         */
        STOPBITS_1(SerialPort.STOPBITS_1),
        /**
         * 2 stop bits will be sent at the end of every character
         */
        STOPBITS_2(SerialPort.STOPBITS_2),
        /**
         * 1.5 stop bits will be sent at the end of every character
         */
        STOPBITS_1_5(SerialPort.STOPBITS_1_5);

        private final int value;

        public int value() {
            return value;
        }

        public static StopBits valueOf(int value) {
            for (StopBits stopbit : StopBits.values()) {
                if (stopbit.value == value) {
                    return stopbit;
                }
            }
            throw new IllegalArgumentException("unknown " + StopBits.class.getSimpleName() + " value: " + value);
        }
    }

    @RequiredArgsConstructor
    enum DataBits {
        /**
         * 5 data bits will be used for each character (ie. Baudot code)
         */
        DATABITS_5(SerialPort.DATABITS_5),
        /**
         * 6 data bits will be used for each character
         */
        DATABITS_6(SerialPort.DATABITS_6),
        /**
         * 7 data bits will be used for each character (ie. ASCII)
         */
        DATABITS_7(SerialPort.DATABITS_7),
        /**
         * 8 data bits will be used for each character (ie. binary data)
         */
        DATABITS_8(SerialPort.DATABITS_8);

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
        NONE(SerialPort.PARITY_NONE),
        /**
         * An odd parity bit will be sent with each data character, ie. will be set
         * to 1 if the data character contains an even number of bits set to 1.
         */
        ODD(SerialPort.PARITY_ODD),
        /**
         * An even parity bit will be sent with each data character, ie. will be set
         * to 1 if the data character contains an odd number of bits set to 1.
         */
        EVEN(SerialPort.PARITY_EVEN),
        /**
         * A mark parity bit (ie. always 1) will be sent with each data character
         */
        MARK(SerialPort.PARITY_MARK),
        /**
         * A space parity bit (ie. always 0) will be sent with each data character
         */
        SPACE(SerialPort.PARITY_SPACE);

        private final int value;

        public int value() {
            return value;
        }

        public static ParityBit valueOf(int value) {
            for (ParityBit paritybit : ParityBit.values()) {
                if (paritybit.value == value) {
                    return paritybit;
                }
            }
            throw new IllegalArgumentException("unknown " + ParityBit.class.getSimpleName() + " value: " + value);
        }
    }
}
