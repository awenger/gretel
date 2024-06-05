package de.awenger.gretel.util

import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type

class GretelTraceIncludingCallValuesAddingMethodVisitor(
    private val traceName: String,
    private val arguments: Array<Type>,
    private val isStatic: Boolean,
    apiVersion: Int,
    nextMethodVisitor: MethodVisitor,
) : MethodVisitor(apiVersion, nextMethodVisitor) {

    override fun visitCode() {
        visitNewStringBuilder()
        visitStringBuilderAppendWithStaticText(traceName)
        visitStringBuilderAppendWithStaticText("(")

        arguments.forEachIndexed { pos, argumentType ->
            // args are starting from position 0 for static functions, position 1 for member functions
            val argumentPosition = pos + if (isStatic) 0 else 1
            when (argumentType.sort) {
                Type.BOOLEAN, Type.CHAR, Type.DOUBLE, Type.FLOAT, Type.INT, Type.LONG -> {
                    visitLoadArgumentOntoStack(argumentPosition)
                    // StringBuilder::append accepts these directly
                    visitStringBuilderAppendWithTextOnStack(argumentType.descriptor)
                }

                Type.OBJECT -> {
                    visitLoadArgumentOntoStack(argumentPosition)
                    // StringBuilder::append accepts Object directly
                    visitStringBuilderAppendWithTextOnStack("Ljava/lang/Object;")
                }

                else -> {
                    visitLoadArgumentOntoStack(argumentPosition)
                    // convert to String first
                    visitToStringForObjectOnStack()
                    visitStringBuilderAppendWithTextOnStack("Ljava/lang/String;")
                }
            }
            if (pos in 0 until arguments.size - 1) {
                visitStringBuilderAppendWithStaticText(", ")
            }
        }
        visitStringBuilderAppendWithStaticText(")")
        visitStringBuilderToString()

        // duplicate the string - will be consumed by String::substring further down
        visitInsn(Opcodes.DUP)

        visitStringLengthWithStringOnStack()

        visitIntInsn(Opcodes.BIPUSH, 127)
        visitMathMinWithIntIntOnStack()

        visitInsn(Opcodes.ICONST_0)
        visitInsn(Opcodes.SWAP)
        visitStringSubstringWithStringIntIntOnStack()

        // -----
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