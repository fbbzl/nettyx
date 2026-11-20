package org.fz.nettyx.channel.serial;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
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

    @Getter
    @Accessors(fluent = true)
    private final int value;

    public static SerialDataBits valueOf(int value) {
        for (SerialDataBits dataBit : values()) if (dataBit.value == value) return dataBit;

        throw new UnknownConfigException(SerialDataBits.class.getSimpleName(), value);
    }
}
