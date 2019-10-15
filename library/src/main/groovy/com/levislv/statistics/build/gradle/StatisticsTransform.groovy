package com.levislv.statistics.build.gradle

import com.android.build.api.transform.*
import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.internal.pipeline.TransformManager
import com.android.builder.model.SourceProvider
import com.levislv.statistics.build.gradle.asm.StatisticsClassVisitor
import com.levislv.statistics.build.gradle.util.StatisticsLog
import com.levislv.statistics.build.gradle.util.StatisticsUtils
import groovy.io.FileType
import groovy.util.slurpersupport.GPathResult
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.gradle.api.DomainObjectSet
import org.gradle.api.Project
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter

import java.util.function.Consumer
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry

/**
 * @author LevisLv
 * @email  levislv@levislv.com
 * @blog   https://blog.levislv.com/
 * @book   https://book.levislv.com/
 * @github https://github.com/LevisLv/
 */
class StatisticsTransform extends Transform {

    private Project project
    private String pkgName

    StatisticsTransform(Project project) {
        this.project = project
    }

    @Override
    String getName() {
        return 'statistics'
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    @Override
    Set<QualifiedContent.Scope> getScopes() {
        return TransformManager.PROJECT_ONLY
    }

    @Override
    boolean isIncremental() {
        return false
    }

    private String getPackageName(BaseVariant variant) {
        XmlSlurper slurper = new XmlSlurper(false, false)
        List<File> list = new ArrayList<>(variant.sourceSets.size())
        variant.sourceSets.forEach(new Consumer<SourceProvider>() {
            @Override
            void accept(SourceProvider sourceProvider) {
                list.add(sourceProvider.manifestFile)
            }
        })

        // According to the documentation, the earlier files in the list are meant to be overridden by the later ones.
        // So the first file in the sourceSets list should be main.
        GPathResult result = slurper.parse(list.get(0))
        return result.getProperty("@package").toString()
    }

    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        Context context = transformInvocation.getContext()
        Collection<TransformInput> inputs = transformInvocation.getInputs()
        TransformOutputProvider outputProvider = transformInvocation.getOutputProvider()
        boolean isIncremental = transformInvocation.isIncremental()

        if (!isIncremental) {
            outputProvider.deleteAll()
        }

        DomainObjectSet<BaseVariant> baseVariants
        if (project.plugins.hasPlugin(AppPlugin.class)) {
            baseVariants = project.extensions.getByType(AppExtension).applicationVariants
        } else if (project.plugins.hasPlugin(LibraryPlugin.class)) {
            baseVariants = project.extensions.getByType(LibraryExtension).libraryVariants
        }

        boolean first = true
        baseVariants.all { variant ->
            if (!first) {
                return
            }
            first = false
            pkgName = getPackageName(variant)

            /** 遍历输入文件 */
            inputs.each { TransformInput input ->
                /** 遍历jar */
                input.jarInputs.each { JarInput jarInput ->
                    StatisticsLog.info("||jar绝对路径：${jarInput.file.absolutePath}")

                    String jarInputFileName = jarInput.file.name

                    /** 截取文件路径的md5值重命名输出文件，因为可能同名，会覆盖 */
                    String hexName = DigestUtils.md5Hex(jarInput.file.absolutePath).substring(0, 8)
                    if (jarInputFileName.endsWith('.jar')) {
                        jarInputFileName = jarInputFileName.substring(0, jarInputFileName.length() - '.jar'.length())
                    }

                    /** 获得输出文件 */
                    File destFile = outputProvider.getContentLocation("${jarInputFileName}_${hexName}", jarInput.contentTypes, jarInput.scopes, Format.JAR)
                    StatisticsLog.info("\n||-->开始遍历jar: ${destFile.absolutePath}")
                    /** 修改jar */
                    File resFile = modifyJarFile(jarInput.file, context.getTemporaryDir())
                    StatisticsLog.info("||-->结束遍历jar: ${destFile.absolutePath}")
                    if (resFile == null) {
                        resFile = jarInput.file
                    }
                    FileUtils.copyFile(resFile, destFile)
                }

                /** 遍历dir */
                input.directoryInputs.each { DirectoryInput dirInput ->
                    StatisticsLog.info("||directory绝对路径路径：${dirInput.file.absolutePath}")

                    /** 获得输出文件 */
                    File destDir = outputProvider.getContentLocation(dirInput.name, dirInput.contentTypes, dirInput.scopes, Format.DIRECTORY)
                    StatisticsLog.info("\n||-->开始遍历dir: ${destDir.absolutePath}")
                    File resDir = dirInput.file
                    if (resDir) {
                        FileUtils.copyDirectory(resDir, destDir)

                        Map<String, File> resFileMap = new HashMap<>()
                        [true, false].each { boolean topLevelClass -> // 先遍历顶级类，再遍历内部类
                            resDir.traverse(type: FileType.FILES, nameFilter: ~/.*\.class/) { File classFile ->
                                if (!classFile.name.contains('$') == topLevelClass) {
                                    if (!('BuildConfig.class'.equals(classFile.name)
                                            || 'R.class'.equals(classFile.name)
                                            || 'R2.class'.equals(classFile.name)
                                            || 'StatisticsR.class'.equals(classFile.name)
                                            || classFile.name.startsWith('R$')
                                            || classFile.name.startsWith('R2$')
                                            || classFile.name.startsWith('StatisticsR$'))) {
                                        StatisticsLog.info("\n||-->file: ${classFile.name}")
                                    }
                                    File resFile = modifyClassFile(resDir, classFile, context.getTemporaryDir())
                                    if (resFile != null) {
                                        resFileMap.put(classFile.absolutePath.replace(resDir.absolutePath, ''), resFile)
                                    }
                                }
                            }
                        }

                        resFileMap.entrySet().each { Map.Entry<String, File> resFileEntry ->
                            File destFile = new File(destDir.absolutePath + resFileEntry.getKey())
                            if (destFile.exists()) {
                                destFile.delete()
                            }
                            File resFile = resFileEntry.getValue()
                            FileUtils.copyFile(resFile, destFile)
                            resFile.delete()
                        }
                    }
                    StatisticsLog.info("||-->结束遍历dir: ${destDir.absolutePath}")
                }
            }
        }
    }

