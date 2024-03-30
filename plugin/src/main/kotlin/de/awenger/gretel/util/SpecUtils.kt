package de.awenger.gretel.util

import com.android.build.api.instrumentation.ClassData
import de.awenger.gretel.config.ClassMatcherSpec
import de.awenger.gretel.config.MethodMatcherSpec
import de.awenger.gretel.config.TypeSpec


fun ClassMatcherSpec.matches(classData: ClassData): Boolean {
    val targetType = this.typeSpec.orNull
    val targetSuper = this.superTypeSpecs.orNull?.takeIf { it.isNotEmpty() }
    val targetAnnotation = this.annotationTypeSpec.orNull?.takeIf { it.isNotEmpty() }

    val actualType = classData.className
    val actualSuperTypes = classData.interfaces.plus(classData.superClasses)
    val actualAnnotations = classData.classAnnotations

    val typeMatches = targetType == null || targetType.matches(actualType)
    val superMatches = targetSuper == null || targetSuper.all { it.matchesAny(actualSuperTypes) }
    val annotationMatches = targetAnnotation == null || targetAnnotation.all { it.matchesAny(actualAnnotations) }

    return typeMatches && superMatches && annotationMatches
}

fun TypeSpec.matchesAny(fullTypeNames: List<String>): Boolean = fullTypeNames.any { this.matches(it) }
fun TypeSpec.matches(fullTypeName: String): Boolean {
    val classNameSegments = fullTypeName.split('.')
    val actualClassName = classNameSegments.last()
    val actualPackageName = classNameSegments.take(classNameSegments.size - 1).joinToString(".")

    val targetClassName = this.name.orNull
    val targetPackageName = this.packageName.orNull

    val classNameMatch = targetClassName == null || targetClassName == actualClassName
    val packageNameMatch = targetPackageName == null || targetPackageName == actualPackageName

    return classNameMatch && packageNameMatch
}

fun MethodMatcherSpec.matches(methodName: String?): Boolean {
    val targetMethodName = this.name.orNull
    val matchesMethodName = targetMethodName == null || targetMethodName == methodName
    return matchesMethodName
}

fun ClassMatcherSpec.debugString(): String {
    val superDbgString = superTypeSpecs
        .orNull
        ?.takeIf { it.isNotEmpty() }
        ?.joinToString { superType -> superType.debugString() }
        ?: "*"
    return "${typeSpec.orNull.debugString()} : $superDbgString"
}

private fun TypeSpec?.debugString(): String {
    if (this == null) return "*.*"
    return "${packageName.orElse("*")}.${name.orElse("*")}"
}