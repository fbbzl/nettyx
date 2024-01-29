package org.fz.nettyx.serializer.xml.element;

import lombok.Data;

import java.util.List;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/1/30 15:19
 */

@Data
public class Model {

    private CounterType counterType;
    private List<Prop> props;

    public enum CounterType {
        RELATIVE,
        ABSOLUTE,
        ;
    }
}
