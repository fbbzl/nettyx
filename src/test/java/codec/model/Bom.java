package codec.model;

import lombok.Data;
import org.fz.nettyx.serializer.struct.annotation.Struct;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/3/19 11:17
 */

@Data
@Struct
public class Bom<T > {

    private T  t;


}
