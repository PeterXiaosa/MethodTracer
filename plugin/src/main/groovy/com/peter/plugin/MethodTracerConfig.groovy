package com.peter.plugin

class MethodTracerConfig {
    String output
    boolean open
    String traceConfigFile
    boolean logTraceInfo

    MethodTracerConfig() {
        open = true
        output = ""
        logTraceInfo = false
    }
}