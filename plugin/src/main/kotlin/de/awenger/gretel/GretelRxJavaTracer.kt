package de.awenger.gretel

import com.android.build.api.instrumentation.ClassContext
import de.awenger.gretel.util.GretelTraceAddingMethodVisitor
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor

class GretelRxJavaTracer : GretelInstrumentable {

    override fun isInstrumentable(classData: ClassContext): Boolean {
        return classData.currentClassData.interfaces.any { interf ->
            RX_FUNCTION_INTERFACES.any { it.first == interf }
        }
    }

    override fun createClassVisitor(
        classContext: ClassContext,
        apiVersion: Int,
        nextClassVisitor: ClassVisitor,
        parameters: GretelTransformations.Parameters,
    ): ClassVisitor {
        return object : ClassVisitor(apiVersion, nextClassVisitor) {

            override fun visitMethod(
                access: Int,
                name: String?,
                descriptor: String?,
                signature: String?,
                exceptions: Array<out String>?,
            ): MethodVisitor {
                val classInterfaces = classContext.currentClassData.interfaces
                val methodNames = RX_FUNCTION_INTERFACES
                    .filter { (interf, _) -> classInterfaces.contains(interf) }
                    .map { (_, method) -> method }

                if (methodNames.contains(name).not()) return super.visitMethod(
                    access,
                    name,
                    descriptor,
                    signature,
                    exceptions,
                )
                if (access and 0x1000 == 0x1000) return super.visitMethod(
                    access,
                    name,
                    descriptor,
                    signature,
                    exceptions,
                )

                val className = classContext.currentClassData.className.split(".").last()
                val traceName = "$className::$name(rx)"

                val mv = super.visitMethod(access, name, descriptor, signature, exceptions)
                return GretelTraceAddingMethodVisitor(traceName, apiVersion, mv)
            }
        }
    }

    companion object {
        private val RX1_FUNCTION_INTERFACES = listOf(
            "rx.functions.Function" to "call"
        )
        private val RX2_FUNCTION_INTERFACES = listOf(
            "io.reactivex.functions.Action" to "run",
            "io.reactivex.functions.Consumer" to "accept",
            "io.reactivex.functions.BiConsumer" to "accept",
            "io.reactivex.functions.Predicate" to "test",
            "io.reactivex.functions.BiPredicate" to "test",
            "io.reactivex.functions.Function" to "apply",
            "io.reactivex.functions.BiFunction" to "apply",
            "io.reactivex.functions.Function3" to "apply",
            "io.reactivex.functions.Function4" to "apply",
            "io.reactivex.functions.Function5" to "apply",
            "io.reactivex.functions.Function6" to "apply",
            "io.reactivex.functions.Function7" to "apply",
            "io.reactivex.functions.Function8" to "apply",
            "io.reactivex.functions.Function9" to "apply",
            "io.reactivex.functions.IntFunction" to "apply",
            "io.reactivex.functions.LongConsumer" to "accept",
            "io.reactivex.functions.BooleanSupplier" to "getAsBoolean",
        )

        private val RX3_FUNCTION_INTERFACES = listOf(
            "io.reactivex.rxjava3.functions.Action" to "run",
            "io.reactivex.rxjava3.functions.Consumer" to "accept",
            "io.reactivex.rxjava3.functions.BiConsumer" to "accept",
            "io.reactivex.rxjava3.functions.Predicate" to "test",
            "io.reactivex.rxjava3.functions.BiPredicate" to "test",
            "io.reactivex.rxjava3.functions.Function" to "apply",
            "io.reactivex.rxjava3.functions.BiFunction" to "apply",
            "io.reactivex.rxjava3.functions.Function3" to "apply",
            "io.reactivex.rxjava3.functions.Function4" to "apply",
            "io.reactivex.rxjava3.functions.Function5" to "apply",
            "io.reactivex.rxjava3.functions.Function6" to "apply",
            "io.reactivex.rxjava3.functions.Function7" to "apply",
            "io.reactivex.rxjava3.functions.Function8" to "apply",
            "io.reactivex.rxjava3.functions.Function9" to "apply",
            "io.reactivex.rxjava3.functions.IntFunction" to "apply",
            "io.reactivex.rxjava3.functions.LongConsumer" to "accept",
            "io.reactivex.rxjava3.functions.Supplier" to "get",
            "io.reactivex.rxjava3.functions.BooleanSupplier" to "getAsBoolean",
        )

        private val RX_FUNCTION_INTERFACES = RX1_FUNCTION_INTERFACES + RX2_FUNCTION_INTERFACES + RX3_FUNCTION_INTERFACES
    }
}