package de.awenger.gretel.config

import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property

interface ClassMatcherSpec {
    val type: Property<TypeSpec>
    val superTypes: ListProperty<TypeSpec>
    val annotationTypes: ListProperty<TypeSpec>
}

interface TypeSpec {
    val packageName: Property<String>
    val name: Property<String>
}
