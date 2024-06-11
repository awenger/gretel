package de.awenger.gretel.bytecode.testutil

import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor

class MethodClassVisitor(
    api: Int,
    nextVisitor: ClassVisitor?,
    private val methodVisitorBuilder: BuildMethodVisitor
) : ClassVisitor(api, nextVisitor) {

    override fun visitMethod(
        access: Int,
        name: String,
        descriptor: String,
        signature: String?,
        exceptions: Array<out String>?
    ): MethodVisitor? {
        val nextVisitor = super.visitMethod(access, name, descriptor, signature, exceptions)
        return methodVisitorBuilder.build(access, descriptor, name, api, nextVisitor)
    }

    fun interface BuildMethodVisitor {
        fun build(
            methodAccess: Int,
            methodDescriptor : String,
            methodName: String,
            api: Int,
            nextMethodVisitor: MethodVisitor?
        ): MethodVisitor?
    }
}