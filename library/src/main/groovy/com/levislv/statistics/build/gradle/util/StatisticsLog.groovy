package com.levislv.statistics.build.gradle.util

/**
 * @author LevisLv
 * @email  levislv@levislv.com
 * @blog   https://blog.levislv.com/
 * @book   https://book.levislv.com/
 * @github https://github.com/LevisLv/
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
