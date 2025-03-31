package codec.model;

import lombok.Data;
import org.fz.nettyx.serializer.struct.annotation.Struct;
import org.fz.nettyx.serializer.struct.annotation.ToCharSequence;


@Data
@Struct
public class GirlFriend {

    @ToCharSequence(bufferLength = 2)
    private String cup;
}