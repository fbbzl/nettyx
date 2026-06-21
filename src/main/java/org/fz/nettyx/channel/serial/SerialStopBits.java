package org.fz.nettyx.channel.serial;

import lombok.RequiredArgsConstructor;
import org.fz.nettyx.exception.UnknownConfigException;

/**
 * Common stop bits for serial device connections.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2024/3/1 14:44
 */
@RequiredArgsConstructor
public enum SerialStopBits {

    STOP_BITS_1(1),
    STOP_BITS_2(2),
    STOP_BITS_1_5(3);

    private final int value;

    public int value() {
        return value;
    }

    public static SerialStopBits valueOf(int value) {
        for (SerialStopBits stopBit : values()) {
            if (stopBit.value == value) {
                return stopBit;
            }
        }
        throw new UnknownConfigException(SerialStopBits.class.getSimpleName(), value);
    }
}
