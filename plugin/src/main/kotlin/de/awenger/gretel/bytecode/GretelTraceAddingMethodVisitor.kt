package de.awenger.gretel.bytecode

import de.awenger.gretel.bytecode.util.visitLoadStaticStringOntoStack
import de.awenger.gretel.bytecode.util.visitTraceCompatBeginSectionWithStringOnStack
import de.awenger.gretel.bytecode.util.visitTraceCompatEndSection
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

class GretelTraceAddingMethodVisitor(
    private val traceName: String,
    apiVersion: Int,
    nextMethodVisitor: MethodVisitor?,
) : MethodVisitor(apiVersion, nextMethodVisitor) {

    override fun visitCode() {
        visitLoadStaticStringOntoStack(traceName)
        visitTraceCompatBeginSectionWithStringOnStack()
        super.visitCode()
    }

    override fun visitInsn(opcode: Int) {
        when (opcode) {
            Opcodes.IRETURN, Opcodes.FRETURN, Opcodes.ARETURN, Opcodes.LRETURN, Opcodes.DRETURN, Opcodes.RETURN -> {
                visitTraceCompatEndSection()
            }

            else -> {}
        }
        super.visitInsn(opcode)
    }
}