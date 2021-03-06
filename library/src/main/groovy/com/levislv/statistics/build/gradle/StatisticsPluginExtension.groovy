package com.levislv.statistics.build.gradle

/**
 * @author LevisLv
 * @email  levislv@levislv.com
 * @blog   https://blog.levislv.com/
 * @book   https://book.levislv.com/
 * @github https://github.com/LevisLv/
 */
class StatisticsPluginExtension {
    /**
     * 是否打印编译日志
     */
    boolean enableCompileLog = false
    /**
     * 是否开启热力图
     */
    boolean enableHeatMap = true
    /**
     * 是否允许view的onTouch回调全埋点
     */
    boolean enableViewOnTouch = false
}
