package de.awenger.gretel

import com.android.build.api.instrumentation.ClassContext
import org.objectweb.asm.ClassVisitor

interface GretelInstrumentable {

    fun isInstrumentable(classData: ClassContext): Boolean

    fun createClassVisitor(
        classContext: ClassContext,
        apiVersion: Int,
        nextClassVisitor: ClassVisitor,
        parameters: GretelTransformations.Parameters,
    ): ClassVisitor

}