package com.levislv.statistics.build.gradle

import com.android.build.gradle.*
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.internal.res.GenerateLibraryRFileTask
import com.android.build.gradle.internal.res.LinkApplicationAndroidResourcesTask
import com.android.build.gradle.tasks.ProcessAndroidResources
import com.android.builder.model.SourceProvider
import com.levislv.statistics.build.gradle.r.RGenerator
import com.levislv.statistics.build.gradle.runtime.StatisticsConfig
import com.levislv.statistics.build.gradle.util.StatisticsLog
import groovy.util.slurpersupport.GPathResult
import kotlin.io.FilesKt
import kotlin.text.StringsKt
import org.gradle.api.DomainObjectSet
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection

import java.util.concurrent.atomic.AtomicBoolean
import java.util.function.Consumer

/**
 * @author LevisLv
 * @email  levislv@levislv.com
 * @blog   https://blog.levislv.com/
 * @book   https://book.levislv.com/
 * @github https://github.com/LevisLv/
 */
class StatisticsPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        if (project.plugins.hasPlugin(AppPlugin.class)) {
            AppExtension appExtension = project.extensions.getByType(AppExtension)
            configureStatisticsRGeneration(project, appExtension.applicationVariants)
            configureStatisticsTransform(project, appExtension)
        } else if (project.plugins.hasPlugin(LibraryPlugin.class)) {
            LibraryExtension libraryExtension = project.extensions.getByType(LibraryExtension)
            configureStatisticsRGeneration(project, libraryExtension.libraryVariants)
            configureStatisticsTransform(project, libraryExtension)
        }
    }

    // Parse the variant's main manifest file in order to get the package id which is used to create
    // R.java in the right place.
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

    // 配置StatisticsR
    void configureStatisticsRGeneration(Project project, DomainObjectSet<BaseVariant> variants) {
        variants.all { variant ->
            File outputDir = FilesKt.resolve(project.buildDir, "generated/source/statistics_r/${variant.dirName}")

            String rPackage = getPackageName(variant)
            AtomicBoolean once = new AtomicBoolean()
            variant.outputs.all { output ->
                ProcessAndroidResources processResources = output.processResources

                // Though there might be multiple outputs, their R files are all the same. Thus, we only
                // need to configure the task once with the R.java input and action.
                if (once.compareAndSet(false, true)) {
                    File file
                    if (processResources instanceof GenerateLibraryRFileTask) {
                        file = ((GenerateLibraryRFileTask) processResources).textSymbolOutputFile
                    } else if (processResources instanceof LinkApplicationAndroidResourcesTask) {
                        file = ((LinkApplicationAndroidResourcesTask) processResources).textSymbolOutputFile
                    } else {
                        throw new RuntimeException('Minimum supported Android Gradle Plugin is 3.1.0')
                    }

                    ConfigurableFileCollection rFile = project.files(file).builtBy(processResources)
                    project.tasks.create("generate${StringsKt.capitalize(variant.name)}StatisticsR", RGenerator.class, { generator ->
                        generator.outputDir = outputDir
                        generator.RFile = rFile
                        generator.packageName = rPackage
                        generator.className = 'StatisticsR'
                        variant.registerJavaGeneratingTask(generator, outputDir)
                    })
                }
            }
        }
    }

    // 配置StatisticsTransform
    void configureStatisticsTransform(Project project, BaseExtension extension) {
        project.extensions.create('statistics', StatisticsPluginExtension)

        StatisticsConfig.project = project
        StatisticsConfig.statisticsClassVisitorMap.clear()

        project.afterEvaluate {
            StatisticsLog.enableCompileLog = StatisticsConfig.getPluginExtension().enableCompileLog
        }

        extension.registerTransform(new StatisticsTransform(project))
    }
}
