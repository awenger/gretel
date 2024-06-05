package de.awenger.gretel.util

import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type

class GretelTraceIncludingCallValuesAddingMethodVisitor(
    private val traceName: String,
    private val arguments: Array<Type>,
    apiVersion: Int,
    nextMethodVisitor: MethodVisitor,
) : MethodVisitor(apiVersion, nextMethodVisitor) {

    override fun visitCode() {
        visitNewStringBuilder()
        visitStringBuilderAppendWithStaticText(traceName)
        visitStringBuilderAppendWithStaticText("(")

        arguments.forEachIndexed { argumentPosition, argumentType ->
            when (argumentType.sort) {
                Type.BOOLEAN, Type.CHAR, Type.DOUBLE, Type.FLOAT, Type.INT, Type.LONG -> {
                    visitLoadArgumentOntoStack(argumentPosition)
                    // StringBuilder::append accepts these directly
                    visitStringBuilderAppendWithTextOnStack(argumentType.descriptor)
                }

                else -> {
                    visitLoadArgumentOntoStack(argumentPosition)
                    // convert to String first
                    visitToStringForObjectOnStack()
                    visitStringBuilderAppendWithTextOnStack("Ljava/lang/String;")
                }
            }
            if (argumentPosition in 0 until arguments.size - 1) {
                visitStringBuilderAppendWithStaticText(", ")
            }
        }
        visitStringBuilderAppendWithStaticText(")")
        visitStringBuilderToString()

        // String::take(127)
        visitIntInsn(Opcodes.BIPUSH, 127) // max length of trace
        visitKotlinStringTakeWithStringAndLengthOnStack()

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