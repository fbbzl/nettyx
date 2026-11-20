package org.fz.nettyx.channel.serial;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
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

    @Getter
    @Accessors(fluent = true)
    private final int value;

    public static SerialParityBit valueOf(int value) {
        for (SerialParityBit parityBit : values()) if (parityBit.value == value) return parityBit;

        throw new UnknownConfigException(SerialParityBit.class.getSimpleName(), value);
    }
}
