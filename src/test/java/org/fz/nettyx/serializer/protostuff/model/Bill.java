package org.fz.nettyx.serializer.protostuff.model;

import lombok.Data;



@Data

public class Bill {

    private char bid;

    private String orgName;


    private BillType billType;

    public enum BillType {
        C,
        CC,
        CCC,
        CCCC, CCCCC,
        ;
    }
}
