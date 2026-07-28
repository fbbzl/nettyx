package org.fz.nettyx.channel.serial;

import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelOption;
import io.netty.channel.DefaultMessageSizeEstimator;
import io.netty.channel.FixedRecvByteBufAllocator;
import io.netty.channel.WriteBufferWaterMark;
import org.fz.nettyx.channel.serial.jsc.JscChannel;
import org.fz.nettyx.channel.serial.jsc.JscChannelConfig;
import org.fz.nettyx.channel.serial.jsc.JscChannelOption;
import org.fz.nettyx.channel.serial.rxtx.RxtxChannel;
import org.fz.nettyx.channel.serial.rxtx.RxtxChannelConfig;
import org.fz.nettyx.channel.serial.rxtx.RxtxChannelOption;
import org.fz.nettyx.exception.UnknownConfigException;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.*;

public class SerialChannelConfigTest {

    @Test
    public void jscConfigSupportsCustomAndCommonOptions() {
        JscChannelConfig config = new JscChannel().config();

        assertDefaults(config);
        assertTrue(config.setOption(JscChannelOption.BAUD_RATE, 9600));
        assertTrue(config.setOption(JscChannelOption.DTR, true));
        assertTrue(config.setOption(JscChannelOption.RTS, true));
        assertTrue(config.setOption(JscChannelOption.STOP_BITS, SerialStopBits.STOP_BITS_2));
        assertTrue(config.setOption(JscChannelOption.DATA_BITS, SerialDataBits.DATA_BITS_7));
        assertTrue(config.setOption(JscChannelOption.PARITY_BIT, SerialParityBit.EVEN));
        assertTrue(config.setOption(JscChannelOption.READ_TIMEOUT, 250));
        assertEquals(Integer.valueOf(9600), config.getOption(JscChannelOption.BAUD_RATE));
        assertEquals(Boolean.TRUE, config.getOption(JscChannelOption.DTR));
        assertEquals(Boolean.TRUE, config.getOption(JscChannelOption.RTS));
        assertEquals(SerialStopBits.STOP_BITS_2, config.getOption(JscChannelOption.STOP_BITS));
        assertEquals(SerialDataBits.DATA_BITS_7, config.getOption(JscChannelOption.DATA_BITS));
        assertEquals(SerialParityBit.EVEN, config.getOption(JscChannelOption.PARITY_BIT));
        assertEquals(Integer.valueOf(250), config.getOption(JscChannelOption.READ_TIMEOUT));
        assertCommonConfiguration(config);
        assertContainsCustomOptions(config.getOptions(), JscChannelOption.BAUD_RATE, JscChannelOption.DTR,
                                    JscChannelOption.RTS, JscChannelOption.STOP_BITS,
                                    JscChannelOption.DATA_BITS, JscChannelOption.PARITY_BIT,
                                    JscChannelOption.READ_TIMEOUT);
    }

    @Test
    public void rxtxConfigSupportsCustomAndCommonOptions() {
        RxtxChannelConfig config = new RxtxChannel().config();

        assertDefaults(config);
        assertTrue(config.setOption(RxtxChannelOption.BAUD_RATE, 57600));
        assertTrue(config.setOption(RxtxChannelOption.DTR, true));
        assertTrue(config.setOption(RxtxChannelOption.RTS, true));
        assertTrue(config.setOption(RxtxChannelOption.STOP_BITS, SerialStopBits.STOP_BITS_1_5));
        assertTrue(config.setOption(RxtxChannelOption.DATA_BITS, SerialDataBits.DATA_BITS_6));
        assertTrue(config.setOption(RxtxChannelOption.PARITY_BIT, SerialParityBit.ODD));
        assertTrue(config.setOption(RxtxChannelOption.READ_TIMEOUT, 500));
        assertEquals(Integer.valueOf(57600), config.getOption(RxtxChannelOption.BAUD_RATE));
        assertEquals(Boolean.TRUE, config.getOption(RxtxChannelOption.DTR));
        assertEquals(Boolean.TRUE, config.getOption(RxtxChannelOption.RTS));
        assertEquals(SerialStopBits.STOP_BITS_1_5, config.getOption(RxtxChannelOption.STOP_BITS));
        assertEquals(SerialDataBits.DATA_BITS_6, config.getOption(RxtxChannelOption.DATA_BITS));
        assertEquals(SerialParityBit.ODD, config.getOption(RxtxChannelOption.PARITY_BIT));
        assertEquals(Integer.valueOf(500), config.getOption(RxtxChannelOption.READ_TIMEOUT));
        assertCommonConfiguration(config);
        assertContainsCustomOptions(config.getOptions(), RxtxChannelOption.BAUD_RATE, RxtxChannelOption.DTR,
                                    RxtxChannelOption.RTS, RxtxChannelOption.STOP_BITS,
                                    RxtxChannelOption.DATA_BITS, RxtxChannelOption.PARITY_BIT,
                                    RxtxChannelOption.READ_TIMEOUT);
    }

