package de.awenger.gretel

import com.android.build.api.instrumentation.AsmClassVisitorFactory
import com.android.build.api.instrumentation.ClassContext
import com.android.build.api.instrumentation.ClassData
import com.android.build.api.instrumentation.InstrumentationParameters
import com.android.build.gradle.internal.instrumentation.ClassContextImpl
import com.android.build.gradle.internal.instrumentation.ClassesDataCache
import com.android.build.gradle.internal.instrumentation.ClassesHierarchyResolver
import de.awenger.gretel.GretelTransformations.Parameters
import de.awenger.gretel.dagger.GretelDaggerAndroidInjectorTracer
import de.awenger.gretel.dagger.GretelDaggerFactoryTracer
import de.awenger.gretel.dagger.GretelDaggerMembersInjectorTracer
import org.gradle.api.tasks.Input
import org.objectweb.asm.ClassVisitor

abstract class GretelTransformations : AsmClassVisitorFactory<Parameters> {
    interface Parameters : InstrumentationParameters {
        @get:Input
        val shouldAnnotate: org.gradle.api.provider.Property<(String, String?) -> String?>
    }


    override fun createClassVisitor(
        classContext: ClassContext,
        nextClassVisitor: ClassVisitor,
    ): ClassVisitor {
        val visitors = instrumentables
            .plus(GretelDynamicTracer(parameters.get().shouldAnnotate.get()))
            .filter { it.isInstrumentable(classContext) }
        if (visitors.isEmpty()) return nextClassVisitor
        return visitors.fold(nextClassVisitor) { nextVisitor, instrumentable ->
            instrumentable.createClassVisitor(
                classContext,
                instrumentationContext.apiVersion.get(),
                nextVisitor,
                parameters.get(),
            )
        }
    }

    override fun isInstrumentable(classData: ClassData): Boolean {
        val classContext = ClassContextImpl(
            classData,
            ClassesHierarchyResolver.Builder(ClassesDataCache()).build(),
        )
        return instrumentables
            .plus(GretelDynamicTracer(parameters.get().shouldAnnotate.get()))
            .any { it.isInstrumentable(classContext) }
    }

    companion object {
        private val instrumentables = listOf(
            GretelApplicationLifecycleTracer(),
            GretelActivityLifecycleTracer(),
            GretelFragmentLifecycleTracer(),
            GretelBroadcastReceiverTracer(),
            GretelDaggerFactoryTracer(),
            GretelDaggerMembersInjectorTracer(),
            GretelDaggerAndroidInjectorTracer(),
            GretelRxJavaTracer()
        )
    }
}