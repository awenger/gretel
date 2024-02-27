package de.awenger.gretel

import com.android.build.api.instrumentation.ClassContext
import de.awenger.gretel.util.GretelTraceAddingMethodVisitor
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor

class GretelApplicationLifecycleTracer : GretelInstrumentable {

    override fun isInstrumentable(classData: ClassContext): Boolean {
        return classData.currentClassData.superClasses.contains(APPLICATION_CLASS)
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

                if (LIFECYCLE_CALLBACKS.contains(name).not()) return nextMethodVisitor
                if (access and 0x1000 == 0x1000) return nextMethodVisitor

                val className = classContext.currentClassData.className.split(".").last()
                val traceName = "$className::$name"

                return GretelTraceAddingMethodVisitor(traceName, apiVersion, nextMethodVisitor)
            }
        }
    }

    companion object {
        private const val APPLICATION_CLASS = "android.app.Application"
        private val LIFECYCLE_CALLBACKS = listOf(
            "onCreate",
            "onTerminate",
            "onConfigurationChanged",
            "onLowMemory",
            "onTrimMemory",
            "onDestroy",
        )
    }
}
