package de.awenger.gretel

import com.android.build.api.instrumentation.FramesComputationMode.COMPUTE_FRAMES_FOR_INSTRUMENTED_METHODS
import com.android.build.api.instrumentation.InstrumentationScope
import com.android.build.api.variant.AndroidComponentsExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

class GretelPlugin : Plugin<Project> {
    override fun apply(project: Project) {

        val extension: GretelPluginExtension = project.extensions
            .create("gretel", GretelPluginExtension::class.java)

        project
            .extensions
            .getByType(AndroidComponentsExtension::class.java)
            .onVariants { variant ->
                if (extension.enabled.getOrElse(true).not()) return@onVariants

                variant.instrumentation.transformClassesWith(
                    GretelTransformations::class.java,
                    InstrumentationScope.ALL,
                ) { params ->
                    params.tracingTargets.set(extension.traces.orElse(emptyList()))
                }
                variant.instrumentation.setAsmFramesComputationMode(COMPUTE_FRAMES_FOR_INSTRUMENTED_METHODS)
            }
    }
}