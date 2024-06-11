package de.awenger.gretel.bytecode.util

import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type

fun MethodVisitor.visitLoadStaticStringOntoStack(staticString: String) {
    visitLdcInsn(staticString)
}

fun MethodVisitor.visitLoadArgumentOntoStack(argumentPosition: Int, type: Type) {
    val opcode = when (type.sort) {
        Type.BOOLEAN, Type.CHAR, Type.BYTE, Type.SHORT, Type.INT -> Opcodes.ILOAD
        Type.FLOAT -> Opcodes.FLOAD
        Type.DOUBLE -> Opcodes.DLOAD
        Type.ARRAY, Type.OBJECT, Type.METHOD -> Opcodes.ALOAD
        else -> throw IllegalArgumentException("Not supported to load argument at $argumentPosition of type $type")
    }
    visitIntInsn(opcode, argumentPosition)
}

fun MethodVisitor.visitNewStringBuilder() {
    visitTypeInsn(Opcodes.NEW, "java/lang/StringBuilder")
    visitInsn(Opcodes.DUP)
    visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false)
}

fun MethodVisitor.visitStringBuilderAppendWithStaticText(text: String) {
    visitLoadStaticStringOntoStack(text)
    visitStringBuilderAppendWithTextOnStack("Ljava/lang/String;")
}

fun MethodVisitor.visitStringBuilderAppendWithTextOnStack(argumentDescriptor: String) {
    visitMethodInsn(
        Opcodes.INVOKEVIRTUAL,
        "java/lang/StringBuilder",
        "append",
        "($argumentDescriptor)Ljava/lang/StringBuilder;",
        false
    )
}

fun MethodVisitor.visitStringBuilderToString() {
    visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false)
}

fun MethodVisitor.visitToStringForObjectOnStack() {
    visitMethodInsn(
        Opcodes.INVOKESTATIC,
        "java/lang/String",
        "valueOf",
        "(Ljava/lang/Object;)Ljava/lang/String;",
        false
    )
}

fun MethodVisitor.visitStringLengthWithStringOnStack() {
    visitMethodInsn(
        Opcodes.INVOKEVIRTUAL,
        "java/lang/String",
        "length",
        "()I",
        false
    )
}

fun MethodVisitor.visitStringSubstringWithStringIntIntOnStack() {
    visitMethodInsn(
        Opcodes.INVOKEVIRTUAL,
        "java/lang/String",
        "substring",
        "(II)Ljava/lang/String;",
        false
    )
}

fun MethodVisitor.visitMathMinWithIntIntOnStack() {
    visitMethodInsn(
        Opcodes.INVOKESTATIC,
        "java/lang/Math",
        "min",
        "(II)I",
        false
    )
}

fun MethodVisitor.visitLoadLocalVariableAsStringAppendToStringBuilder(index: Int, type: Type) {
    visitLoadArgumentOntoStack(index, type)
    when (type.sort) {
        Type.BOOLEAN, Type.CHAR, Type.DOUBLE, Type.FLOAT, Type.INT, Type.LONG -> {
            // StringBuilder::append accepts these directly
            visitStringBuilderAppendWithTextOnStack(type.descriptor)
        }

        Type.OBJECT -> {
            // StringBuilder::append accepts Object directly
            visitStringBuilderAppendWithTextOnStack("Ljava/lang/Object;")
        }

        else -> {
            // convert to String first
            visitToStringForObjectOnStack()
            visitStringBuilderAppendWithTextOnStack("Ljava/lang/String;")
        }
    }
}


fun MethodVisitor.visitCustomStringTakeWithStringOnStack(maxLength: Int) {
    // duplicate the string - will be consumed by String::substring further down
    visitInsn(Opcodes.DUP)

    visitStringLengthWithStringOnStack()

    visitIntInsn(Opcodes.BIPUSH, maxLength)
    visitMathMinWithIntIntOnStack()

    visitInsn(Opcodes.ICONST_0)
    visitInsn(Opcodes.SWAP)
    visitStringSubstringWithStringIntIntOnStack()
}

fun MethodVisitor.visitTraceCompatBeginSectionWithStringOnStack() {
    visitMethodInsn(
        Opcodes.INVOKESTATIC,
        "androidx/core/os/TraceCompat",
        "beginSection",
        "(Ljava/lang/String;)V",
        false,
    )
}

fun MethodVisitor.visitTraceCompatEndSection() {
    visitMethodInsn(
        Opcodes.INVOKESTATIC,
        "androidx/core/os/TraceCompat",
        "endSection",
        "()V",
        false,
    )
}