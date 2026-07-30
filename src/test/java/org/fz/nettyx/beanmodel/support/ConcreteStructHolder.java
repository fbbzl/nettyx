package org.fz.nettyx.beanmodel.support;

import org.fz.nettyx.serializer.struct.annotation.Struct;

@Struct(endian = Struct.Endian.BE)
public class ConcreteStructHolder {

    private BigEndianLeaf leaf;

    public BigEndianLeaf getLeaf() {
        return leaf;
    }

    public void setLeaf(BigEndianLeaf leaf) {
        this.leaf = leaf;
    }
}
