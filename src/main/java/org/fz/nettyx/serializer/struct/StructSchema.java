package org.fz.nettyx.serializer.struct;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.fz.nettyx.serializer.struct.StructDefinition.StructField;
import org.fz.nettyx.util.TypeRefer;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.fz.nettyx.serializer.struct.StructFieldHandler.isBasic;
import static org.fz.nettyx.serializer.struct.StructFieldHandler.isStruct;
import static org.fz.nettyx.serializer.struct.StructSerializerContext.getStructDefinition;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024 /1/15 11:24
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class StructSchema {

    final Map<StructField, Class<?>> fields;

    public static <T> StructSchema of(TypeRefer<T> type) {
        Type         root   = type.getTypeValue();
        StructSchema schema = new StructSchema(new HashMap<>(16));

        StructDefinition structDef = getStructDefinition(root);
        if (structDef != null) {
            Stream.of(structDef.fields()).forEach(sf -> getSchema(root, sf, schema.getFields()));
        }

        return schema;
    }

    static void getSchema(final Type root, final StructField structField, Map<StructField, Class<?>> cumulate) {
        Type type = structField.type(root);

        if (isBasic(root, type)) {
            cumulate.put(structField, (Class<?>) type);
        }
        if (isStruct(root, type)) {
            StructDefinition structDef = getStructDefinition(type);
            if (structDef == null) {
                cumulate.put(structField, null);
                return;
            }

            Stream.of(structDef.fields()).forEach(sf -> getSchema(root, sf, cumulate));
        }
    }
}