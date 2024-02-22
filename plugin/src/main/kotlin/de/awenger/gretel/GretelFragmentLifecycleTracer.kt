package de.awenger.gretel

import com.android.build.api.instrumentation.ClassContext
import de.awenger.gretel.util.GretelTraceAddingMethodVisitor
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor

class GretelFragmentLifecycleTracer : GretelInstrumentable {

    override fun isInstrumentable(classData: ClassContext): Boolean {
        return classData.currentClassData.superClasses.contains(FRAGMENT_CLASS)
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
                if (LIFECYCLE_CALLBACKS.contains(name).not()) return super.visitMethod(
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
        private const val FRAGMENT_CLASS = "androidx.fragment.app.Fragment"
        private val LIFECYCLE_CALLBACKS = listOf(
            "onCreate",
            "onCreateView",
            "onViewCreated",
            "onViewStateRestored",
            "onStart",
            "onResume",
            "onPause",
            "onStop",
            "onSaveInstanceState",
            "onDestroyView",
            "onDestroy",
            "onAttach",
            "onInflate",
            "onActivityCreated",
            "onViewStateRestored",
            "onConfigurationChanged",
            "onDetach"
        )
    }
}