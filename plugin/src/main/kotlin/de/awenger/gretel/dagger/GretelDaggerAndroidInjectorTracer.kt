package de.awenger.gretel.dagger

import com.android.build.api.instrumentation.ClassContext
import de.awenger.gretel.GretelInstrumentable
import de.awenger.gretel.GretelTransformations
import de.awenger.gretel.util.GretelTraceAddingMethodVisitor
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Type

class GretelDaggerAndroidInjectorTracer : GretelInstrumentable {

    override fun isInstrumentable(classData: ClassContext): Boolean {
        return classData.currentClassData.interfaces.contains(DAGGER_ANDROID_INJECTOR_INTERFACE)
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

                if (name != METHOD) return nextMethodVisitor
                if (access and 0x1000 == 0x1000) return nextMethodVisitor

                val argument = Type.getArgumentTypes(descriptor).firstOrNull() ?: return nextMethodVisitor
                val type = argument.className.split(".").last()
                val traceName = "AndroidInjector<$type>::inject"

                return GretelTraceAddingMethodVisitor(traceName, apiVersion, nextMethodVisitor)
            }
        }
    }

    companion object {
        private const val DAGGER_ANDROID_INJECTOR_INTERFACE = "dagger.android.AndroidInjector"
        private const val METHOD = "inject"
    }
}