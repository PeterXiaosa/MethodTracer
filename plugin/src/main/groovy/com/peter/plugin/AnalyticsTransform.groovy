package com.peter.plugin

import com.android.build.api.transform.Context
import com.android.build.api.transform.DirectoryInput
import com.android.build.api.transform.Format
import com.android.build.api.transform.JarInput
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformException
import com.android.build.api.transform.TransformInput
import com.android.build.api.transform.TransformInvocation
import com.android.build.api.transform.TransformOutputProvider
import com.android.build.gradle.internal.pipeline.TransformManager
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.gradle.api.Project
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes

import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry
import static org.objectweb.asm.ClassReader.EXPAND_FRAMES

//import com.peter.plugin.Config

/**
 * @author Peter Fu
 * @date 2021/6/23
 */
class AnalyticsTransform extends Transform{
    private Project project

    AnalyticsTransform(Project project) {
        this.project = project
    }

    @Override
    String getName() {
        return "BlockMonitor0720"
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    @Override
    Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    @Override
    boolean isIncremental() {
        return false
    }

    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        _transform(transformInvocation.context, transformInvocation.inputs, transformInvocation.outputProvider, transformInvocation.incremental)
    }

    void _transform(Context context, Collection<TransformInput> inputs, TransformOutputProvider outputProvider, boolean  isIncremental) {
        if (!incremental) {
            outputProvider.deleteAll()
        }

        println '******************* [MethodTracer] : transform() **********************************'

        MethodTracerConfig methodTracerConfig = project.MethodTracer
        String output = methodTracerConfig.output
        if (output == null || output.isEmpty()) {
            methodTracerConfig.output = project.getBuildDir().getAbsolutePath() + File.separator + "methodtracer_output"
        }

        if (methodTracerConfig.open) {
            Config traceConfig = initConfig()
            traceConfig.parseTraceConfigFile()

            /**Transform ??? inputs ????????????????????????????????????????????? jar ????????????????????? */
            inputs.each { TransformInput input ->
                /**????????????*/
                input.directoryInputs.each { DirectoryInput directoryInput ->
                    traceSrcFiles(directoryInput, outputProvider, traceConfig)
                }

                /**?????? jar*/
                input.jarInputs.each { JarInput jarInput ->
                    traceJarFiles(jarInput, outputProvider, traceConfig)
                }
            }
        }
    }

    static void traceSrcFiles(DirectoryInput directoryInput, TransformOutputProvider outputProvider, Config traceConfig) {
        if (directoryInput.file.isDirectory()) {
            directoryInput.file.eachFileRecurse { File file ->
                def name = file.name
                if (traceConfig.isNeedTraceClass(name)) {
                    ClassReader classReader = new ClassReader(file.bytes)
                    ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_MAXS)
//                    ClassVisitor cv = new TraceClassVisitor(Opcodes.ASM5, classWriter, traceConfig)
                    ClassVisitor cv = new TraceClassVisitor(Opcodes.ASM6, classWriter, traceConfig)
                    classReader.accept(cv, EXPAND_FRAMES)
                    byte[] code = classWriter.toByteArray()
                    FileOutputStream fos = new FileOutputStream(
                            file.parentFile.absolutePath + File.separator + name)
                    fos.write(code)
                    fos.close()
                }
            }
        }

        //??????????????????????????????????????????
        def dest = outputProvider.getContentLocation(directoryInput.name,
                directoryInput.contentTypes, directoryInput.scopes,
                Format.DIRECTORY)
        FileUtils.copyDirectory(directoryInput.file, dest)
    }

    static void traceJarFiles(JarInput jarInput, TransformOutputProvider outputProvider, Config traceConfig) {
        if (jarInput.file.getAbsolutePath().endsWith(".jar")) {
            //?????????????????????,??????????????????,?????????
            def jarName = jarInput.name
            def md5Name = DigestUtils.md5Hex(jarInput.file.getAbsolutePath())
            if (jarName.endsWith(".jar")) {
                jarName = jarName.substring(0, jarName.length() - 4)
            }
            JarFile jarFile = new JarFile(jarInput.file)
            Enumeration enumeration = jarFile.entries()

            File tmpFile = new File(jarInput.file.getParent() + File.separator + "classes_temp.jar")
            if (tmpFile.exists()) {
                tmpFile.delete()
            }

            JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream(tmpFile))

            //??????jar???????????????
            while (enumeration.hasMoreElements()) {
                JarEntry jarEntry = (JarEntry) enumeration.nextElement()
                String entryName = jarEntry.getName()
                ZipEntry zipEntry = new ZipEntry(entryName)
                InputStream inputStream = jarFile.getInputStream(jarEntry)
                if (traceConfig.isNeedTraceClass(entryName)) {
                    jarOutputStream.putNextEntry(zipEntry)
                    ClassReader classReader = new ClassReader(IOUtils.toByteArray(inputStream))
                    ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_MAXS)
//                    ClassVisitor cv = new TraceClassVisitor(Opcodes.ASM5, classWriter, traceConfig)
                    ClassVisitor cv = new TraceClassVisitor(Opcodes.ASM6, classWriter, traceConfig)
                    classReader.accept(cv, EXPAND_FRAMES)
                    byte[] code = classWriter.toByteArray()
                    jarOutputStream.write(code)
                } else {
                    jarOutputStream.putNextEntry(zipEntry)
                    jarOutputStream.write(IOUtils.toByteArray(inputStream))
                }
                jarOutputStream.closeEntry()
            }

            jarOutputStream.close()
            jarFile.close()

            //??????????????????????????????????????????
            def dest = outputProvider.getContentLocation(jarName + md5Name,
                    jarInput.contentTypes, jarInput.scopes, Format.JAR)
            FileUtils.copyFile(tmpFile, dest)

            tmpFile.delete()
        }
    }

    Config initConfig() {
        def configuration = project.MethodTracer
        Config config = new Config()
        config.MTraceConfigFile = configuration.traceConfigFile
        config.MIsNeedLogTraceInfo = configuration.logTraceInfo
        return config
    }
}
