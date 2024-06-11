package de.awenger.gretel

import com.android.build.api.instrumentation.ClassContext
import de.awenger.gretel.bytecode.GretelTraceAddingMethodVisitor
import de.awenger.gretel.bytecode.TraceNameSegment
import de.awenger.gretel.config.AddTraceSpec
import de.awenger.gretel.util.matches
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Type

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
                name: String,
                descriptor: String,
                signature: String?,
                exceptions: Array<out String>?,
            ): MethodVisitor {
                val nextMethodVisitor = super.visitMethod(access, name, descriptor, signature, exceptions)

                if (access and 0x1000 == 0x1000) return nextMethodVisitor

                val methodSpecs = tracingSpec.methods.get()

                val methodMatches = methodSpecs.isEmpty() || methodSpecs.any { it.matches(name) }
                if (!methodMatches) return nextMethodVisitor

                val includeArgumentValues = tracingSpec.trace.orNull?.includeArgumentValues?.orNull ?: false

                return GretelTraceAddingMethodVisitor(
                    className = classContext.currentClassData.className,
                    methodAccess = access,
                    methodName = name,
                    descriptor = descriptor,
                    traceNameSegments = generateTraceNameSegments(descriptor, includeArgumentValues),
                    apiVersion = apiVersion,
                    nextMethodVisitor = nextMethodVisitor
                )
            }
        }
    }

    private fun generateTraceNameSegments(descriptor: String, includeArgumentValues: Boolean) = buildList {
        add(TraceNameSegment.ClassName)
        add(TraceNameSegment.StaticText("::"))
        add(TraceNameSegment.MethodName)
        add(TraceNameSegment.StaticText("("))

        val args = Type.getArgumentTypes(descriptor)
        repeat(args.size) { index ->
            if (includeArgumentValues) {
                add(TraceNameSegment.ArgumentValue(index))
                add(TraceNameSegment.StaticText(": "))
            }
            add(TraceNameSegment.ArgumentType(index))
            if (index != args.lastIndex) add(TraceNameSegment.StaticText(", "))
        }

        add(TraceNameSegment.StaticText("): "))
        add(TraceNameSegment.ReturnType)
    }

}
