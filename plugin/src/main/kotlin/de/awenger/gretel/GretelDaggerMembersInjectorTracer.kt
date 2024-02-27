package de.awenger.gretel

import com.android.build.api.instrumentation.ClassContext
import de.awenger.gretel.util.GretelTraceAddingMethodVisitor
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Type

class GretelDaggerMembersInjectorTracer : GretelInstrumentable {

    override fun isInstrumentable(classData: ClassContext): Boolean {
        return classData.currentClassData.interfaces.contains(DAGGER_MEMBERS_INJECTOR_INTERFACE)
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
                val traceName = "MembersInjector<$type>::injectMembers"

                return GretelTraceAddingMethodVisitor(traceName, apiVersion, nextMethodVisitor)
            }
        }
    }

    companion object {
        private const val DAGGER_MEMBERS_INJECTOR_INTERFACE = "dagger.MembersInjector"
        private const val METHOD = "injectMembers"
    }
}