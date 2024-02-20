package de.awenger.gretel.util

import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

class GretelTraceAddingMethodVisitor(
    private val traceName: String,
    apiVersion: Int,
    nextMethodVisitor: MethodVisitor,
) : MethodVisitor(apiVersion, nextMethodVisitor) {

    override fun visitCode() {
        visitLdcInsn(traceName)
        visitMethodInsn(
            Opcodes.INVOKESTATIC,
            "androidx/core/os/TraceCompat",
            "beginSection",
            "(Ljava/lang/String;)V",
            false,
        )
        //println("injected beginSection($traceName)")
        super.visitCode()
    }

    override fun visitInsn(opcode: Int) {
        when (opcode) {
            Opcodes.IRETURN, Opcodes.FRETURN, Opcodes.ARETURN, Opcodes.LRETURN, Opcodes.DRETURN, Opcodes.RETURN -> {
                visitMethodInsn(
                    Opcodes.INVOKESTATIC,
                    "androidx/core/os/TraceCompat",
                    "endSection",
                    "()V",
                    false,
                )
                //println("injected endSection($traceName)")
            }

            else -> {}
        }
        super.visitInsn(opcode)
    }
}