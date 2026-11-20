package org.fz.nettyx.serializer.struct.comparison;

import com.google.common.reflect.TypeToken;
import io.leangen.geantyref.GenericTypeReflector;
import org.apache.commons.lang3.reflect.TypeUtils;
import org.springframework.core.ResolvableType;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * Test-only calls to public library APIs, without repairing their returned types.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2026-11-11
 */
public final class GenericTypeCandidates {

    /**
     * Identifies a generic type resolution strategy used in tests and benchmarks.
     *
     * @author fengbinbin
     * @version 1.0
     * @since 2026-11-11
     */
    public enum Provider { GEANTYREF, GUAVA, COMMONS_LANG, SPRING }

    private GenericTypeCandidates() {}

    public static Type resolve(Provider provider, Type root, Type declared, Class<?> declaration) {
        return switch (provider) {
            case GEANTYREF -> GenericTypeReflector.resolveType(declared, root);
            case GUAVA -> TypeToken.of(root).resolveType(declared).getType();
            case COMMONS_LANG -> {
                Map<java.lang.reflect.TypeVariable<?>, Type> arguments = TypeUtils.getTypeArguments(root, declaration);
                yield TypeUtils.unrollVariables(arguments, declared);
            }
            // getType preserves the backing declaration, which may remain unresolved.
            // Resolving to Class would erase the nested contract used by StructSerializer.
            case SPRING -> ResolvableType.forType(declared, ResolvableType.forType(root)).getType();
        };
    }
}
