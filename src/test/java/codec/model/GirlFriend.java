package codec.model;

import lombok.Data;
import org.fz.nettyx.serializer.typed.annotation.Struct;
import org.fz.nettyx.serializer.typed.annotation.ToString;

@Data
@Struct
public class GirlFriend {

    @ToString(bufferLength = 2)
    private String cup;
}