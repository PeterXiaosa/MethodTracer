package com.peter.plugin

import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.IOUtils
import org.objectweb.asm.ClassWriter
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.*
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.regex.Matcher
import kotlin.collections.HashSet

/**
 * @author Peter Fu
 * @date 2021/6/23
 */
class AnalyticsClassModifier {

    companion object {
        val exclude = HashSet<String>()

        fun modifyJar(jarFile : File, tempDir : File, nameHex : Boolean) : File{
            val file = JarFile(jarFile, false)

            var hexName = ""
            if (nameHex) {
                hexName = DigestUtils.md5Hex(jarFile.absolutePath).substring(0, 8)
            }

            val outputJar = File(tempDir, hexName + jarFile.name)
            val jarOutputStream = JarOutputStream(FileOutputStream(outputJar))
            val enumeration = file.entries()
            while (enumeration.hasMoreElements()) {
                val jarEntry = enumeration.nextElement()
                var inputStream: InputStream

                inputStream = file.getInputStream(jarEntry)
                val entryName = jarEntry.getName()
                if (entryName.endsWith(".DSA") || entryName.endsWith(".SF")) {

                } else {
                    val jarEntry2 = JarEntry(entryName)
                    jarOutputStream.putNextEntry(jarEntry2)

                    var modifiedClassBytes : ByteArray? = null
                    val sourceClassBytes = IOUtils.toByteArray(inputStream)
                    if (entryName.endsWith(".class")) {
                        val className = entryName.replace(Matcher.quoteReplacement(File.separator), ".").replace(".class", "")
                        if (isShouldModify(className)) {
                            modifiedClassBytes = modifyClass(sourceClassBytes)
                        }
                    }
                    if (modifiedClassBytes == null) {
                        modifiedClassBytes = sourceClassBytes
                    }
                    jarOutputStream.write(modifiedClassBytes)
                    jarOutputStream.closeEntry()
                }
            }
            jarOutputStream.close()
            file.close()
            return outputJar
        }

        private fun modifyClass(srcClass: ByteArray?): ByteArray? {
            val classWriter = ClassWriter(ClassWriter.COMPUTE_MAXS)
            val classVisitor = AnalyticsClassVisitor(classWriter)

            // TODO change
            return ByteArray(1)
        }

        private fun isShouldModify(className: String): Boolean {
            val iterator = exclude.iterator()
            while (iterator.hasNext()) {
                val packageName = iterator.next()
                if (className.startsWith(packageName)) {
                    return false
                }
            }

            if (className.contains("R$") ||
                    className.contains("R2$") ||
                    className.contains("R.class") ||
                    className.contains("R2.class") ||
                    className.contains("BuildConfig.class")) {
                return false
            }
            return true
        }

        fun test() {

        }
    }

}