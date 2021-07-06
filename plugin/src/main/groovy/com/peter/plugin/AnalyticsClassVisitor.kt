package com.peter.plugin

import com.sun.xml.fastinfoset.util.StringArray
import org.objectweb.asm.*

/**
 * @author Peter Fu
 * @date 2021/6/24
 */
class AnalyticsClassVisitor(classVisitor: ClassVisitor, api: Int = Opcodes.ASM6) : ClassVisitor(api, classVisitor), Opcodes{

    var mInterfaces : Array<out String>? = null

    companion object {
        val SDK_API_CLASS : String = "com/sensorsdata/analytics/android/sdk/SensorsDataAutoTrackHelper"
    }

    override fun visit(
        version: Int,
        access: Int,
        name: String?,
        signature: String?,
        superName: String?,
        interfaces: Array<out String>?
    ) {
        super.visit(version, access, name, signature, superName, interfaces)
        mInterfaces = interfaces
    }

    override fun visitMethod(
        access: Int,
        name: String?,
        desc: String?,
        signature: String?,
        exceptions: Array<out String>?
    ): MethodVisitor {
        var methodVisitor: MethodVisitor = super.visitMethod(access, name, desc, signature, exceptions)

        val nameDesc : String = name + desc

        methodVisitor = object : AnalyticsDefaultMethodVisitor(methodVisitor, access, name, desc) {

            override fun visitEnd() {
                super.visitEnd()
            }

            override fun visitInvokeDynamicInsn(name: String?, desc: String?, bsm: Handle?, vararg bsmArgs: Any?) {
                super.visitInvokeDynamicInsn(name, desc, bsm, *bsmArgs)

                val desc2 : String = bsmArgs[0] as String

            }

            override fun onMethodEnter() {
                super.onMethodEnter()
            }

            override fun onMethodExit(opcode: Int) {
                super.onMethodExit(opcode)
            }

            override fun visitAnnotation(desc: String?, visible: Boolean): AnnotationVisitor {
                return super.visitAnnotation(desc, visible)
            }
        }

        return methodVisitor
    }
}