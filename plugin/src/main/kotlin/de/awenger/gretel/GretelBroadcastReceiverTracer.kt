package de.awenger.gretel

import com.android.build.api.instrumentation.ClassContext
import de.awenger.gretel.util.GretelTraceAddingMethodVisitor
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor

class GretelBroadcastReceiverTracer : GretelInstrumentable {

    override fun isInstrumentable(classData: ClassContext): Boolean {
        return classData.currentClassData.superClasses.contains(RECEIVER_CLASS)
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
                if (METHODS.contains(name).not()) return super.visitMethod(
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
                val traceName = "$className::$name"

                val mv = super.visitMethod(access, name, descriptor, signature, exceptions)
                return GretelTraceAddingMethodVisitor(traceName, apiVersion, mv)
            }
        }
    }

    companion object {
        private const val RECEIVER_CLASS = "android.content.BroadcastReceiver"
        private val METHODS = listOf("onReceive")
    }
}