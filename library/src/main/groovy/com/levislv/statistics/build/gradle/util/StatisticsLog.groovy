package com.levislv.statistics.build.gradle.util

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
