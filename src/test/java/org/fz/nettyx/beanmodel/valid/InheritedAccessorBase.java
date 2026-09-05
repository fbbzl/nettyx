package org.fz.nettyx.beanmodel.valid;

import org.fz.nettyx.serializer.type.basic.c.signed.cint;

public class InheritedAccessorBase {
    private cint inherited;

    public cint getInherited() {
        return inherited;
    }

    public void setInherited(cint inherited) {
        this.inherited = inherited;
    }
}
