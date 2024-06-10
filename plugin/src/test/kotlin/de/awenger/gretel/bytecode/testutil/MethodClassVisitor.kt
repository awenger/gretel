package de.awenger.gretel.bytecode.testutil

import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor

class MethodClassVisitor(
    api: Int,
    nextVisitor: ClassVisitor?,
    private val createMethodVisitor: (String?, Int, MethodVisitor) -> MethodVisitor
) : ClassVisitor(api, nextVisitor) {

    override fun visitMethod(
        access: Int,
        name: String?,
        descriptor: String?,
        signature: String?,
        exceptions: Array<out String>?
    ): MethodVisitor {
        val nextVisitor = super.visitMethod(access, name, descriptor, signature, exceptions)
        val visitor = createMethodVisitor(name, api, nextVisitor)
        return visitor
    }
}