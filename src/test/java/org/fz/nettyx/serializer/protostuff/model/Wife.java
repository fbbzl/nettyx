package org.fz.nettyx.serializer.protostuff.model;

import lombok.Data;



@Data

public class Wife<I, V> {

    private I      intt;

    private String name;

    private V vv;
}
