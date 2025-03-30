package codec.model;

import lombok.Data;
import org.fz.nettyx.serializer.struct.annotation.Struct;
import org.fz.nettyx.serializer.struct.annotation.ToCharSequence;
import org.fz.nettyx.serializer.struct.annotation.ToNamedEnum;
import org.fz.nettyx.serializer.struct.basic.c.unsigned.Cuchar;


@Data
@Struct
public class Bill {

    private Cuchar bid;

    @ToCharSequence(bufferLength = 4)
    private String orgName;

    @ToNamedEnum(enumType = BillType.class, bufferLength = 3)
    private BillType billType;

    public enum BillType {
        C,
        CC,
        CCC,
        CCCC, CCCCC,
        ;
    }
}






