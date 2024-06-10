package de.awenger.gretel.bytecode.util

import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

fun MethodVisitor.visitLoadStaticStringOntoStack(staticString: String) {
    visitLdcInsn(staticString)
}

fun MethodVisitor.visitLoadArgumentOntoStack(argumentPosition: Int) {
    visitIntInsn(Opcodes.ALOAD, argumentPosition)
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