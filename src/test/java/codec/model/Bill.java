package codec.model;

import lombok.Data;
import org.fz.nettyx.serializer.struct.annotation.Struct;
import org.fz.nettyx.serializer.struct.annotation.ToNamedEnum;
import org.fz.nettyx.serializer.struct.annotation.ToString;
import org.fz.nettyx.serializer.struct.basic.c.unsigned.Cuchar;

@Data
@Struct
public class Bill {

    private Cuchar bid;

    @ToString(bufferLength = 4)
    private String orgName;

    @ToNamedEnum(enumType = BillType.class,bufferLength = 4)
    private BillType billType;

    @Override
    public String toString() {
        return "Bill{" + "bid='" + bid + '\'' + '}';
    }

    public enum BillType {
        NORMAL, VIP,CCCC,
        ;
    }
}






