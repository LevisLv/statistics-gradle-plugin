package com.levislv.statistics.build.gradle.runtime

import com.levislv.statistics.build.gradle.StatisticsPluginExtension
import com.levislv.statistics.build.gradle.asm.StatisticsClassVisitor
import org.gradle.api.Project

/**
 * @author levislv
 * @email  levislv@levislv.com
 * @blog   https://blog.levislv.com/
 * @github https://github.com/levislv/
 */
class StatisticsConfig {
    private static Project project

    static Map<String, StatisticsClassVisitor> statisticsClassVisitorMap = new HashMap<>()

    static setProject(Project project) {
        this.project = project
    }

    static StatisticsPluginExtension getPluginExtension() {
        return project.statistics
    }

    static boolean enableHeatMap() {
        return getPluginExtension().enableHeatMap
    }

    static boolean enableViewOnTouch() {
        return getPluginExtension().enableViewOnTouch
    }
}
