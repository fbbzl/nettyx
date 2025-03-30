package codec.model;

import lombok.Data;
import org.fz.nettyx.serializer.struct.annotation.Struct;
import org.fz.nettyx.serializer.struct.annotation.ToCharSequence;


@Data
@Struct
public class Wife<I, V> {

    private I      intt;
    @ToCharSequence(bufferLength = 2)
    private String name;

    private V vv;
}