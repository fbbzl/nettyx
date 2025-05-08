package protostuff.model;

import lombok.Data;



@Data

public class Son<B, Y> {

    private B name;
    private Y sonOrder;

}
