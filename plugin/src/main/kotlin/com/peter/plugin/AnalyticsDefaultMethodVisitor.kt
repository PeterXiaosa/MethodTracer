package com.peter.plugin

import org.objectweb.asm.*
import org.objectweb.asm.commons.AdviceAdapter

open class AnalyticsDefaultMethodVisitor(methodVisitor: MethodVisitor?, access: Int, name: String?, desc: String?) : AdviceAdapter(Opcodes.ASM6, methodVisitor, access, name, desc) {
    override fun visitCode() {
        super.visitCode()
    }

    override fun visitInsn(opcode: Int) {
        super.visitInsn(opcode)
    }

    override fun visitIntInsn(opcode: Int, operand: Int) {
        super.visitIntInsn(opcode, operand)
    }

    override fun visitAttribute(attr: Attribute?) {
        super.visitAttribute(attr)
    }

    override fun visitEnd() {
        super.visitEnd()
    }

    override fun visitMaxs(maxStack: Int, maxLocals: Int) {
        super.visitMaxs(maxStack, maxLocals)
    }

    override fun visitJumpInsn(opcode: Int, label: Label?) {
        super.visitJumpInsn(opcode, label)
    }

    override fun visitAnnotation(desc: String?, visible: Boolean): AnnotationVisitor {
        return super.visitAnnotation(desc, visible)
    }

    override fun onMethodEnter() {
        super.onMethodEnter()
    }

    override fun onMethodExit(opcode: Int) {
        super.onMethodExit(opcode)
    }

    override fun visitInvokeDynamicInsn(name: String?, desc: String?, bsm: Handle?, vararg bsmArgs: Any?) {
        super.visitInvokeDynamicInsn(name, desc, bsm, *bsmArgs)
    }
}
