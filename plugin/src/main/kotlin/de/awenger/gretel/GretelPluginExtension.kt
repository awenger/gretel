package de.awenger.gretel

import de.awenger.gretel.config.*
import org.gradle.api.Action
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import javax.inject.Inject

open class GretelPluginExtension @Inject constructor(private val objectFactory: ObjectFactory) {

    val traces: ListProperty<AddTraceSpec> = objectFactory.listProperty(AddTraceSpec::class.java)

    fun defineTrace(action: Action<in AddTraceSpec>): AddTraceSpec {
        val addTraceSpec = traceSpec()
        action.execute(addTraceSpec)
        return addTraceSpec
    }

    fun traceSpec(
        trace: TraceNameSpec? = null,
        classes: ClassMatcherSpec? = null,
        methods: List<MethodMatcherSpec>? = null
    ): AddTraceSpec {
        return objectFactory
            .newInstance(AddTraceSpec::class.java)
            .also {
                it.trace.set(trace)
                it.classes.set(classes)
                if (methods != null) it.methods.addAll(methods)
            }
    }

    fun trace(
        name: String? = null,
        prefix: String? = null,
        suffix: String? = null
    ): TraceNameSpec {
        return objectFactory
            .newInstance(TraceNameSpec::class.java)
            .also {
                it.name.set(name)
                it.prefix.set(prefix)
                it.suffix.set(suffix)
            }
    }

    fun classes(
        type: TypeSpec? = null,
        superType: TypeSpec? = null,
        annotationType: TypeSpec? = null
    ): ClassMatcherSpec {
        return objectFactory
            .newInstance(ClassMatcherSpec::class.java)
            .also {
                it.type.set(type)
                if (superType != null) it.superTypes.add(superType)
                if (annotationType != null) it.annotationTypes.add(annotationType)
            }

    }

    fun type(
        packageName: String? = null,
        name: String? = null
    ): TypeSpec {
        return objectFactory
            .newInstance(TypeSpec::class.java)
            .also {
                it.packageName.set(packageName)
                it.name.set(name)
            }
    }

    fun method(
        name: String? = null
    ): MethodMatcherSpec {
        return objectFactory
            .newInstance(MethodMatcherSpec::class.java)
            .also {
                it.name.set(name)
            }
    }
}