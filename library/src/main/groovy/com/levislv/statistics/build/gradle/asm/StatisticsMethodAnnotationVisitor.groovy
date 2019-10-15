package com.levislv.statistics.build.gradle.asm

import com.levislv.statistics.build.gradle.constant.StatisticsConsts
import com.levislv.statistics.build.gradle.util.StatisticsLog
import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.Opcodes

/**
 * @author LevisLv
 * @email  levislv@levislv.com
 * @blog   https://blog.levislv.com/
 * @book   https://book.levislv.com/
 * @github https://github.com/LevisLv/
 */
class StatisticsMethodAnnotationVisitor extends AnnotationVisitor {

    private StatisticsMethodVisitor mv
    private String descriptor
    private boolean visible

    StatisticsMethodAnnotationVisitor(StatisticsMethodVisitor mv, AnnotationVisitor annotationVisitor, String descriptor, boolean visible) {
        super(Opcodes.ASM7, annotationVisitor)

        StatisticsLog.info("||---visitAnnotation: descriptor=${descriptor}, visible=${visible}")

        this.mv = mv
        this.descriptor = descriptor
        this.visible = visible
    }

    @Override
    void visit(String name, Object value) {
        StatisticsLog.info("||---visit: name=${name}, value=${value}")

        super.visit(name, value)

        if (StatisticsConsts.ANNOTATION_VISITOR_DESCRIPTOR_STATISTICS_VIEW.equals(this.descriptor)) {
            if ('parentName'.equals(name)) {
                this.mv.view.parentName = (String) value
            }
            if ('name'.equals(name)) {
                this.mv.view.name = (String) value
            }
            if ('data'.equals(name)) {
                this.mv.view.data = (String) value
            }
        }

        if ('Lbutterknife/OnClick;'.equals(this.descriptor)) {
            this.mv.useOnClickAnnotation = true
        }
        if ('Lbutterknife/OnLongClick;'.equals(this.descriptor)) {
            this.mv.useOnLongClickAnnotation = true
        }
        if ('Lbutterknife/OnTouch;'.equals(this.descriptor)) {
            this.mv.useOnTouchAnnotation = true
        }
        if ('Lbutterknife/OnFocusChange;'.equals(this.descriptor)) {
            this.mv.useOnFocusChangeAnnotation = true
        }
        if ('Lbutterknife/OnEditorAction;'.equals(this.descriptor)) {
            this.mv.useOnEditorActionAnnotation = true
        }
        if ('Lbutterknife/OnCheckedChanged;'.equals(this.descriptor)) {
            this.mv.useOnCheckedChangedAnnotation = true
        }
        if ('Lbutterknife/OnItemClick;'.equals(this.descriptor)) {
            this.mv.useOnItemClickAnnotation = true
        }
        if ('Lbutterknife/OnItemLongClick;'.equals(this.descriptor)) {
            this.mv.useOnItemLongClickAnnotation = true
        }
//        if ('Lbutterknife/OnItemSelected;'.equals(this.descriptor)) {
//            this.mv.useOnItemSelectedAnnotation = true
//        }
    }

    @Override
    void visitEnum(String name, String descriptor, String value) {
        StatisticsLog.info("||---visitEnum: name=${name}, descriptor=${descriptor}, value=${value}")

        super.visitEnum(name, descriptor, value)

        if ('Lbutterknife/OnItemSelected;'.equals(this.descriptor)) {
            if ('callback'.equals(name)
                    && 'Lbutterknife/OnItemSelected$Callback;'.equals(descriptor)
                    && 'ITEM_SELECTED'.equals(value)) {
                this.mv.useOnItemSelectedAnnotation = true
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
