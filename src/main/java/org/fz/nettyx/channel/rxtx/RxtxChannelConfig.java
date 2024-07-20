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

import gnu.io.SerialPort;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.*;
import lombok.RequiredArgsConstructor;
import org.fz.nettyx.exception.UnknownConfigException;

import java.util.Map;

import static io.netty.util.internal.ObjectUtil.checkPositiveOrZero;

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
            throw new UnknownConfigException(StopBits.class.getSimpleName(), value);
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
        private volatile boolean   rts;
        private volatile StopBits  stopbits    = StopBits.STOPBITS_1;
        private volatile DataBits  databits    = DataBits.DATABITS_8;
        private volatile ParityBit paritybit   = ParityBit.NONE;
        private volatile int       readTimeout = 1000;

        DefaultRxtxChannelConfig(RxtxChannel channel) {
            super(channel);
            setAllocator(new PreferHeapByteBufAllocator(getAllocator()));
        }

        @Override
        public Map<ChannelOption<?>, Object> getOptions() {
            return getOptions(super.getOptions(), RxtxChannelOption.BAUD_RATE, RxtxChannelOption.DTR, RxtxChannelOption.RTS, RxtxChannelOption.STOP_BITS, RxtxChannelOption.DATA_BITS, RxtxChannelOption.PARITY_BIT);
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
}
