package org.fz.nettyx.channel.serial;

import lombok.RequiredArgsConstructor;
import org.fz.nettyx.exception.UnknownConfigException;

/**
 * Common data bits for serial device connections.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2024/3/1 14:44
 */
@RequiredArgsConstructor
public enum SerialDataBits {

    DATA_BITS_5(5),
    DATA_BITS_6(6),
    DATA_BITS_7(7),
    DATA_BITS_8(8);

    private final int value;

    public int value() {
        return value;
    }

    public static SerialDataBits valueOf(int value) {
        for (SerialDataBits dataBit : values()) {
            if (dataBit.value == value) {
                return dataBit;
            }
        }
        throw new UnknownConfigException(SerialDataBits.class.getSimpleName(), value);
    }
}
