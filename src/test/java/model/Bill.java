package model;

import lombok.Data;
import org.fz.nettyx.serializer.struct.annotation.Struct;
import org.fz.nettyx.serializer.struct.basic.c.unsigned.Cuchar;

@Data
@Struct
public class Bill {

    private Cuchar bid;

    @Override
    public String toString() {
        return "Bill{" + "bid='" + bid + '\'' + '}';
    }
}






