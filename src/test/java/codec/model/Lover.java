package codec.model;

import lombok.Data;
import org.fz.nettyx.serializer.struct.annotation.Struct;
import org.fz.nettyx.serializer.struct.basic.c.signed.cshort;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2025/9/26 0:32
 */
@Struct
@Data
public class Lover {
    cshort uname;
}
