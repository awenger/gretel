package de.awenger.gretel.config

import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property

interface AddTraceSpec {
    val trace: Property<TraceNameSpec>
    val classes: Property<ClassMatcherSpec>
    val methods: ListProperty<MethodMatcherSpec>
}
