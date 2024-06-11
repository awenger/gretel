package de.awenger.gretel.bytecode

import de.awenger.gretel.bytecode.util.*
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type

class GretelTraceAddingMethodVisitor(
    private val className: String,
    private val methodAccess: Int,
    private val methodName: String,
    private val descriptor: String,
    private val traceNameSegments: List<TraceNameSegment>,
    apiVersion: Int,
    nextMethodVisitor: MethodVisitor?,
) : MethodVisitor(apiVersion, nextMethodVisitor) {

    private val isStatic = (methodAccess and Opcodes.ACC_STATIC) != 0

    private val arguments = Type.getArgumentTypes(descriptor)
    private val returnType = Type.getReturnType(descriptor)

    override fun visitCode() {
        visitNewStringBuilder()

        traceNameSegments.forEach {
            when (it) {
                is TraceNameSegment.StaticText -> visitStringBuilderAppendWithStaticText(it.text)
                TraceNameSegment.ClassName -> visitStringBuilderAppendWithStaticText(className.shortClassName())
                TraceNameSegment.MethodName -> visitStringBuilderAppendWithStaticText(methodName)
                is TraceNameSegment.ArgumentType -> visitStringBuilderAppendWithStaticText(arguments[it.position].className.shortClassName())
                is TraceNameSegment.ArgumentValue -> {
                    val type = arguments[it.position]
                    val argumentIndex = it.position + if (isStatic) 0 else 1
                    visitLoadLocalVariableAsStringAppendToStringBuilder(argumentIndex, type)
                }

                TraceNameSegment.ReturnType -> visitStringBuilderAppendWithStaticText(returnType.className.shortClassName())
            }
        }

        visitStringBuilderToString()

        visitCustomStringTakeWithStringOnStack(127)

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

sealed class TraceNameSegment {
    object ClassName : TraceNameSegment()
    object MethodName : TraceNameSegment()
    data class StaticText(val text: String) : TraceNameSegment()
    data class ArgumentType(val position: Int) : TraceNameSegment()
    data class ArgumentValue(val position: Int) : TraceNameSegment()
    object ReturnType : TraceNameSegment()
}

private fun String.shortClassName() = this.substring(this.lastIndexOf('.') + 1)