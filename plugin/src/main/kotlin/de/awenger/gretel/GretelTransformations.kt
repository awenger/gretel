package de.awenger.gretel

import com.android.build.api.instrumentation.AsmClassVisitorFactory
import com.android.build.api.instrumentation.ClassContext
import com.android.build.api.instrumentation.ClassData
import com.android.build.api.instrumentation.InstrumentationParameters
import com.android.build.gradle.internal.instrumentation.ClassContextImpl
import com.android.build.gradle.internal.instrumentation.ClassesDataCache
import com.android.build.gradle.internal.instrumentation.ClassesHierarchyResolver
import de.awenger.gretel.GretelTransformations.Parameters
import de.awenger.gretel.config.AddTraceSpec
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Input
import org.objectweb.asm.ClassVisitor

abstract class GretelTransformations : AsmClassVisitorFactory<Parameters> {
    interface Parameters : InstrumentationParameters {
        @get:Input
        val tracingTargets: ListProperty<AddTraceSpec>
    }

    private fun getInstrumentables(): List<GretelInstrumentable> {
        return parameters.get()
            .tracingTargets.get()
            .map { GretelConfigurableTracer(it) }
    }

    override fun isInstrumentable(classData: ClassData): Boolean {
        val classContext = ClassContextImpl(classData, ClassesHierarchyResolver.Builder(ClassesDataCache()).build())
        return getInstrumentables().any { it.isInstrumentable(classContext) }
    }

    override fun createClassVisitor(
        classContext: ClassContext,
        nextClassVisitor: ClassVisitor,
    ): ClassVisitor {
        val visitors = getInstrumentables().filter { it.isInstrumentable(classContext) }

        if (visitors.isEmpty()) return nextClassVisitor
        return visitors
            .fold(nextClassVisitor) { nextVisitor, instrumentable ->
                instrumentable.createClassVisitor(
                    classContext,
                    instrumentationContext.apiVersion.get(),
                    nextVisitor,
                    parameters.get(),
                )
            }
    }
}
