package de.awenger.gretel

import de.awenger.gretel.config.*
import org.gradle.api.Action
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Provider
import javax.inject.Inject

open class GretelPluginExtension @Inject constructor(private val objectFactory: ObjectFactory) {
    private val traceSpecs: ListProperty<AddTraceSpec> = objectFactory.listProperty(AddTraceSpec::class.java)

    fun getTraceSpecs(): Provider<List<AddTraceSpec>> = traceSpecs

    fun traceClass(
        classPackageName: String? = null,
        className: String? = null,
        superPackageName: String? = null,
        superName: String? = null,
        annotationPackageName: String? = null,
        annotationName: String? = null,
        action: Action<in AddTraceSpec>? = null
    ) {
        val superTypeSpec = if (superPackageName != null || superName != null) {
            objectFactory.newInstance(TypeSpec::class.java).also {
                it.packageName.set(superPackageName)
                it.name.set(superName)
            }
        } else null

        val annotationTypeSpec = if (annotationPackageName != null || annotationName != null) {
            objectFactory.newInstance(TypeSpec::class.java).also {
                it.packageName.set(annotationPackageName)
                it.name.set(annotationName)
            }
        } else null

        val typeSpec = if (classPackageName != null || className != null) {
            objectFactory.newInstance(TypeSpec::class.java).also {
                it.packageName.set(classPackageName)
                it.name.set(className)
            }
        } else null

        val classSpec = objectFactory.newInstance(ClassMatcherSpec::class.java).also {
            it.typeSpec.set(typeSpec)
            if (superTypeSpec != null) it.superTypeSpecs.add(superTypeSpec)
            if (annotationTypeSpec != null) it.annotationTypeSpec.add(annotationTypeSpec)
        }

        val target = objectFactory.newInstance(AddTraceSpec::class.java)
        target.classSpec.set(classSpec)

        action?.execute(target)
        traceSpecs.add(target)
    }

    private fun AddTraceSpec.traceMethod(method: MethodMatcherSpec, traceName: TraceNameSpec) {
        val methodToTraceSpec = objectFactory.newInstance(MethodToTraceSpec::class.java)
        methodToTraceSpec.methodSpec.set(method)
        methodToTraceSpec.traceSpec.set(traceName)

        methodsToTrace.add(methodToTraceSpec)
    }

    fun AddTraceSpec.traceMethods(
        methodName: String? = null,
        traceName: String = "",
        traceNamePrefix: String = "",
        traceNameSuffix: String = ""
    ) {
        val methodMatcherSpec = objectFactory.newInstance(MethodMatcherSpec::class.java)
        methodMatcherSpec.name.set(methodName)

        val traceNameSpec = objectFactory.newInstance(TraceNameSpec::class.java)
        traceNameSpec.name.set(traceName)
        traceNameSpec.prefix.set(traceNamePrefix)
        traceNameSpec.suffix.set(traceNameSuffix)

        traceMethod(methodMatcherSpec, traceNameSpec)
    }

    fun AddTraceSpec.traceMethod(
        methodName: String? = null,
        traceName: String = "",
        traceNamePrefix: String = "",
        traceNameSuffix: String = ""
    ) = traceMethods(
        methodName,
        traceName,
        traceNamePrefix,
        traceNameSuffix
    )
}