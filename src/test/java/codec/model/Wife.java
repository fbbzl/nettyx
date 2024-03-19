package codec.model;

import lombok.Data;
import org.fz.nettyx.serializer.struct.annotation.Struct;
import org.fz.nettyx.serializer.struct.annotation.ToString;

@Data
@Struct
public class Wife<V> {

    @ToString(bufferLength = 2)
    private String name;

    private V vv;
}