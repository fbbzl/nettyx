package codec.model;

import lombok.Data;
import org.fz.nettyx.serializer.struct.annotation.Struct;
import org.fz.nettyx.serializer.struct.annotation.ToArray;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2025/9/26 0:41
 */
@Struct
@Data
public class Brother {
//    @ToArray(flexible = true)
//    cint[] ages;

//    @ToArray(flexible = true)
//    Son<cint, cint>[] sons;

    @ToArray(flexible = true)
    Lover[] lovers;

}
