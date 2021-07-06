package com.peter.plugin

import java.io.File
import java.io.FileNotFoundException

/**
 * @author Peter Fu
 * @date 2021/7/1
 */
class Config {
    val UNNEED_TRACE_CLASS = arrayOf("R.class", "R$", "Manifest", "BuildConfig")

    var mTraceConfigFile : String? = null

    private val mNeedTracePackageMap : HashSet<String> by lazy {
        HashSet<String>()
    }

    //在需插桩的包范围内的 无需插桩的白名单
    private val mWhiteClassMap: java.util.HashSet<String> by lazy {
        java.util.HashSet<String>()
    }

    //在需插桩的包范围内的 无需插桩的包名
    private val mWhitePackageMap: java.util.HashSet<String> by lazy {
        java.util.HashSet<String>()
    }

    //插桩代码所在类
    var mBeatClass: String? = null

    //是否需要打印出所有被插桩的类和方法
    var mIsNeedLogTraceInfo = false

    fun isNeedTraceClass(fileName : String) : Boolean {
        var isNeed = true
        if (fileName.endsWith(".class")) {
            for (unTraceCls in UNNEED_TRACE_CLASS) {
                if (fileName.contains(unTraceCls)) {
                    isNeed = false
                    break
                }
            }
        } else {
            isNeed = false
        }
        return isNeed
    }

    fun isConfigTraceClass(className : String) : Boolean {

        fun isInNeedTracePackage() : Boolean {
            var isn = false
            mNeedTracePackageMap.forEach {
                if (className.contains(it)) {
                    isn = true
                    return@forEach
                }
            }
            return isn
        }

        fun isInWhitePackage() : Boolean {
            var isLn = false
            mWhitePackageMap.forEach {
                if (className.contains(it)) {
                    isLn = true
                    return@forEach
                }
            }
            return isLn
        }

        fun isInWhiteClass(): Boolean {
            var isIn = false
            mWhiteClassMap.forEach {
                if (className == it) {
                    isIn = true
                    return@forEach
                }

            }
            return isIn
        }

        return if (mNeedTracePackageMap.isEmpty()) {
            !(isInWhitePackage() || isInWhiteClass())
        } else {
            if (isInNeedTracePackage()) {
                !(isInWhitePackage() || isInWhiteClass())
            } else {
                false
            }
        }
    }

    /**
     * 解析插桩配置文件
     */
    fun parseTraceConfigFile() {

        System.out.println("parseTraceConfigFile start!!!!!!!!!!!!")
        val traceConfigFile = File(mTraceConfigFile)
        if (!traceConfigFile.exists()) {
            throw FileNotFoundException(
                    """
                    Trace config file not exist, Please read quickstart.
                    找不到 $mTraceConfigFile 配置文件, 尝试阅读一下 QuickStart。
                """.trimIndent()
            )
        }

        val configStr = Utils.readFileAsString(traceConfigFile.absolutePath)

        val configArray =
                configStr.split(System.lineSeparator().toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        if (configArray != null) {
            for (i in 0 until configArray.size) {
                var config = configArray[i]
                if (config.isNullOrBlank()) {
                    continue
                }
                if (config.startsWith("#")) {
                    continue
                }
                if (config.startsWith("[")) {
                    continue
                }

                when {
                    config.startsWith("-tracepackage ") -> {
                        config = config.replace("-tracepackage ", "")
                        mNeedTracePackageMap.add(config)
                        System.out.println("tracepackage:$config")
                    }
                    config.startsWith("-keepclass ") -> {
                        config = config.replace("-keepclass ", "")
                        mWhiteClassMap.add(config)
                        System.out.println("keepclass:$config")
                    }
                    config.startsWith("-keeppackage ") -> {
                        config = config.replace("-keeppackage ", "")
                        mWhitePackageMap.add(config)
                        System.out.println("keeppackage:$config")
                    }
                    config.startsWith("-beatclass ") -> {
                        config = config.replace("-beatclass ", "")
                        mBeatClass = config
                        System.out.println("beatclass:$config")
                    }
                    else -> {
                    }
                }
            }

        }


    }
}