    @Test
    public void fluentSettersAndValidationWorkForBothImplementations() {
        JscChannelConfig jsc = new JscChannel().config();
        assertSame(jsc, jsc.setBaudRate(19200));
        assertSame(jsc, jsc.setStopBits(SerialStopBits.STOP_BITS_2));
        assertSame(jsc, jsc.setDataBits(SerialDataBits.DATA_BITS_5));
        assertSame(jsc, jsc.setParityBit(SerialParityBit.MARK));
        assertSame(jsc, jsc.setDtr(true));
        assertSame(jsc, jsc.setRts(true));
        assertSame(jsc, jsc.setReadTimeout(0));

        RxtxChannelConfig rxtx = new RxtxChannel().config();
        assertSame(rxtx, rxtx.setBaudRate(38400));
        assertSame(rxtx, rxtx.setStopBits(SerialStopBits.STOP_BITS_1));
        assertSame(rxtx, rxtx.setDataBits(SerialDataBits.DATA_BITS_8));
        assertSame(rxtx, rxtx.setParityBit(SerialParityBit.SPACE));
        assertSame(rxtx, rxtx.setDtr(true));
        assertSame(rxtx, rxtx.setRts(true));
        assertSame(rxtx, rxtx.setReadTimeout(0));

        assertThrows(IllegalArgumentException.class, () -> jsc.setReadTimeout(-1));
        assertThrows(IllegalArgumentException.class, () -> rxtx.setReadTimeout(-1));
    }

    @Test
    public void serialEnumsMapValuesAndRejectUnknownValues() {
        for (SerialDataBits value : SerialDataBits.values()) {
            assertSame(value, SerialDataBits.valueOf(value.value()));
        }
        for (SerialParityBit value : SerialParityBit.values()) {
            assertSame(value, SerialParityBit.valueOf(value.value()));
        }
        for (SerialStopBits value : SerialStopBits.values()) {
            assertSame(value, SerialStopBits.valueOf(value.value()));
        }

        assertThrows(UnknownConfigException.class, () -> SerialDataBits.valueOf(9));
        assertThrows(UnknownConfigException.class, () -> SerialParityBit.valueOf(5));
        assertThrows(UnknownConfigException.class, () -> SerialStopBits.valueOf(0));
    }

    @SuppressWarnings("deprecation")
    private static void assertCommonConfiguration(SerialChannelConfig config) {
        FixedRecvByteBufAllocator recvAllocator = new FixedRecvByteBufAllocator(256);
        WriteBufferWaterMark waterMark = new WriteBufferWaterMark(32, 64);

        assertSame(config, config.setConnectTimeoutMillis(100));
        assertSame(config, config.setMaxMessagesPerRead(2));
        assertSame(config, config.setWriteSpinCount(4));
        assertSame(config, config.setAllocator(ByteBufAllocator.DEFAULT));
        assertSame(config, config.setRecvByteBufAllocator(recvAllocator));
        assertSame(config, config.setAutoRead(false));
        assertSame(config, config.setAutoClose(false));
        assertSame(config, config.setWriteBufferWaterMark(waterMark));
        assertSame(config, config.setWriteBufferHighWaterMark(96));
        assertSame(config, config.setWriteBufferLowWaterMark(16));
        assertSame(config, config.setMessageSizeEstimator(DefaultMessageSizeEstimator.DEFAULT));
        assertTrue(config.setOption(ChannelOption.CONNECT_TIMEOUT_MILLIS, 200));
        assertEquals(Integer.valueOf(200), config.getOption(ChannelOption.CONNECT_TIMEOUT_MILLIS));
        assertEquals(200, config.getConnectTimeoutMillis());
        assertSame(recvAllocator, config.getRecvByteBufAllocator());
        assertFalse(config.isAutoRead());
        assertFalse(config.isAutoClose());
    }

    private static void assertDefaults(SerialChannelConfig config) {
        assertEquals(115200, config.getBaudRate());
        assertEquals(SerialStopBits.STOP_BITS_1, config.getStopBits());
        assertEquals(SerialDataBits.DATA_BITS_8, config.getDataBits());
        assertEquals(SerialParityBit.NO, config.getParityBit());
        assertEquals(1000, config.getReadTimeout());
        assertFalse(config.getDtr());
        assertFalse(config.getRts());
    }

    @SafeVarargs
    private static void assertContainsCustomOptions(Map<ChannelOption<?>, Object> options,
                                                    ChannelOption<?>... expected) {
        for (ChannelOption<?> option : expected) {
            assertTrue(options.containsKey(option));
        }
    }
}
