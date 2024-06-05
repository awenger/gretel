package de.awenger.gretel.util

import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

fun MethodVisitor.visitLoadStaticStringOntoStack(staticString: String) {
    visitLdcInsn(staticString)
}

fun MethodVisitor.visitLoadArgumentOntoStack(argumentPosition: Int) {
    visitIntInsn(Opcodes.ALOAD, argumentPosition + 1)
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

fun MethodVisitor.visitStringBuilderToString(){
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

fun MethodVisitor.visitKotlinStringTakeWithStringAndLengthOnStack() {
    visitMethodInsn(
        Opcodes.INVOKESTATIC,
        "kotlin/text/StringsKt",
        "take",
        "(Ljava/lang/String;I)Ljava/lang/String;",
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