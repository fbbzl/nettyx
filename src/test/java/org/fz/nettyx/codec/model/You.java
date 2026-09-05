package org.fz.nettyx.codec.model;

import lombok.Data;
import org.fz.nettyx.serializer.type.annotation.Chunk;
import org.fz.nettyx.serializer.type.annotation.Struct;
import org.fz.nettyx.serializer.type.annotation.ToArray;
import org.fz.nettyx.serializer.type.basic.c.signed.cchar;
import org.fz.nettyx.serializer.type.basic.c.signed.cdouble;
import org.fz.nettyx.serializer.type.basic.c.signed.cfloat;
import org.fz.nettyx.serializer.type.basic.c.signed.cint;
import org.fz.nettyx.serializer.type.basic.c.unsigned.cuchar;
import org.fz.nettyx.serializer.type.basic.c.unsigned.culong8;

import static org.fz.nettyx.serializer.type.annotation.Struct.Endian.BE;
import static org.fz.nettyx.serializer.type.annotation.Struct.Endian.LE;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2025/4/19 16:48
 */

@Data
@Struct(endian = BE)
public class You {
    @Chunk(length = 20)
    byte[]  chunk;
    cchar   uname;
    cint   isMarried;
    cuchar sex;
    cfloat address;
    cdouble platformId;
    @ToArray(length = 5)
    Hit[] description;
    culong8 interest;
    Hit     c;
    cchar   uname1;
    cint    isMarried1;
    cchar   sex1;
    cfloat  address1;
    cdouble platformId1;
    cchar   uname2;
    cint    isMarried2;
    cchar   sex2;
    cfloat  address2;


    @Data
    @Struct(endian = LE)
    public static class Hit {
        culong8 interest;
    }
}
