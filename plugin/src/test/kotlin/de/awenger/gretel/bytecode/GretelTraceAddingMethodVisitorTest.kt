package de.awenger.gretel.bytecode

import com.tschuchort.compiletesting.SourceFile
import de.awenger.gretel.bytecode.testutil.TestEnvironment
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.objectweb.asm.test.AsmTest


class GretelTraceAddingMethodVisitorTest : AsmTest() {

    @Test
    fun `without transformation, no trace should be recorded`() {
        val environment = TestEnvironment
            .Builder()
            .addSource(SRC_SAMPLE_CLASS, SRC_TRACE_COMPAT)
            .build()

        val sampleClass = environment.loadClass("my.sample.SampleClass")
        val sampleClassInstance = sampleClass.getDeclaredConstructor().newInstance()
        val additionResult = sampleClass
            .getMethod("addition", Int::class.java, Int::class.java)
            .invoke(sampleClassInstance, 2, 3)

        assertEquals(5, additionResult, "2 + 3 should be")

        val traceCompatClass = environment.loadClass("androidx.core.os.TraceCompat")
        val actualInteractions = traceCompatClass.getMethod("getActions").invoke(null)

        val expectedInteractions = emptyList<String>()

        assertEquals(expectedInteractions, actualInteractions)
    }

    @Test
    fun `applying transformation, trace calls should be recorded`() {
        val environment = TestEnvironment
            .Builder()
            .addSource(SRC_SAMPLE_CLASS, SRC_TRACE_COMPAT)
            .registerMethodVisitor("my.sample.SampleClass", "addition") { api, nextVisitor ->
                GretelTraceAddingMethodVisitor("addition-trace", api, nextVisitor)
            }
            .build()

        val sampleClass = environment.loadClass("my.sample.SampleClass")
        val sampleClassInstance = sampleClass.getDeclaredConstructor().newInstance()
        val additionResult = sampleClass
            .getMethod("addition", Int::class.java, Int::class.java)
            .invoke(sampleClassInstance, 2, 3)

        assertEquals(5, additionResult, "2 + 3 should be")

        val traceCompatClass = environment.loadClass("androidx.core.os.TraceCompat")
        val actualInteractions = traceCompatClass.getMethod("getActions").invoke(null)

        val expectedInteractions = listOf("beginSection(addition-trace)", "endSection()")

        assertEquals(expectedInteractions, actualInteractions)
    }

    companion object {
        val SRC_SAMPLE_CLASS: SourceFile = SourceFile.kotlin(
            name = "SampleClass.kt",
            contents = """
                package my.sample
                
                class SampleClass {
                    fun addition(x: Int, y: Int) = x + y
                }
            """.trimIndent()
        )

        val SRC_TRACE_COMPAT: SourceFile = SourceFile.java(
            name = "TraceCompat.java",
            contents = """
                package androidx.core.os;
                
                import java.util.LinkedList;
                import java.util.List;
                
                public final class TraceCompat {
                
                    static {
                        actions = new LinkedList<>();
                    }
                
                    private static final List<String> actions;
                
                    public static List<String> getActions() {
                        return actions;
                    }
                
                    public static void beginSection(String sectionName) {
                        actions.add("beginSection(" + sectionName + ")");
                    }
                
                    public static void endSection() {
                        actions.add("endSection()");
                    }
                }
            """.trimIndent()
        )
    }
}