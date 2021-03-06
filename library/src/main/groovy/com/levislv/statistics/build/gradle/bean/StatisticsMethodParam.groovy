package com.levislv.statistics.build.gradle.bean

/**
 * @author LevisLv
 * @email  levislv@levislv.com
 * @blog   https://blog.levislv.com/
 * @book   https://book.levislv.com/
 * @github https://github.com/LevisLv/
 */
class StatisticsMethodParam {
    int opcode
    int index
    int tagKey
    String tagValueDescriptor // 基本类型的descriptor 例如：Z
    String tagValueConvertedOwner // 基本类型包装类的owner 例如：java/lang/Boolean

    StatisticsMethodParam(int opcode, int index, int tagKey, String tagValueDescriptor, String tagValueConvertedOwner) {
        this.opcode = opcode
        this.index = index
        this.tagKey = tagKey
        this.tagValueDescriptor = tagValueDescriptor
        this.tagValueConvertedOwner = tagValueConvertedOwner
    }
}
