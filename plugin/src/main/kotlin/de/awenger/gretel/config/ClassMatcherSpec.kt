package de.awenger.gretel.config

import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property

interface ClassMatcherSpec {
    val typeSpec: Property<TypeSpec>
    val superTypeSpecs: ListProperty<TypeSpec>
    val annotationTypeSpec: ListProperty<TypeSpec>
}

interface TypeSpec {
    val packageName: Property<String>
    val name: Property<String>
}
