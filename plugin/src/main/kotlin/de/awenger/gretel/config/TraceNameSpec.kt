package de.awenger.gretel.config

import org.gradle.api.provider.Property

interface TraceNameSpec {
    val prefix: Property<String>
    val name: Property<String>
    val suffix: Property<String>
}