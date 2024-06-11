package de.awenger.gretel.bytecode

import com.tschuchort.compiletesting.SourceFile
import de.awenger.gretel.bytecode.TraceNameSegment.*
import de.awenger.gretel.bytecode.testutil.TestEnvironment
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.objectweb.asm.MethodVisitor
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
            .registerMethodVisitor("my.sample.SampleClass") { methodAccess: Int, methodDescriptor: String, methodName: String?, api: Int, nextMethodVisitor: MethodVisitor? ->
                when (methodName) {
                    "addition" -> GretelTraceAddingMethodVisitor(
                        "my.sample.SampleClass",
                        methodAccess,
                        methodName,
                        methodDescriptor,
                        listOf(StaticText("addition-trace")),
                        api,
                        nextMethodVisitor
                    )

                    else -> nextMethodVisitor
                }
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

    @Test
    fun `applying transformation, trace calls should be recorded and the limit of 127 chars should be enforced`() {

        val traceNameGenerator = { length: Int ->
            generateSequence('a') { if (it == 'z') 'a' else it + 1 }.take(length).joinToString(separator = "")
        }

        val environment = TestEnvironment
            .Builder()
            .addSource(SRC_SAMPLE_CLASS, SRC_TRACE_COMPAT)
            .registerMethodVisitor("my.sample.SampleClass") { methodAccess: Int, methodDescriptor: String, methodName: String?, api: Int, nextMethodVisitor: MethodVisitor? ->
                when (methodName) {
                    "addition" -> GretelTraceAddingMethodVisitor(
                        "my.sample.SampleClass",
                        methodAccess,
                        methodName,
                        methodDescriptor,
                        listOf(StaticText(traceNameGenerator(9001))),
                        api,
                        nextMethodVisitor
                    )

                    else -> nextMethodVisitor
                }
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

        val expectedInteractions = listOf("beginSection(${traceNameGenerator(127)})", "endSection()")

        assertEquals(expectedInteractions, actualInteractions)
    }

    @Test
    fun `applying transformation to add trace containing argument values, traces with values should be recorded`() {
        val environment = TestEnvironment
            .Builder()
            .addSource(SRC_SAMPLE_CLASS, SRC_TRACE_COMPAT)
            .registerMethodVisitor("my.sample.SampleClass") { methodAccess: Int, methodDescriptor: String, methodName: String?, api: Int, nextMethodVisitor: MethodVisitor? ->
                when (methodName) {
                    "addition" -> GretelTraceAddingMethodVisitor(
                        "my.sample.SampleClass",
                        methodAccess,
                        methodName,
                        methodDescriptor,
                        listOf(
                            ClassName,
                            StaticText("::"),
                            MethodName,
                            StaticText("("),
                            ArgumentValue(0),
                            StaticText(": "),
                            ArgumentType(0),
                            StaticText(", "),
                            ArgumentValue(1),
                            StaticText(": "),
                            ArgumentType(1),
                            StaticText("): "),
                            ReturnType
                        ),
                        api,
                        nextMethodVisitor
                    )

                    else -> nextMethodVisitor
                }
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

        val expectedInteractions = listOf("beginSection(SampleClass::addition(2: int, 3: int): int)", "endSection()")

        assertEquals(expectedInteractions, actualInteractions)
    }

    @Test
    fun `applying transformation to add trace containing String argument values, traces with values should be recorded`() {
        val environment = TestEnvironment
            .Builder()
            .addSource(SRC_SAMPLE_CLASS, SRC_TRACE_COMPAT)
            .registerMethodVisitor("my.sample.SampleClass") { methodAccess: Int, methodDescriptor: String, methodName: String?, api: Int, nextMethodVisitor: MethodVisitor? ->
                when (methodName) {
                    "concat" -> GretelTraceAddingMethodVisitor(
                        "my.sample.SampleClass",
                        methodAccess,
                        methodName,
                        methodDescriptor,
                        listOf(
                            ClassName,
                            StaticText("::"),
                            MethodName,
                            StaticText("("),
                            ArgumentValue(0),
                            StaticText(": "),
                            ArgumentType(0),
                            StaticText(", "),
                            ArgumentValue(1),
                            StaticText(": "),
                            ArgumentType(1),
                            StaticText("): "),
                            ReturnType
                        ),
                        api,
                        nextMethodVisitor
                    )

                    else -> nextMethodVisitor
                }
            }
            .build()

        val sampleClass = environment.loadClass("my.sample.SampleClass")
        val sampleClassInstance = sampleClass.getDeclaredConstructor().newInstance()
        val additionResult = sampleClass
            .getMethod("concat", String::class.java, String::class.java)
            .invoke(sampleClassInstance, "hello", "world")

        assertEquals("helloworld", additionResult, "\"hello\" + \"world\" should be")

        val traceCompatClass = environment.loadClass("androidx.core.os.TraceCompat")
        val actualInteractions = traceCompatClass.getMethod("getActions").invoke(null)

        val expectedInteractions =
            listOf("beginSection(SampleClass::concat(hello: String, world: String): String)", "endSection()")

        assertEquals(expectedInteractions, actualInteractions)
    }

    companion object {
        val SRC_SAMPLE_CLASS: SourceFile = SourceFile.kotlin(
            name = "SampleClass.kt",
            contents = """
                package my.sample
                
                class SampleClass {
                    fun addition(x: Int, y: Int) = x + y
                    fun concat(x: String, y: String) = x + y
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