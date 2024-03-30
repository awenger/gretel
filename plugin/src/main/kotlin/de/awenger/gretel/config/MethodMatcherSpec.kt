package de.awenger.gretel.config

import org.gradle.api.provider.Property

interface MethodMatcherSpec {
    val name: Property<String>
}