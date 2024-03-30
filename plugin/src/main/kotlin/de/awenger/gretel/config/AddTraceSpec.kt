package de.awenger.gretel.config

import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property

interface AddTraceSpec {
    val classSpec: Property<ClassMatcherSpec>
    val methodsToTrace: ListProperty<MethodToTraceSpec>
}

interface MethodToTraceSpec {
    val methodSpec: Property<MethodMatcherSpec>
    val traceSpec: Property<TraceNameSpec>
}