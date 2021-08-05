package com.peter.plugin

import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.commons.AdviceAdapter

class TraceMethodVisitor(api: Int, mv: MethodVisitor?, access: Int, name: String?, desc: String?, className: String?, var traceConfig: Config)
    : AdviceAdapter(api, mv, access, name, desc) {
    private var methodName: String? = null
    private var name: String? = null
    private var className: String? = null
    private val maxSectionNameLength = 127


    init {
        val traceMethod = TraceMethod.create(0, access, className, name, desc)
        this.methodName = traceMethod.getMethodNameText()
        this.className = className
        this.name = name
    }

    override fun onMethodEnter() {
        super.onMethodEnter()
        val methodName = generatorMethodName()
        mv.visitLdcInsn(methodName)
        mv.visitMethodInsn(
            INVOKESTATIC,
            traceConfig.mBeatClass,
            "onMethodStart",
            "(Ljava/lang/String;)V",
            false
        )

        if (traceConfig.mIsNeedLogTraceInfo) {
            println("MethodTraceMan-trace-method: ${methodName ?: "未知"}")
        }
    }

    override fun onMethodExit(opcode: Int) {
        mv.visitLdcInsn(generatorMethodName())
        mv.visitLdcInsn(traceConfig.costTime)
        mv.visitMethodInsn(
                INVOKESTATIC,
                "java/lang/Long",
                "valueOf",
                "(J)Ljava/lang/Long;",
                false
        )

        mv.visitMethodInsn(
            INVOKESTATIC,
            traceConfig.mBeatClass,
            "onMethodEnd",
            "(Ljava/lang/String;Ljava/lang/Long;)V",
            false
        )
    }

    private fun generatorMethodName(): String? {
        var sectionName = methodName
        var length = sectionName?.length ?: 0
        if (length > maxSectionNameLength && !sectionName.isNullOrBlank()) {
            // 先去掉参数
            val parmIndex = sectionName.indexOf('(')
            sectionName = sectionName.substring(0, parmIndex)
            // 如果依然更大，直接裁剪
            length = sectionName.length
            if (length > 127) {
                sectionName = sectionName.substring(length - maxSectionNameLength)
            }
        }
        return sectionName
    }
}
