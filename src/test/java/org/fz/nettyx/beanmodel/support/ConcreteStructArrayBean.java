package org.fz.nettyx.beanmodel.support;

import org.fz.nettyx.serializer.type.annotation.Struct;
import org.fz.nettyx.serializer.type.annotation.ToArray;

@Struct(endian = Struct.Endian.BE)
public class ConcreteStructArrayBean {

    @ToArray(length = 2)
    private BigEndianLeaf[] fixed;

    @ToArray(flexible = true)
    private BigEndianLeaf[] flexible;

    public BigEndianLeaf[] getFixed() {
        return fixed;
    }

    public void setFixed(BigEndianLeaf[] fixed) {
        this.fixed = fixed;
    }

    public BigEndianLeaf[] getFlexible() {
        return flexible;
    }

    public void setFlexible(BigEndianLeaf[] flexible) {
        this.flexible = flexible;
    }
}
