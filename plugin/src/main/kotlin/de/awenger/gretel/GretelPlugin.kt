package de.awenger.gretel

import com.android.build.api.instrumentation.FramesComputationMode.COMPUTE_FRAMES_FOR_INSTRUMENTED_METHODS
import com.android.build.api.instrumentation.InstrumentationScope
import com.android.build.api.variant.AndroidComponentsExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

class GretelPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project
            .extensions
            .getByType(AndroidComponentsExtension::class.java)
            .onVariants { variant ->
                variant.instrumentation.transformClassesWith(
                    GretelTransformations::class.java,
                    InstrumentationScope.ALL,
                ) {

                }
                variant.instrumentation.setAsmFramesComputationMode(COMPUTE_FRAMES_FOR_INSTRUMENTED_METHODS)
            }
    }
}