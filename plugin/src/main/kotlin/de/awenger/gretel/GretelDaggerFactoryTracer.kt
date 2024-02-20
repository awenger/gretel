package de.awenger.gretel

import com.android.build.api.instrumentation.ClassContext
import de.awenger.gretel.util.GretelTraceAddingMethodVisitor
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Type

class GretelDaggerFactoryTracer : GretelInstrumentable {

    override fun isInstrumentable(classData: ClassContext): Boolean {
        return classData.currentClassData.interfaces.contains(DAGGER_FACTORY_INTERFACE)
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
                if (name != "get") return super.visitMethod(
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

                val returnClass = Type.getReturnType(descriptor).className.split(".").last()
                val traceName = "Factory<$returnClass>::get"

                val mv = super.visitMethod(access, name, descriptor, signature, exceptions)
                return GretelTraceAddingMethodVisitor(traceName, apiVersion, mv)
            }
        }
    }

    companion object {
        private const val DAGGER_FACTORY_INTERFACE = "dagger.internal.Factory"
    }
}