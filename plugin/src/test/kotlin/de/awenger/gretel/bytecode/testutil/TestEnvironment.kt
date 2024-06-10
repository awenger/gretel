package de.awenger.gretel.bytecode.testutil

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import java.net.URLClassLoader

class TestEnvironment private constructor(
    private val api: Int,
    private val sources: List<SourceFile>,
    private val createVisitors: List<(String, Int, ClassVisitor?) -> ClassVisitor?>
) {

    fun build(): URLClassLoader {
        val compilationResult = KotlinCompilation()
            .apply {
                sources = this@TestEnvironment.sources
                inheritClassPath = false
            }
            .compile()

        compilationResult.generatedFiles.forEach { file ->
            if (file.name.endsWith(".class").not()) return@forEach
            val writer = ClassWriter(api)
            val reader = ClassReader(file.inputStream())

            val className = file.relativeTo(compilationResult.outputDirectory).toString().dropLast(6).replace('/', '.')

            val visitor = createVisitors
                .fold(writer as ClassVisitor?) { next, builder -> builder(className, api, next) }

            reader.accept(visitor, 0)

            file.outputStream().write(writer.toByteArray())
        }

        return compilationResult.classLoader
    }

    class Builder private constructor(
        private val sourceFiles: List<SourceFile>,
        private val visitors: List<(String, Int, ClassVisitor?) -> ClassVisitor?>
    ) {
        constructor() : this(emptyList<SourceFile>(), emptyList())

        fun addSource(vararg sourceFile: SourceFile) = Builder(sourceFiles.plus(sourceFile), visitors)
        fun registerMethodVisitor(
            className: String,
            methodName: String,
            methodVisitor: (Int, MethodVisitor) -> MethodVisitor
        ): Builder {
            val builder = fun(cls: String, api: Int, nextVisitor: ClassVisitor?): ClassVisitor? {
                if (cls != className) return nextVisitor
                return MethodClassVisitor(api, nextVisitor) { method, api, nextVisitor ->
                    if (method != methodName) nextVisitor else methodVisitor(api, nextVisitor)
                }
            }

            return Builder(sourceFiles, visitors.plus(builder))
        }

        fun build() = TestEnvironment(Opcodes.ASM9, sourceFiles, visitors).build()
    }
}