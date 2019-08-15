package com.levislv.statistics.build.gradle.util

/**
 * @author levislv
 * @email  levislv@levislv.com
 * @blog   https://blog.levislv.com/
 * @github https://github.com/levislv/
 */
class StatisticsLog {
    static boolean enableCompileLog = false

    static info(Object msg) {
        if (!enableCompileLog) {
            return
        }
        try {
            println "${msg}"
        } catch (Throwable throwable) {
            throwable.printStackTrace()
        }
    }
}
