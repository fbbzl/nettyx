package org.fz.nettyx.util;

import cn.hutool.core.util.TypeUtil;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.fz.nettyx.serializer.struct.StructDefinition;
import org.fz.nettyx.serializer.struct.StructDefinition.StructField;
import org.fz.nettyx.serializer.struct.StructFieldHandler;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static org.fz.nettyx.serializer.struct.StructHelper.getRawType;
import static org.fz.nettyx.serializer.struct.StructHelper.getStructDefinition;

/**
 * type parameter at the time of serialization
 *
 * @param <T> the type parameter
 * @author fengbinbin
 * @version 1.0
 * @since 2024 /1/15 11:24
 */
@Getter
public abstract class TypeRefer<T> implements Type {

    private final Type typeValue = TypeUtil.getTypeArgument(this.getClass());

    public TypeInspector getTypeInspector() {
        return TypeInspector.doInspect(this.getTypeValue());
    }

    @Override
    public String toString() {
        return "TypeRefer(typeValue=" + this.getTypeValue() + ")";
    }

    @Getter
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class TypeInspector {

        final Type             root;
        final Map<Field, Type> classMap;

        public static TypeInspector doInspect(Type type) {
            TypeInspector typeInspector = new TypeInspector(type, new HashMap<>(16));

            StructDefinition structDef = getStructDefinition(getRawType(type));

            for (StructField structField : structDef.fields()) {
                Field                      field           = structField.getWrapped();
                Function<Object, Object>   getter          = structField.getGetter();
                BiConsumer<Object, Object> setter          = structField.getSetter();
                Annotation                 annotation      = structField.getAnnotation();
                StructFieldHandler<?>      handler         = structField.getStructFieldHandler();
                Type                       fieldActualType = TypeUtil.getActualType(type, field);

                typeInspector.getClassMap().put(field, fieldActualType);
            }

            return typeInspector;
        }
    }
}
