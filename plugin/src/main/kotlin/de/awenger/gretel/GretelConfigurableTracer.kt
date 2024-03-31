package de.awenger.gretel

import com.android.build.api.instrumentation.ClassContext
import de.awenger.gretel.config.AddTraceSpec
import de.awenger.gretel.util.GretelTraceAddingMethodVisitor
import de.awenger.gretel.util.matches
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor

class GretelConfigurableTracer(
    val tracingSpec: AddTraceSpec
) : GretelInstrumentable {

    override fun isInstrumentable(classData: ClassContext): Boolean {
        val classSpec = tracingSpec.classes.orNull ?: return true
        return classSpec.matches(classData.currentClassData)
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
                val nextMethodVisitor = super.visitMethod(access, name, descriptor, signature, exceptions)

                if (access and 0x1000 == 0x1000) return nextMethodVisitor

                val methodSpecs = tracingSpec.methods.get()

                val methodMatches = methodSpecs.isEmpty() || methodSpecs.any { it.matches(name) }
                if (!methodMatches) return nextMethodVisitor

                val className = classContext.currentClassData.className.split('.').last()
                val traceName = buildString {
                    append(tracingSpec.trace.orNull?.prefix?.orNull ?: "")
                    append(tracingSpec.trace.orNull?.name?.orNull?.takeIf { it.isNotBlank() } ?: "$className::$name")
                    append(tracingSpec.trace.orNull?.suffix?.orNull ?: "")
                }.take(TRACE_NAME_MAX_LENGTH)

                // println("- adding trace to ${classContext.currentClassData.className}::$name: '$traceName'")
                return GretelTraceAddingMethodVisitor(traceName, apiVersion, nextMethodVisitor)
            }
        }
    }

    companion object {
        private const val TRACE_NAME_MAX_LENGTH = 127
    }
}
