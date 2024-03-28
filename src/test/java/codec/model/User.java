package codec.model;

import lombok.Data;
import org.fz.nettyx.serializer.struct.annotation.Struct;
import org.fz.nettyx.serializer.struct.basic.c.signed.Cint;

@Data
@Struct
public class User<T, W, G> {


    private Cint         cint;

}