package org.fz.nettyx.channel.jsc;


import com.fazecast.jSerialComm.SerialPort;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.*;
import lombok.RequiredArgsConstructor;
import org.fz.nettyx.exception.UnknownConfigException;

import java.util.Arrays;
import java.util.Map;

/**
 * java serial comm channel config
 *
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
     * @return The configured stop bits, defaulting to {@link StopBits#STOP_BITS_1} if unset
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
     * @return The configured parity bit, defaulting to {@link ParityBit#NO} if unset
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
        STOP_BITS_1(SerialPort.ONE_STOP_BIT),
        /**
         * 2 stop bits will be sent at the end of every character
         */
        STOP_BITS_2(SerialPort.TWO_STOP_BITS),
        /**
         * 1.5 stop bits will be sent at the end of every character
         */
        STOP_BITS_1_5(SerialPort.ONE_POINT_FIVE_STOP_BITS);

        private final int value;

        public static StopBits valueOf(int value) {
            return Arrays.stream(StopBits.values())
                         .filter(stopBit -> stopBit.value == value)
                         .findFirst()
                         .orElseThrow(() -> new UnknownConfigException(StopBits.class.getSimpleName(), value));
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
        DATA_BITS_5(5),
        /**
         * 6 data bits will be used for each character
         */
        DATA_BITS_6(6),
        /**
         * 7 data bits will be used for each character (ie. ASCII)
         */
        DATA_BITS_7(7),
        /**
         * 8 data bits will be used for each character (ie. binary data)
         */
        DATA_BITS_8(8);

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
        NO(SerialPort.NO_PARITY),
        /**
         * An odd parity bit will be sent with each data character, ie. will be set to 1 if the data character contains
         * an even number of bits set to 1.
         */
        ODD(SerialPort.ODD_PARITY),
        /**
         * An even parity bit will be sent with each data character, ie. will be set to 1 if the data character contains
         * an odd number of bits set to 1.
         */
        EVEN(SerialPort.EVEN_PARITY),
        /**
         * A mark parity bit (ie. always 1) will be sent with each data character
         */
        MARK(SerialPort.MARK_PARITY),
        /**
         * A space parity bit (ie. always 0) will be sent with each data character
         */
        SPACE(SerialPort.SPACE_PARITY);

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

    /**
     * @author fengbinbin
     * @version 1.0
     * @since 2024/3/2 13:29
     */
    @SuppressWarnings("deprecation")
    final class DefaultJscChannelConfig extends DefaultChannelConfig implements JscChannelConfig {

        private volatile int       baudRate    = 115200;
        private volatile DataBits  dataBits    = DataBits.DATA_BITS_8;
        private volatile StopBits  stopbits    = StopBits.STOP_BITS_1;
        private volatile ParityBit paritybit   = ParityBit.NO;
        private volatile int       readTimeout = 1000;
        private volatile boolean   dtr;
        private volatile boolean   rts;

        DefaultJscChannelConfig(JscChannel channel) {
            super(channel);
            setAllocator(new PreferHeapByteBufAllocator(getAllocator()));
        }

        @Override
        public Map<ChannelOption<?>, Object> getOptions() {
            return getOptions(super.getOptions(), JscChannelOption.BAUD_RATE, JscChannelOption.DTR, JscChannelOption.RTS, JscChannelOption.STOP_BITS, JscChannelOption.DATA_BITS, JscChannelOption.PARITY_BIT);
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
}