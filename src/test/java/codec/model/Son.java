package codec.model;

import lombok.Data;
import org.fz.nettyx.serializer.typed.annotation.Struct;

@Data
@Struct
public class Son<B, Y> {

    private B name;
    private Y sonOrder;

}
