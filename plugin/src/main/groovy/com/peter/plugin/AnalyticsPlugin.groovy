package com.peter.plugin

import com.android.build.gradle.AppExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

class AnalyticsPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        println '*******************开始编译Peter自己的插件**********************************'

        // 通过在 build.gradle中配置
        project.extensions.create("MethodTracer", MethodTracerConfig)
        def extension = project.extensions.getByType(AppExtension)
        extension.registerTransform(new AnalyticsTransform(project))
    }
}