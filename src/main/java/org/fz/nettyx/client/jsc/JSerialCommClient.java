package org.fz.nettyx.client.jsc;

import static java.util.stream.Collectors.toList;

import com.fazecast.jSerialComm.SerialPort;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.EventLoopGroup;
import io.netty.util.ReferenceCountUtil;
import java.util.List;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.fz.nettyx.client.Client;
import org.fz.nettyx.exception.NoSuchPortException;

/**
 * comm util from the following url
 * <link href="https://blog.csdn.net/Sensemoment/article/details/127806291"></link>
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023/5/15
 */
@Slf4j
public class JSerialCommClient extends Client {

    private SerialPort serialPort;

    @Override
    public final EventLoopGroup getEventLoopGroup() {
        throw new UnsupportedOperationException("jsc do not support this method");
    }

    @Override
    public final Bootstrap getBootstrap() {
        throw new UnsupportedOperationException("jsc do not support this method");
    }

    public static List<String> find() {
        return Stream.of(SerialPort.getCommPorts()).map(SerialPort::getSystemPortName).distinct().collect(toList());
    }

    public JSerialCommClient open(String portName,
                                  int baudRate,
                                  int dataBit,
                                  int stopBit,
                                  int parityBit) throws NoSuchPortException {
        return open(portName, baudRate, SerialPort.FLOW_CONTROL_DISABLED, dataBit, stopBit, parityBit);
    }

    public JSerialCommClient open(String portName,
                                  int baudRate,
                                  int flowCtrl,
                                  int dataBit,
                                  int stopBit,
                                  int parityBit) throws NoSuchPortException {
        if (!find().contains(portName)) throw new NoSuchPortException(portName);

        SerialPort sp = SerialPort.getCommPort(portName);
        if (sp.isOpen()) {
            sp.setBaudRate(baudRate);
            return this;
        }

        boolean isOpen = sp.openPort();
        if (!isOpen) {
            log.error("open serial port failure {}", portName);
            return null;
        }

        sp.setBaudRate(baudRate);
        sp.setFlowControl(flowCtrl);
        sp.setComPortParameters(baudRate, dataBit, stopBit, parityBit);

        this.serialPort = sp;
        log.info("open serial port success {}", portName);
        return this;
    }

    public synchronized void sendSync(ByteBuf msgBuf) {
        this.send(msgBuf);
    }

    public synchronized void sendSyncAndRelease(ByteBuf msgBuf) {
        this.sendAndRelease(msgBuf);
    }

    public synchronized void sendSync(byte[] content) {
        this.send(content);
    }

    public void send(ByteBuf msgBuf) {
        this.send(ByteBufUtil.getBytes(msgBuf));
    }

    public void sendAndRelease(ByteBuf msgBuf) {
        try {
            this.send(ByteBufUtil.getBytes(msgBuf));
        } finally {
            ReferenceCountUtil.safeRelease(msgBuf);
        }
    }

    public void send(byte[] content) {
        if (serialPort != null && serialPort.isOpen()) serialPort.writeBytes(content, content.length);
    }

    public byte[] read() throws InterruptedException {
        byte[] recvData = null;
        if (serialPort == null || !serialPort.isOpen()) {
            return new byte[0];
        }
        while (serialPort.bytesAvailable() != 0) {
            byte[] readBuffer = new byte[serialPort.bytesAvailable()];
            serialPort.readBytes(readBuffer, readBuffer.length);
            if (recvData == null) {
                recvData = readBuffer;
            } else {
                recvData = addAll(recvData, readBuffer);
            }
            // To read data from the serial port, you must sleep after the serial port is opened and between two readings,
            // otherwise the program will not perceive that there is data in the input stream
            Thread.sleep(10);
        }

        return recvData;
    }

    public void close() {
        if (serialPort != null && serialPort.isOpen()) {
            log.info("close serial port {}", serialPort.getSystemPortName());
            serialPort.closePort();
        }
    }

    /**
     * <p>Adds all the elements of the given arrays into a new array.
     * <p>The new array contains all of the element of {@code array1} followed
     * by all of the elements {@code array2}. When an array is returned, it is always
     * a new array.
     *
     * <pre>
     * ArrayUtils.addAll(array1, null)   = cloned copy of array1
     * ArrayUtils.addAll(null, array2)   = cloned copy of array2
     * ArrayUtils.addAll([], [])         = []
     * </pre>
     *
     * @param array1 the first array whose elements are added to the new array.
     * @param array2 the second array whose elements are added to the new array.
     * @return The new byte[] array.
     * @since 2.1
     */
    private static byte[] addAll(final byte[] array1, final byte... array2) {
        if (array1 == null) {
            return clone(array2);
        } else if (array2 == null) {
            return clone(array1);
        }
        final byte[] joinedArray = new byte[array1.length + array2.length];
        System.arraycopy(array1, 0, joinedArray, 0, array1.length);
        System.arraycopy(array2, 0, joinedArray, array1.length, array2.length);
        return joinedArray;
    }

    /**
     * <p>Clones an array returning a typecast result and handling
     * {@code null}.
     *
     * <p>This method returns {@code null} for a {@code null} input array.
     *
     * @param array the array to clone, may be {@code null}
     * @return the cloned array, {@code null} if {@code null} input
     */
    public static byte[] clone(final byte[] array) {
        if (array == null) {
            return null;
        }
        return array.clone();
    }
}