    /**
     * 修改jar包各class文件
     *
     * @param jarFile
     * @param tempDir
     * @return
     */
    private File modifyJarFile(File jarFile, File tempDir) {
        String hexName = DigestUtils.md5Hex(jarFile.absolutePath).substring(0, 8)
        File outputJar = new File(tempDir, "${hexName}_${jarFile.name}")
        JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream(outputJar))

        JarFile file = new JarFile(jarFile)
        Enumeration<JarEntry> enumeration = file.entries()
        while (enumeration.hasMoreElements()) {
            JarEntry jarEntry = enumeration.nextElement()
            String entryName = jarEntry.getName()
            ZipEntry zipEntry = new ZipEntry(entryName)
            jarOutputStream.putNextEntry(zipEntry)

            byte[] destClassBytes
            byte[] srcClassBytes = IOUtils.toByteArray(file.getInputStream(jarEntry))
            String className
            if (entryName.endsWith('.class')) {
                className = entryName.replace('/', '.').replace('.class', '')
                if (StatisticsUtils.shouldModifyClass(className)) {
                    destClassBytes = modifyClass(srcClassBytes)
                }
            }
            if (destClassBytes == null) {
                jarOutputStream.write(srcClassBytes)
            } else {
                jarOutputStream.write(destClassBytes)
            }
            jarOutputStream.closeEntry()
        }
        file.close()
        jarOutputStream.close()
        return outputJar
    }

    /**
     * 修改class文件
     *
     * @param dir
     * @param classFile
     * @param tempDir
     * @return
     */
    private File modifyClassFile(File dir, File classFile, File tempDir) {
        String className = classFile.absolutePath
                .replace("${dir.absolutePath}${File.separator}", '')
                .replace(File.separator, '.')
                .replace('.class', '')
        if (!StatisticsUtils.shouldModifyClass(className)) {
            return classFile
        }

        File outputClass = null
        FileOutputStream outputStream = null
        byte[] sourceClassBytes = IOUtils.toByteArray(new FileInputStream(classFile))
        byte[] modifiedClassBytes = modifyClass(sourceClassBytes)
        if (modifiedClassBytes != null) {
            outputClass = new File(tempDir, className.replace('.', '') + '.class')
            if (outputClass.exists()) {
                outputClass.delete()
            }
            outputClass.createNewFile()
            outputStream = new FileOutputStream(outputClass)
            outputStream.write(modifiedClassBytes)
        }
        outputStream.close()
        return outputClass
    }

    /**
     * 修改字节码
     *
     * @param srcClassBytes
     * @return
     * @throws IOException
     */
    private byte[] modifyClass(byte[] srcClassBytes) throws IOException {
        ClassReader classReader = new ClassReader(srcClassBytes)
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS)
        /** 类修改器入口StatisticsClassVisitor */
        ClassVisitor classVisitor = new StatisticsClassVisitor(classWriter, pkgName)
        classReader.accept(classVisitor, ClassReader.EXPAND_FRAMES)
        return classWriter.toByteArray()
    }
}
