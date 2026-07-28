package org.fz.nettyx.serializer.struct;

import org.fz.nettyx.exception.StructDefinitionException;
import org.fz.nettyx.invalidmodel.cycle.CycleStructs;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.Assert.*;

public class StructCycleValidationTest {

    private static ExposedContext context;

    @BeforeClass
    public static void init() {
        context = new ExposedContext();
    }

    @Test
    public void rejectsDirectSelfReference() {
        assertCycle("Self.value -> Self", CycleStructs.Self.class);
    }

    @Test
    public void rejectsMutualReference() {
        assertCycle("MutualA.b -> MutualB.a -> MutualA",
                    CycleStructs.MutualA.class, CycleStructs.MutualB.class);
    }

    @Test
    public void rejectsMultiLevelReference() {
        assertCycle("LevelA.b -> LevelB.c -> LevelC.a -> LevelA",
                    CycleStructs.LevelA.class, CycleStructs.LevelB.class, CycleStructs.LevelC.class);
    }

    @Test
    public void rejectsStructArrayReference() {
        assertCycle("ArraySelf.children -> ArraySelf", CycleStructs.ArraySelf.class);
    }

    @Test
    public void rejectsParameterizedSelfReference() {
        assertCycle("GenericSelf.next -> GenericSelf", CycleStructs.GenericSelf.class);
    }

    @Test
    public void rejectsReferenceResolvedFromGenericArgument() {
        assertCycle("GenericOwner.box -> GenericBox.value -> GenericOwner",
                    CycleStructs.GenericOwner.class, CycleStructs.GenericBox.class);
    }

    @Test
    public void rejectsArrayReferenceResolvedFromGenericArgument() {
        assertCycle("GenericArrayOwner.box -> GenericArrayBox.values -> GenericArrayOwner",
                    CycleStructs.GenericArrayOwner.class, CycleStructs.GenericArrayBox.class);
    }

    @Test
    public void rejectsCycleCompletedByLaterScan() {
        context.scanStructTypes(types(CycleStructs.CrossBatchA.class));

        assertCycle("CrossBatchA.b -> CrossBatchB.a -> CrossBatchA", CycleStructs.CrossBatchB.class);
    }

    @Test
    public void acceptsReferenceToStructWithoutDefinition() {
        context.scanStructTypes(types(CycleStructs.MissingDefinitionRoot.class));

        assertNotNull(StructSerializerContext.getStructDefinition(CycleStructs.MissingDefinitionRoot.class));
        assertNull(StructSerializerContext.getStructDefinition(CycleStructs.MissingDefinitionLeaf.class));
    }

    @Test
    public void ignoresNonArrayToArrayFieldDuringCycleAnalysis() {
        context.scanStructTypes(types(CycleStructs.NonArrayAnnotation.class));

        assertNotNull(StructSerializerContext.getStructDefinition(CycleStructs.NonArrayAnnotation.class));
    }

    @Test
    public void acceptsAcyclicStructGraph() {
        context.scanStructTypes(types(CycleStructs.AcyclicRoot.class,
                                      CycleStructs.AcyclicBranch.class,
                                      CycleStructs.AcyclicLeaf.class));

        assertNotNull(StructSerializerContext.getStructDefinition(CycleStructs.AcyclicRoot.class));
    }

    private static void assertCycle(String expectedPath, Class<?>... types) {
        StructDefinitionException exception = assertThrows(StructDefinitionException.class,
                                                            () -> context.scanStructTypes(types(types)));
        assertTrue(exception.getMessage(), exception.getMessage().contains(expectedPath));
        for (Class<?> type : types) assertNull(StructSerializerContext.getStructDefinition(type));
    }

    private static Set<Class<?>> types(Class<?>... types) {
        return new LinkedHashSet<>(Set.of(types));
    }

    private static class ExposedContext extends StructSerializerContext {
        ExposedContext() {
            super("package.that.does.not.exist");
        }

        void scanStructTypes(Set<Class<?>> types) {
            super.scanStruct(types);
        }
    }
}
