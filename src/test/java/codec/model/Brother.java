package codec.model;

import lombok.Data;
import org.fz.nettyx.serializer.struct.annotation.Struct;
import org.fz.nettyx.serializer.struct.annotation.Struct.Endian;
import org.fz.nettyx.serializer.struct.annotation.ToArray;
import org.fz.nettyx.serializer.struct.basic.c.signed.cint;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2025/9/26 0:41
 */
@Struct(endian = Endian.LE)
@Data
public class Brother {
//    @ToArray(flexible = true)
//    cint[] ages;

    @ToArray(flexible = true)
    Son<Lover, cint>[] sons;

//    @ToArray(flexible = true)
//    Lover[] lovers;

}
