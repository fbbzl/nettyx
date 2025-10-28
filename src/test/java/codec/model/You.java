package codec.model;

import lombok.Data;
import org.fz.nettyx.serializer.struct.annotation.Chunk;
import org.fz.nettyx.serializer.struct.annotation.Struct;
import org.fz.nettyx.serializer.struct.annotation.ToArray;
import org.fz.nettyx.serializer.struct.basic.c.signed.cchar;
import org.fz.nettyx.serializer.struct.basic.c.signed.cdouble;
import org.fz.nettyx.serializer.struct.basic.c.signed.cfloat;
import org.fz.nettyx.serializer.struct.basic.c.signed.cint;
import org.fz.nettyx.serializer.struct.basic.c.unsigned.cuchar;
import org.fz.nettyx.serializer.struct.basic.c.unsigned.culong8;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2025/4/19 16:48
 */

@Data
@Struct
public class You {
    @Chunk(length = 20)
    byte[]  chunk;
    cchar   uname;
    cint   isMarried;
    cuchar sex;
    cfloat address;
    cdouble platformId;
    @ToArray(length = 6)
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
    @Struct
    public static class Hit {
        culong8 interest;
    }
}
