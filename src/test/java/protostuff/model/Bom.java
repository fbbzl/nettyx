package protostuff.model;

import lombok.Data;



/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/3/19 11:17
 */

@Data

public class Bom<T, W, U> {

    private T t;
    private W gg;
    private U mm;

}
