package com.levislv.statistics.build.gradle.asm

import com.levislv.statistics.build.gradle.constant.StatisticsConsts
import com.levislv.statistics.build.gradle.runtime.StatisticsConfig
import com.levislv.statistics.build.gradle.util.StatisticsLog
import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.Opcodes

/**
 * @author levislv
 * @email  levislv@levislv.com
 * @blog   https://blog.levislv.com/
 * @github https://github.com/levislv/
 */
class StatisticsClassAnnotationVisitor extends AnnotationVisitor {

    private StatisticsClassVisitor cv
    private String descriptor
    private boolean visible

    StatisticsClassAnnotationVisitor(StatisticsClassVisitor cv, AnnotationVisitor annotationVisitor, String descriptor, boolean visible) {
        super(Opcodes.ASM7, annotationVisitor)

        StatisticsLog.info("||---visitAnnotation: descriptor=${descriptor}, visible=${visible}")

        this.cv = cv
        this.descriptor = descriptor
        this.visible = visible
    }

    @Override
    void visit(String name, Object value) {
        StatisticsLog.info("||---visit: name=${name}, value=${value}")

        super.visit(name, value)

        if (StatisticsConsts.ANNOTATION_VISITOR_DESCRIPTOR_STATISTICS_PAGE.equals(this.descriptor)) {
            this.cv.page.pkgName = this.cv.pkgName
            if ('id'.equals(name)) {
                this.cv.page.id = (int) value
            }
            if ('name'.equals(name)) {
                this.cv.page.name = (String) value
            }
            if ('data'.equals(name)) {
                this.cv.page.data = (String) value
            }
            if (!StatisticsConfig.statisticsClassVisitorMap.containsKey(this.cv.name)) {
                StatisticsConfig.statisticsClassVisitorMap.put(this.cv.name, this.cv)
            }
        }
    }

    @Override
    void visitEnum(String name, String descriptor, String value) {
        StatisticsLog.info("||---visitEnum: name=${name}, descriptor=${descriptor}, value=${value}")

        super.visitEnum(name, descriptor, value)

        if (StatisticsConsts.ANNOTATION_VISITOR_DESCRIPTOR_STATISTICS_PAGE.equals(this.descriptor)
                && StatisticsConsts.ANNOTATION_VISITOR_DESCRIPTOR_STATISTICS_PAGE_TYPE.equals(descriptor)) {
            if ('type'.equals(name)) {
                if ('ACTIVITY'.equals(value)) {
                    this.cv.page.isActivity = true
                } else if ('FRAGMENT'.equals(value)) {
                    this.cv.page.isFragment = true
                }
            }
            if (!StatisticsConfig.statisticsClassVisitorMap.containsKey(this.cv.name)) {
                StatisticsConfig.statisticsClassVisitorMap.put(this.cv.name, this.cv)
            }
        }
    }

    @Override
    AnnotationVisitor visitAnnotation(String name, String descriptor) {
        StatisticsLog.info("||---visitAnnotation: name=${name}, descriptor=${descriptor}")

        return super.visitAnnotation(name, desc)
    }

    @Override
    AnnotationVisitor visitArray(String name) {
        StatisticsLog.info("||---visitArray: name=${name}")

        return super.visitArray(name)
    }

    @Override
    void visitEnd() {
        StatisticsLog.info("||---visitEnd")

        super.visitEnd()
    }
}
