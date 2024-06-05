package de.awenger.gretel.config

import org.gradle.api.provider.Property

interface TraceNameSpec {
    val name: Property<String>
    val prefix: Property<String>
    val suffix: Property<String>
    val includeArgumentValues : Property<Boolean>
}