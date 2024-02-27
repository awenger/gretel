package de.awenger.gretel

import com.android.build.api.instrumentation.ClassContext
import de.awenger.gretel.util.GretelTraceAddingMethodVisitor
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor

class GretelActivityLifecycleTracer : GretelInstrumentable {

    override fun isInstrumentable(classData: ClassContext): Boolean {
        return classData.currentClassData.superClasses.contains(ACTIVITY_CLASS)
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
        private const val ACTIVITY_CLASS = "android.app.Activity"
        private val LIFECYCLE_CALLBACKS = listOf(
            "onCreate",
            "onStart",
            "onRestart",
            "onResume",
            "onPause",
            "onStop",
            "onDestroy",
            "onRestoreInstanceState",
            "onSaveInstanceState"
        )
    }
}
