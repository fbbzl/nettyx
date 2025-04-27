package org.fz.nettyx.serializer.struct;

import cn.hutool.core.util.TypeUtil;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.fz.nettyx.serializer.struct.StructDefinition.StructField;
import org.fz.nettyx.util.TypeRefer;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static org.fz.nettyx.serializer.struct.StructHelper.getRawType;
import static org.fz.nettyx.serializer.struct.StructSerializerContext.getStructDefinition;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024 /1/15 11:24
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class StructSchema {

    final Map<StructField, Type> classMap;

    public static <T> StructSchema of(TypeRefer<T> type) {
        StructSchema schema = new StructSchema(new HashMap<>(16));

        StructDefinition structDef = getStructDefinition(getRawType(type));

        for (StructField structField : structDef.fields()) {
            Field                      field           = null;
            Function<Object, Object>   getter          = structField.getter();
            BiConsumer<Object, Object> setter          = structField.setter();
            Annotation                 annotation      = structField.annotation();
            StructFieldHandler<?>      handler         = structField.handler();
            Type                       fieldActualType = TypeUtil.getActualType(type, field);

            schema.getClassMap().put(null, fieldActualType);
        }
        return schema;
    }




}