package org.fz.nettyx.channel.serial;

import lombok.RequiredArgsConstructor;
import org.fz.nettyx.exception.UnknownConfigException;

/**
 * Common parity bits for serial device connections.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2024/3/1 14:44
 */
@RequiredArgsConstructor
public enum SerialParityBit {

    NO(0),
    ODD(1),
    EVEN(2),
    MARK(3),
    SPACE(4);

    private final int value;

    public int value() {
        return value;
    }

    public static SerialParityBit valueOf(int value) {
        for (SerialParityBit parityBit : values()) {
            if (parityBit.value == value) {
                return parityBit;
            }
        }
        throw new UnknownConfigException(SerialParityBit.class.getSimpleName(), value);
    }
}
