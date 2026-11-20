package org.fz.nettyx.serializer.struct;

import org.fz.nettyx.exception.SerializeException;
import org.fz.nettyx.exception.StructDefinitionException;
import org.fz.nettyx.invalidmodel.cycle.CycleStructs;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Checks detection of cyclic struct references, including those introduced through generic arguments or arrays.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2026-07-29
 */
public class StructCycleValidationTest {

    @BeforeClass
    public static void init() {
        new StructContext("package.that.does.not.exist");
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
        StructScannerTestSupport.invoke("scanStruct", Set.class, types(CycleStructs.CrossBatchA.class));

        assertCycle("CrossBatchA.b -> CrossBatchB.a -> CrossBatchA", CycleStructs.CrossBatchB.class);
    }

    @Test
    public void acceptsReferenceToStructWithoutDefinition() {
        StructScannerTestSupport.invoke("scanStruct", Set.class, types(CycleStructs.MissingDefinitionRoot.class));

        assertNotNull(StructContext.getStructDefinition(CycleStructs.MissingDefinitionRoot.class));
        assertNull(StructContext.getStructDefinition(CycleStructs.MissingDefinitionLeaf.class));
    }

    @Test
    public void rejectsToArrayOnNonArrayFieldBeforeCycleAnalysis() {
        assertThrows(SerializeException.class,
                     () -> StructScannerTestSupport.invoke("scanStruct", Set.class,
                                                          types(CycleStructs.NonArrayAnnotation.class)));
        assertNull(StructContext.getStructDefinition(CycleStructs.NonArrayAnnotation.class));
    }

    @Test
    public void acceptsAcyclicStructGraph() {
        StructScannerTestSupport.invoke("scanStruct", Set.class,
                                        types(CycleStructs.AcyclicRoot.class,
                                              CycleStructs.AcyclicBranch.class,
                                              CycleStructs.AcyclicLeaf.class));

        assertNotNull(StructContext.getStructDefinition(CycleStructs.AcyclicRoot.class));
    }

    private static void assertCycle(String expectedPath, Class<?>... types) {
        StructDefinitionException exception = assertThrows(StructDefinitionException.class,
                                                            () -> StructScannerTestSupport.invoke("scanStruct", Set.class,
                                                                                                 types(types)));
        assertTrue(exception.getMessage(), exception.getMessage().contains(expectedPath));
        for (Class<?> type : types) assertNull(StructContext.getStructDefinition(type));
    }

    private static Set<Class<?>> types(Class<?>... types) {
        return new LinkedHashSet<>(Set.of(types));
    }
}