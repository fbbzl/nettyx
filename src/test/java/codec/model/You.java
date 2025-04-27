package codec.model;

import lombok.Data;
import org.fz.nettyx.serializer.struct.annotation.Struct;
import org.fz.nettyx.serializer.struct.annotation.ToArrayList;
import org.fz.nettyx.serializer.struct.basic.c.signed.*;
import org.fz.nettyx.serializer.struct.basic.c.unsigned.Culong8;

import java.util.List;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2025/4/19 16:48
 */

@Data
@Struct
public class You {

    Cchar   uname;
    Cint    isMarried;
    Cchar   sex;
    Cfloat  address;
    Cdouble platformId;
    @ToArrayList(size = 1)
    List<Clong8> description;
    Culong8 interest;
    Hit     c;
    Cchar   uname1;
    Cint    isMarried1;
    Cchar   sex1;
    Cfloat  address1;
    Cdouble platformId1;
    Cchar   uname2;
    Cint    isMarried2;
    Cchar   sex2;
    Cfloat  address2;
    Cdouble platformId2;
    Cchar   uname3;
    Cint    isMarried3;
    Cchar   sex3;


    @Data
    @Struct
    public static class Hit {
        Culong8 interest;
    }
}
