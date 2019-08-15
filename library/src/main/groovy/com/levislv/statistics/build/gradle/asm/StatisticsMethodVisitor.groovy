package com.levislv.statistics.build.gradle.asm

import com.levislv.statistics.build.gradle.bean.StatisticsMethodParam
import com.levislv.statistics.build.gradle.bean.StatisticsView
import com.levislv.statistics.build.gradle.constant.StatisticsConsts
import com.levislv.statistics.build.gradle.constant.StatisticsTagConsts
import com.levislv.statistics.build.gradle.runtime.StatisticsConfig
import com.levislv.statistics.build.gradle.util.StatisticsLog
import com.levislv.statistics.build.gradle.util.StatisticsUtils
import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.Handle
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.commons.AdviceAdapter

import java.util.regex.Pattern

/**
 * @author levislv
 * @email  levislv@levislv.com
 * @blog   https://blog.levislv.com/
 * @github https://github.com/levislv/
 */
class StatisticsMethodVisitor extends AdviceAdapter {

    StatisticsClassVisitor cv
    int access
    String name
    String descriptor
    String signature
    String[] exceptions

    boolean useOnClickAnnotation
    boolean useOnLongClickAnnotation
    boolean useOnTouchAnnotation
    boolean useOnFocusChangeAnnotation
    boolean useOnEditorActionAnnotation
    boolean useOnCheckedChangedAnnotation
    boolean useOnItemClickAnnotation
    boolean useOnItemLongClickAnnotation
    boolean useOnItemSelectedAnnotation

    StatisticsView view

    protected StatisticsMethodVisitor(StatisticsClassVisitor cv, MethodVisitor methodVisitor, int access, String name, String descriptor, String signature, String[] exceptions) {
        super(Opcodes.ASM7, methodVisitor, access, name, descriptor)

        StatisticsLog.info("||---visitMethod: access=${StatisticsUtils.accCode2String(access)}, name=${name}, descriptor=${descriptor}, signature=${signature}, exceptions=${exceptions}")

        this.cv = cv
        this.access = access
        this.name = name
        this.descriptor = descriptor
        this.signature = signature
        this.exceptions = exceptions

        this.view = new StatisticsView()
    }

    @Override
    AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        AnnotationVisitor av = super.visitAnnotation(descriptor, visible)
        return av == null ? null : new StatisticsMethodAnnotationVisitor(this, av, descriptor, visible)
    }

    @Override
    void visitInvokeDynamicInsn(String name, String descriptor, Handle bootstrapMethodHandle, Object... bootstrapMethodArguments) {
        StatisticsLog.info("||---visitInvokeDynamicInsn: name=${name}, descriptor=${descriptor}, bootstrapMethodHandle=${bootstrapMethodHandle}")
        bootstrapMethodArguments.each { bootstrapMethodArgument ->
            StatisticsLog.info("||---visitInvokeDynamicInsn: bootstrapMethodArgument=${bootstrapMethodArgument}")
        }

        super.visitInvokeDynamicInsn(name, descriptor, bootstrapMethodHandle, bootstrapMethodArguments)

        // android.view.View.OnClickListener#onClick(View)的lambda表达式
        if ('onClick'.equals(name) && '()Landroid/view/View$OnClickListener;'.equals(descriptor)) {
            if (bootstrapMethodArguments != null && bootstrapMethodArguments.length == 3 && bootstrapMethodArguments[1] instanceof Handle) {
                this.cv.onClickLambdaName = ((Handle) bootstrapMethodArguments[1]).name
            }
        } else
        // android.view.View.OnLongClickListener#onLongClick(View)的lambda表达式
        if ('onLongClick'.equals(name) && '()Landroid/view/View$OnLongClickListener;'.equals(descriptor)) {
            if (bootstrapMethodArguments != null && bootstrapMethodArguments.length == 3 && bootstrapMethodArguments[1] instanceof Handle) {
                this.cv.onLongClickLambdaName = ((Handle) bootstrapMethodArguments[1]).name
            }
        } else
        // android.view.View.OnTouchListener#onTouch(View, MotionEvent)的lambda表达式
        if ('onTouch'.equals(name) && '()Landroid/view/View$OnTouchListener;'.equals(descriptor)) {
            if (bootstrapMethodArguments != null && bootstrapMethodArguments.length == 3 && bootstrapMethodArguments[1] instanceof Handle) {
                this.cv.onTouchLambdaName = ((Handle) bootstrapMethodArguments[1]).name
            }
        } else
        // android.view.View.OnFocusChangeListener#onFocusChange(View, boolean)的lambda表达式
        if ('onFocusChange'.equals(name) && '()Landroid/view/View$OnFocusChangeListener;'.equals(descriptor)) {
            if (bootstrapMethodArguments != null && bootstrapMethodArguments.length == 3 && bootstrapMethodArguments[1] instanceof Handle) {
                this.cv.onFocusChangeLambdaName = ((Handle) bootstrapMethodArguments[1]).name
            }
        } else
        // android.widget.TextView.OnEditorActionListener#onEditorAction(TextView, int, KeyEvent)的lambda表达式
        if ('onEditorAction'.equals(name) && '()Landroid/widget/TextView$OnEditorActionListener;'.equals(descriptor)) {
            if (bootstrapMethodArguments != null && bootstrapMethodArguments.length == 3 && bootstrapMethodArguments[1] instanceof Handle) {
                this.cv.onEditorActionLambdaName = ((Handle) bootstrapMethodArguments[1]).name
            }
        } else
        // android.widget.CompoundButton.OnCheckedChangeListener#onCheckedChanged(CompoundButton, boolean)的lambda表达式
        if ('onCheckedChanged'.equals(name) && '()Landroid/widget/CompoundButton$OnCheckedChangeListener;'.equals(descriptor)) {
            if (bootstrapMethodArguments != null && bootstrapMethodArguments.length == 3 && bootstrapMethodArguments[1] instanceof Handle) {
                this.cv.onCompoundButtonCheckedChangedLambdaName = ((Handle) bootstrapMethodArguments[1]).name
            }
        } else
        // android.widget.RadioGroup.OnCheckedChangeListener#onCheckedChanged(CompoundButton, int)的lambda表达式
        if ('onCheckedChanged'.equals(name) && '()Landroid/widget/RadioGroup$OnCheckedChangeListener;'.equals(descriptor)) {
            if (bootstrapMethodArguments != null && bootstrapMethodArguments.length == 3 && bootstrapMethodArguments[1] instanceof Handle) {
                this.cv.onRadioGroupCheckedChangedLambdaName = ((Handle) bootstrapMethodArguments[1]).name
            }
        } else
        // android.widget.RatingBar.OnRatingBarChangeListener#onRatingChanged(RatingBar, float, boolean)的lambda表达式
        if ('onRatingChanged'.equals(name) && '()Landroid/widget/RatingBar$OnRatingBarChangeListener;'.equals(descriptor)) {
            if (bootstrapMethodArguments != null && bootstrapMethodArguments.length == 3 && bootstrapMethodArguments[1] instanceof Handle) {
                Handle handle = (Handle) bootstrapMethodArguments[1]
                this.cv.onRatingChangedLambdaName = ((Handle) bootstrapMethodArguments[1]).name
            }
        } else
        // android.widget.AdapterView.OnItemClickListener#onItemClick(AdapterView<?>, View, int, long)的lambda表达式
        if ('onItemClick'.equals(name) && '()Landroid/widget/AdapterView$OnItemClickListener;'.equals(descriptor)) {
            if (bootstrapMethodArguments != null && bootstrapMethodArguments.length == 3 && bootstrapMethodArguments[1] instanceof Handle) {
                this.cv.onItemClickLambdaName = ((Handle) bootstrapMethodArguments[1]).name
            }
        } else
        // android.widget.AdapterView.OnItemLongClickListener#onItemLongClick(AdapterView<?>, View, int, long)的lambda表达式
        if ('onItemLongClick'.equals(name) && '()Landroid/widget/AdapterView$OnItemLongClickListener;'.equals(descriptor)) {
            if (bootstrapMethodArguments != null && bootstrapMethodArguments.length == 3 && bootstrapMethodArguments[1] instanceof Handle) {
                this.cv.onItemLongClickLambdaName = ((Handle) bootstrapMethodArguments[1]).name
            }
        } else
        // android.widget.ExpandableListView.OnGroupClickListener#onGroupClick(ExpandableListView, View, int, long)的lambda表达式
        if ('onGroupClick'.equals(name) && '()Landroid/widget/ExpandableListView$OnGroupClickListener;'.equals(descriptor)) {
            if (bootstrapMethodArguments != null && bootstrapMethodArguments.length == 3 && bootstrapMethodArguments[1] instanceof Handle) {
                this.cv.onGroupClickLambdaName = ((Handle) bootstrapMethodArguments[1]).name
            }
        } else
        // android.widget.ExpandableListView.OnChildClickListener#onChildClick(ExpandableListView, View, int, int, long)的lambda表达式
        if ('onChildClick'.equals(name) && '()Landroid/widget/ExpandableListView$OnChildClickListener;'.equals(descriptor)) {
            if (bootstrapMethodArguments != null && bootstrapMethodArguments.length == 3 && bootstrapMethodArguments[1] instanceof Handle) {
                this.cv.onChildClickLambdaName = ((Handle) bootstrapMethodArguments[1]).name
            }
        }
    }

    @Override
    protected void onMethodEnter() {
        StatisticsLog.info("||---onMethodEnter")

        // 满足条件说明使用了StatisticsPage注解
        if (this.cv.page.pkgName != null && this.cv.page.id > 0 && this.cv.page.name != null && this.cv.page.data != null) {
            if (this.cv.page.isActivity) {
                // android.app.Activity#onPause()
                if ('onPause'.equals(this.name) && '()V'.equals(this.descriptor)) {
                    this.cv.onActivityPauseMissing = false

                    onActivityLifecycleCallbacksMethodExit('onPause')
                } else
                // android.app.Activity#onStop()
                if ('onStop'.equals(this.name) && '()V'.equals(this.descriptor)) {
                    this.cv.onActivityStopMissing = false

                    onActivityLifecycleCallbacksMethodExit('onStop')
                } else
                // android.app.Activity#onDestroy()
                if ('onDestroy'.equals(this.name) && '()V'.equals(this.descriptor)) {
                    this.cv.onActivityDestroyMissing = false

                    onActivityLifecycleCallbacksMethodExit('onDestroy')
                } else
                // android.app.Activity#dispatchTouchEvent(MotionEvent)
                if ('dispatchTouchEvent'.equals(this.name) && '(Landroid/view/MotionEvent;)Z'.equals(this.descriptor)) {
                    this.cv.dispatchTouchEventMissing = false

                    if (StatisticsConfig.enableHeatMap()) {
                        dispatchTouchEventMethodEnter()
                    }
                }
            } else if (this.cv.page.isFragment) {
                // android.support.v4.app.Fragment#onPause()
                if ('onPause'.equals(this.name) && '()V'.equals(this.descriptor)) {
                    this.cv.onFragmentPauseMissing = false

                    onFragmentLifecycleCallbacksMethodExit('onPause')
                } else
                // android.support.v4.app.Fragment#onDestroy()
                if ('onDestroy'.equals(this.name) && '()V'.equals(this.descriptor)) {
                    this.cv.onFragmentDestroyMissing = false

                    onFragmentLifecycleCallbacksMethodExit('onDestroy')
                }
            }


            // @OnClick(R.id.xxx) void onClick(View)
            // 或
            // android.view.View.OnClickListener#onClick(View)
            if ((this.useOnClickAnnotation
                    || this.cv.interfaces.contains('android/view/View$OnClickListener')
                    && 'onClick'.equals(this.name))
                    && '(Landroid/view/View;)V'.equals(this.descriptor)) {
                onViewHandleMethodEnter('onClick', 1, null)
            }
            // android.view.View.OnClickListener#onClick(View)的lambda表达式
            if (isLambdaExpression(this.cv.onClickLambdaName, '(Landroid/view/View;)V')) {
                onViewHandledMethodLambdaEnter('onClick', 0, null)
            }


            // @OnLongClick(R.id.xxx) boolean onLongClick(View)
            // 或
            // android.view.View.setOnLongClickListener#onLongClick(View)
            if ((this.useOnLongClickAnnotation
                    || this.cv.interfaces.contains('android/view/View$OnLongClickListener')
                    && 'onLongClick'.equals(this.name))
                    && '(Landroid/view/View;)Z'.equals(this.descriptor)) {
                onViewHandleMethodEnter('onLongClick', 1, null)
            }
            // android.view.View.OnLongClickListener#onLongClick(View)的lambda表达式
            if (isLambdaExpression(this.cv.onLongClickLambdaName, '(Landroid/view/View;)Z')) {
                onViewHandledMethodLambdaEnter('onLongClick', 0, null)
            }


            // @OnTouch(R.id.xxx) boolean onTouch(View, MotionEvent)
            // 或
            // android.view.View.setOnTouchListener#onTouch(View, MotionEvent)
            if ((this.useOnTouchAnnotation
                    || this.cv.interfaces.contains('android/view/View$OnTouchListener')
                    && 'onTouch'.equals(this.name))
                    && '(Landroid/view/View;Landroid/view/MotionEvent;)Z'.equals(this.descriptor)) {
                if (StatisticsConfig.enableViewOnTouch()) {
                    StatisticsMethodParam[] statisticsMethodParams = [
                            new StatisticsMethodParam(ALOAD, 2, StatisticsTagConsts.View.OnTouchListener.TAG_KEY_MOTION_EVENT, null, null)
                    ]
                    onViewHandleMethodEnter('onTouch', 1, statisticsMethodParams)
                }
            }
            // android.view.View.setOnTouchListener#onTouch(View, MotionEvent)的lambda表达式
            if (isLambdaExpression(this.cv.onTouchLambdaName, '(Landroid/view/View;Landroid/view/MotionEvent;)Z')) {
                if (StatisticsConfig.enableViewOnTouch()) {
                    StatisticsMethodParam[] statisticsMethodParams = [
                            new StatisticsMethodParam(ALOAD, 1, StatisticsTagConsts.View.OnTouchListener.TAG_KEY_MOTION_EVENT, null, null)
                    ]
                    onViewHandledMethodLambdaEnter('onTouch', 0, statisticsMethodParams)
                }
            }


            // @OnFocusChange(R.id.xxx) void onFocusChange(View, boolean)
            // 或
            // android.view.View.setOnFocusChangeListener#onFocusChange(View, boolean)
            if ((this.useOnFocusChangeAnnotation
                    || this.cv.interfaces.contains('android/view/View$OnFocusChangeListener')
                    && 'onFocusChange'.equals(this.name))
                    && '(Landroid/view/View;Z)V'.equals(this.descriptor)) {
                StatisticsMethodParam[] statisticsMethodParams = [
                        new StatisticsMethodParam(ILOAD, 2, StatisticsTagConsts.View.OnFocusChangeListener.TAG_KEY_HAS_FOCUS, 'Z', 'java/lang/Boolean')
                ]
                onViewHandleMethodEnter('onFocusChange', 1, statisticsMethodParams)
            }
            // android.view.View.setOnFocusChangeListener#onFocusChange(View, boolean)的lambda表达式
            if (isLambdaExpression(this.cv.onFocusChangeLambdaName, '(Landroid/view/View;Z)V')) {
                StatisticsMethodParam[] statisticsMethodParams = [
                        new StatisticsMethodParam(ILOAD, 1, StatisticsTagConsts.View.OnFocusChangeListener.TAG_KEY_HAS_FOCUS, 'Z', 'java/lang/Boolean')
                ]
                onViewHandledMethodLambdaEnter('onFocusChange', 0, statisticsMethodParams)
            }


            // @OnEditorAction(R.id.xxx) boolean onEditorAction(TextView, int, KeyEvent)
            // 或
            // android.widget.TextView.setOnEditorActionListener#onEditorAction(TextView, int, KeyEvent)
            if ((this.useOnEditorActionAnnotation
                    || this.cv.interfaces.contains('android/widget/TextView$OnEditorActionListener')
                    && 'onEditorAction'.equals(this.name))
                    && '(Landroid/widget/TextView;ILandroid/view/KeyEvent;)Z'.equals(this.descriptor)) {
                StatisticsMethodParam[] statisticsMethodParams = [
                        new StatisticsMethodParam(ILOAD, 2, StatisticsTagConsts.TextView.OnEditorActionListener.TAG_KEY_ACTION_ID, 'I', 'java/lang/Integer'),
                        new StatisticsMethodParam(ALOAD, 3, StatisticsTagConsts.TextView.OnEditorActionListener.TAG_KEY_KEY_EVENT, null, null)
                ]
                onViewHandleMethodEnter('onEditorAction', 1, statisticsMethodParams)
            }
            // android.widget.TextView.setOnEditorActionListener#onEditorAction(TextView, int, KeyEvent)的lambda表达式
            if (isLambdaExpression(this.cv.onEditorActionLambdaName, '(Landroid/widget/TextView;ILandroid/view/KeyEvent;)Z')) {
                StatisticsMethodParam[] statisticsMethodParams = [
                        new StatisticsMethodParam(ILOAD, 1, StatisticsTagConsts.TextView.OnEditorActionListener.TAG_KEY_ACTION_ID, 'I', 'java/lang/Integer'),
                        new StatisticsMethodParam(ALOAD, 2, StatisticsTagConsts.TextView.OnEditorActionListener.TAG_KEY_KEY_EVENT, null, null)
                ]
                onViewHandledMethodLambdaEnter('onEditorAction', 0, statisticsMethodParams)
            }


            // @OnCheckedChanged(R.id.xxx) void onCheckedChanged(CompoundButton, boolean)
            // 或
            // android.widget.CompoundButton.setOnCheckedChangeListener#onCheckedChanged(CompoundButton, boolean)
            if ((this.useOnCheckedChangedAnnotation
                    || this.cv.interfaces.contains('android/widget/CompoundButton$OnCheckedChangeListener')
                    && 'onCheckedChanged'.equals(this.name))
                    && '(Landroid/widget/CompoundButton;Z)V'.equals(this.descriptor)) {
                StatisticsMethodParam[] statisticsMethodParams = [
                        new StatisticsMethodParam(ILOAD, 2, StatisticsTagConsts.CompoundButton.OnCheckedChangeListener.TAG_KEY_IS_CHECKED, 'Z', 'java/lang/Boolean')
                ]
                onViewHandleMethodEnter('onCheckedChanged', 1, statisticsMethodParams)
            }
            // android.widget.CompoundButton.setOnCheckedChangeListener#onCheckedChanged(CompoundButton, boolean)的lambda表达式
            if (isLambdaExpression(this.cv.onCompoundButtonCheckedChangedLambdaName, '(Landroid/widget/CompoundButton;Z)V')) {
                StatisticsMethodParam[] statisticsMethodParams = [
                        new StatisticsMethodParam(ILOAD, 1, StatisticsTagConsts.CompoundButton.OnCheckedChangeListener.TAG_KEY_IS_CHECKED, 'Z', 'java/lang/Boolean')
                ]
                onViewHandledMethodLambdaEnter('onCheckedChanged', 0, statisticsMethodParams)
            }


            // android.widget.RadioGroup.setOnCheckedChangeListener#onCheckedChanged(RadioGroup, int)
            if (this.cv.interfaces.contains('android/widget/RadioGroup$OnCheckedChangeListener')
                    && 'onCheckedChanged'.equals(this.name)
                    && '(Landroid/widget/RadioGroup;I)V'.equals(this.descriptor)) {
                StatisticsMethodParam[] statisticsMethodParams = [
                        new StatisticsMethodParam(ILOAD, 2, StatisticsTagConsts.RadioGroup.OnCheckedChangeListener.TAG_KEY_CHECKED_ID, 'I', 'java/lang/Integer')
                ]
                onViewHandleMethodEnter('onCheckedChanged', 1, statisticsMethodParams)
            }
            // android.widget.RadioGroup.setOnCheckedChangeListener#onCheckedChanged(RadioGroup, int)的lambda表达式
            if (isLambdaExpression(this.cv.onRadioGroupCheckedChangedLambdaName, '(Landroid/widget/RadioGroup;I)V')) {
                StatisticsMethodParam[] statisticsMethodParams = [
                        new StatisticsMethodParam(ILOAD, 1, StatisticsTagConsts.RadioGroup.OnCheckedChangeListener.TAG_KEY_CHECKED_ID, 'I', 'java/lang/Integer')
                ]
                onViewHandledMethodLambdaEnter('onCheckedChanged', 0, statisticsMethodParams)
            }


            // android.widget.SeekBar.setOnSeekBarChangeListener#onProgressChanged(SeekBar, int, boolean)
            if (this.cv.interfaces.contains('android/widget/SeekBar$OnSeekBarChangeListener')
                    && 'onProgressChanged'.equals(this.name)
                    && '(Landroid/widget/SeekBar;IZ)V'.equals(this.descriptor)) {
                StatisticsMethodParam[] statisticsMethodParams = [
                        new StatisticsMethodParam(ILOAD, 2, StatisticsTagConsts.SeekBar.OnSeekBarChangeListener.TAG_KEY_PROGRESS, 'I', 'java/lang/Integer'),
                        new StatisticsMethodParam(ILOAD, 3, StatisticsTagConsts.SeekBar.OnSeekBarChangeListener.TAG_KEY_FROM_USER, 'Z', 'java/lang/Boolean')
                ]
                onViewHandleMethodEnter('onProgressChanged', 1, statisticsMethodParams)
            }


            // android.widget.SeekBar.setOnSeekBarChangeListener#onStartTrackingTouch(SeekBar)
            if (this.cv.interfaces.contains('android/widget/SeekBar$OnSeekBarChangeListener')
                    && 'onStartTrackingTouch'.equals(this.name)
                    && '(Landroid/widget/SeekBar;)V'.equals(this.descriptor)) {
                onViewHandleMethodEnter('onStartTrackingTouch', 1, null)
            }


            // android.widget.SeekBar.setOnSeekBarChangeListener#onStopTrackingTouch(SeekBar)
            if (this.cv.interfaces.contains('android/widget/SeekBar$OnSeekBarChangeListener')
                    && 'onStopTrackingTouch'.equals(this.name)
                    && '(Landroid/widget/SeekBar;)V'.equals(this.descriptor)) {
                onViewHandleMethodEnter('onStopTrackingTouch', 1, null)
            }


            // android.widget.RatingBar.setOnRatingBarChangeListener#onRatingChanged(RatingBar, float, boolean)
            if (this.cv.interfaces.contains('android/widget/RatingBar$OnRatingBarChangeListener')
                    && 'onRatingChanged'.equals(this.name)
                    && '(Landroid/widget/RatingBar;FZ)V'.equals(this.descriptor)) {
                StatisticsMethodParam[] statisticsMethodParams = [
                        new StatisticsMethodParam(FLOAD, 2, StatisticsTagConsts.RatingBar.OnRatingBarChangeListener.TAG_KEY_RATING, 'F', 'java/lang/Float'),
                        new StatisticsMethodParam(ILOAD, 3, StatisticsTagConsts.RatingBar.OnRatingBarChangeListener.TAG_KEY_FROM_USER, 'Z', 'java/lang/Boolean')
                ]
                onViewHandleMethodEnter('onRatingChanged', 1, statisticsMethodParams)
            }
            // android.widget.RatingBar.setOnRatingBarChangeListener#onRatingChanged(RatingBar, float, boolean)的lambda表达式
            if (isLambdaExpression(this.cv.onRatingChangedLambdaName, '(Landroid/widget/RatingBar;FZ)V')) {
                StatisticsMethodParam[] statisticsMethodParams = [
                        new StatisticsMethodParam(FLOAD, 1, StatisticsTagConsts.RatingBar.OnRatingBarChangeListener.TAG_KEY_RATING, 'F', 'java/lang/Float'),
                        new StatisticsMethodParam(ILOAD, 2, StatisticsTagConsts.RatingBar.OnRatingBarChangeListener.TAG_KEY_FROM_USER, 'Z', 'java/lang/Boolean')
                ]
                onViewHandledMethodLambdaEnter('onRatingChanged', 0, statisticsMethodParams)
            }


            // @OnItemClick(R.id.xxx) void onItemClick(AdapterView<?>, View, int, long)
            // 或
            // android.widget.AdapterView.setOnItemClickListener#onItemClick(AdapterView<?>, View, int, long)
            if ((this.useOnItemClickAnnotation
                    || this.cv.interfaces.contains('android/widget/AdapterView$OnItemClickListener')
                    && 'onItemClick'.equals(this.name))
                    && '(Landroid/widget/AdapterView;Landroid/view/View;IJ)V'.equals(this.descriptor)) {
                // 使用了StatisticsView注解，parent覆盖已有contentDescription
                if (this.view.parentName != null) {
                    // view.setContentDescription(this.view.name);
                    mv.visitVarInsn(ALOAD, 1)
                    mv.visitLdcInsn(this.view.parentName)
                    mv.visitMethodInsn(INVOKEVIRTUAL,
                            "android/view/View",
                            "setContentDescription",
                            "(Ljava/lang/CharSequence;)V",
                            false)
                }
                StatisticsMethodParam[] statisticsMethodParams = [
                        new StatisticsMethodParam(ALOAD, 1, StatisticsTagConsts.AdapterView.OnItemClickListener.TAG_KEY_PARENT, null, null),
                        new StatisticsMethodParam(ILOAD, 3, StatisticsTagConsts.AdapterView.OnItemClickListener.TAG_KEY_ITEM_POSITION, 'I', 'java/lang/Integer'),
                        new StatisticsMethodParam(LLOAD, 4, StatisticsTagConsts.AdapterView.OnItemClickListener.TAG_KEY_ITEM_ID, 'J', 'java/lang/Long')
                ]
                onViewHandleMethodEnter('onItemClick', 2, statisticsMethodParams)
            }
            // android.widget.AdapterView.setOnItemClickListener#onItemClick(AdapterView<?>, View, int, long)的lambda表达式
            if (isLambdaExpression(this.cv.onItemClickLambdaName, '(Landroid/widget/AdapterView;Landroid/view/View;IJ)V')) {
                StatisticsMethodParam[] statisticsMethodParams = [
                        new StatisticsMethodParam(ALOAD, 0, StatisticsTagConsts.AdapterView.OnItemClickListener.TAG_KEY_PARENT, null, null),
                        new StatisticsMethodParam(ILOAD, 2, StatisticsTagConsts.AdapterView.OnItemClickListener.TAG_KEY_ITEM_POSITION, 'I', 'java/lang/Integer'),
                        new StatisticsMethodParam(LLOAD, 3, StatisticsTagConsts.AdapterView.OnItemClickListener.TAG_KEY_ITEM_ID, 'J', 'java/lang/Long')
                ]
                onViewHandledMethodLambdaEnter('onItemClick', 1, statisticsMethodParams)
            }


            // @OnItemLongClick(R.id.xxx) boolean onItemLongClick(AdapterView<?>, View, int, long)
            // 或
            // android.widget.AdapterView.setOnItemLongClickListener#onItemLongClick(AdapterView<?>, View, int, long)
            if ((this.useOnItemLongClickAnnotation
                    || this.cv.interfaces.contains('android/widget/AdapterView$OnItemLongClickListener')
                    && 'onItemLongClick'.equals(this.name))
                    && '(Landroid/widget/AdapterView;Landroid/view/View;IJ)Z'.equals(this.descriptor)) {
                // 使用了StatisticsView注解，parent覆盖已有contentDescription
                if (this.view.parentName != null) {
                    // view.setContentDescription(this.view.name);
                    mv.visitVarInsn(ALOAD, 1)
                    mv.visitLdcInsn(this.view.parentName)
                    mv.visitMethodInsn(INVOKEVIRTUAL,
                            "android/view/View",
                            "setContentDescription",
                            "(Ljava/lang/CharSequence;)V",
                            false)
                }
                StatisticsMethodParam[] statisticsMethodParams = [
                        new StatisticsMethodParam(ALOAD, 1, StatisticsTagConsts.AdapterView.OnItemLongClickListener.TAG_KEY_PARENT, null, null),
                        new StatisticsMethodParam(ILOAD, 3, StatisticsTagConsts.AdapterView.OnItemLongClickListener.TAG_KEY_ITEM_POSITION, 'I', 'java/lang/Integer'),
                        new StatisticsMethodParam(LLOAD, 4, StatisticsTagConsts.AdapterView.OnItemLongClickListener.TAG_KEY_ITEM_ID, 'J', 'java/lang/Long')
                ]
                onViewHandleMethodEnter('onItemLongClick', 2, statisticsMethodParams)
            }
            // android.widget.AdapterView.setOnItemLongClickListener#onItemLongClick(AdapterView<?>, View, int, long)的lambda表达式
            if (isLambdaExpression(this.cv.onItemLongClickLambdaName, '(Landroid/widget/AdapterView;Landroid/view/View;IJ)Z')) {
                StatisticsMethodParam[] statisticsMethodParams = [
                        new StatisticsMethodParam(ALOAD, 0, StatisticsTagConsts.AdapterView.OnItemLongClickListener.TAG_KEY_PARENT, null, null),
                        new StatisticsMethodParam(ILOAD, 2, StatisticsTagConsts.AdapterView.OnItemLongClickListener.TAG_KEY_ITEM_POSITION, 'I', 'java/lang/Integer'),
                        new StatisticsMethodParam(LLOAD, 3, StatisticsTagConsts.AdapterView.OnItemLongClickListener.TAG_KEY_ITEM_ID, 'J', 'java/lang/Long')
                ]
                onViewHandledMethodLambdaEnter('onItemLongClick', 1, statisticsMethodParams)
            }


            // @OnItemSelected(value = R.id.xxx, callback = OnItemSelected.Callback.ITEM_SELECTED) void onItemSelected(AdapterView<?>, View, int, long)
            // 或
            // android.widget.AdapterView.setOnItemSelectedListener#onItemSelected(AdapterView<?>, View, int, long)
            if ((this.useOnItemSelectedAnnotation
                    || this.cv.interfaces.contains('android/widget/AdapterView$OnItemSelectedListener')
                    && 'onItemSelected'.equals(this.name))
                    && '(Landroid/widget/AdapterView;Landroid/view/View;IJ)V'.equals(this.descriptor)) {
                // 使用了StatisticsView注解，parent覆盖已有contentDescription
                if (this.view.parentName != null) {
                    // view.setContentDescription(this.view.name);
                    mv.visitVarInsn(ALOAD, 1)
                    mv.visitLdcInsn(this.view.parentName)
                    mv.visitMethodInsn(INVOKEVIRTUAL,
                            "android/view/View",
                            "setContentDescription",
                            "(Ljava/lang/CharSequence;)V",
                            false)
                }
                StatisticsMethodParam[] statisticsMethodParams = [
                        new StatisticsMethodParam(ALOAD, 1, StatisticsTagConsts.AdapterView.OnItemSelectedListener.TAG_KEY_PARENT, null, null),
                        new StatisticsMethodParam(ILOAD, 3, StatisticsTagConsts.AdapterView.OnItemSelectedListener.TAG_KEY_ITEM_POSITION, 'I', 'java/lang/Integer'),
                        new StatisticsMethodParam(LLOAD, 4, StatisticsTagConsts.AdapterView.OnItemSelectedListener.TAG_KEY_ITEM_ID, 'J', 'java/lang/Long')
                ]
                onViewHandleMethodEnter('onItemSelected', 2, statisticsMethodParams)
            }


            // android.widget.ExpandableListView.setOnGroupClickListener#onGroupClick(ExpandableListView, View, int, long)
            if (this.cv.interfaces.contains('android/widget/ExpandableListView$OnGroupClickListener')
                    && 'onGroupClick'.equals(this.name)
                    && '(Landroid/widget/ExpandableListView;Landroid/view/View;IJ)Z'.equals(this.descriptor)) {
                // 使用了StatisticsView注解，parent覆盖已有contentDescription
                if (this.view.parentName != null) {
                    // view.setContentDescription(this.view.name);
                    mv.visitVarInsn(ALOAD, 1)
                    mv.visitLdcInsn(this.view.parentName)
                    mv.visitMethodInsn(INVOKEVIRTUAL,
                            "android/view/View",
                            "setContentDescription",
                            "(Ljava/lang/CharSequence;)V",
                            false)
                }
                StatisticsMethodParam[] statisticsMethodParams = [
                        new StatisticsMethodParam(ALOAD, 1, StatisticsTagConsts.ExpandableListView.OnGroupClickListener.TAG_KEY_PARENT, null, null),
                        new StatisticsMethodParam(ILOAD, 3, StatisticsTagConsts.ExpandableListView.OnGroupClickListener.TAG_KEY_ITEM_POSITION, 'I', 'java/lang/Integer'),
                        new StatisticsMethodParam(LLOAD, 4, StatisticsTagConsts.ExpandableListView.OnGroupClickListener.TAG_KEY_ITEM_ID, 'J', 'java/lang/Long')
                ]
                onViewHandleMethodEnter('onGroupClick', 2, statisticsMethodParams)
            }
            // android.widget.ExpandableListView.setOnGroupClickListener#onGroupClick(ExpandableListView, View, int, long)的lambda表达式
            if (isLambdaExpression(this.cv.onGroupClickLambdaName, '(Landroid/widget/ExpandableListView;Landroid/view/View;IJ)Z')) {
                StatisticsMethodParam[] statisticsMethodParams = [
                        new StatisticsMethodParam(ALOAD, 0, StatisticsTagConsts.ExpandableListView.OnGroupClickListener.TAG_KEY_PARENT, null, null),
                        new StatisticsMethodParam(ILOAD, 2, StatisticsTagConsts.ExpandableListView.OnGroupClickListener.TAG_KEY_ITEM_POSITION, 'I', 'java/lang/Integer'),
                        new StatisticsMethodParam(LLOAD, 3, StatisticsTagConsts.ExpandableListView.OnGroupClickListener.TAG_KEY_ITEM_ID, 'J', 'java/lang/Long')
                ]
                onViewHandledMethodLambdaEnter('onGroupClick', 1, statisticsMethodParams)
            }


            // android.widget.ExpandableListView.setOnChildClickListener#onChildClick(ExpandableListView, View, int, int, long)
            if (this.cv.interfaces.contains('android/widget/ExpandableListView$OnChildClickListener')
                    && 'onChildClick'.equals(this.name)
                    && '(Landroid/widget/ExpandableListView;Landroid/view/View;IIJ)Z'.equals(this.descriptor)) {
                // 使用了StatisticsView注解，parent覆盖已有contentDescription
                if (this.view.parentName != null) {
                    // view.setContentDescription(this.view.name);
                    mv.visitVarInsn(ALOAD, 1)
                    mv.visitLdcInsn(this.view.parentName)
                    mv.visitMethodInsn(INVOKEVIRTUAL,
                            "android/view/View",
                            "setContentDescription",
                            "(Ljava/lang/CharSequence;)V",
                            false)
                }
                StatisticsMethodParam[] statisticsMethodParams = [
                        new StatisticsMethodParam(ALOAD, 1, StatisticsTagConsts.ExpandableListView.OnChildClickListener.TAG_KEY_PARENT, null, null),
                        new StatisticsMethodParam(ILOAD, 3, StatisticsTagConsts.ExpandableListView.OnChildClickListener.TAG_KEY_GROUP_POSITION, 'I', 'java/lang/Integer'),
                        new StatisticsMethodParam(ILOAD, 4, StatisticsTagConsts.ExpandableListView.OnChildClickListener.TAG_KEY_CHILD_POSITION, 'I', 'java/lang/Integer'),
                        new StatisticsMethodParam(LLOAD, 5, StatisticsTagConsts.ExpandableListView.OnChildClickListener.TAG_KEY_ITEM_ID, 'J', 'java/lang/Long')
                ]
                onViewHandleMethodEnter('onChildClick', 2, statisticsMethodParams)
            }
            // android.widget.ExpandableListView.setOnChildClickListener#onChildClick(ExpandableListView, View, int, int, long)的lambda表达式
            if (isLambdaExpression(this.cv.onChildClickLambdaName, '(Landroid/widget/ExpandableListView;Landroid/view/View;IIJ)Z')) {
                StatisticsMethodParam[] statisticsMethodParams = [
                        new StatisticsMethodParam(ALOAD, 0, StatisticsTagConsts.ExpandableListView.OnChildClickListener.TAG_KEY_PARENT, null, null),
                        new StatisticsMethodParam(ILOAD, 2, StatisticsTagConsts.ExpandableListView.OnChildClickListener.TAG_KEY_GROUP_POSITION, 'I', 'java/lang/Integer'),
                        new StatisticsMethodParam(ILOAD, 3, StatisticsTagConsts.ExpandableListView.OnChildClickListener.TAG_KEY_CHILD_POSITION, 'I', 'java/lang/Integer'),
                        new StatisticsMethodParam(LLOAD, 4, StatisticsTagConsts.ExpandableListView.OnChildClickListener.TAG_KEY_ITEM_ID, 'J', 'java/lang/Long')
                ]
                onViewHandledMethodLambdaEnter('onChildClick', 1, statisticsMethodParams)
            }
        }
    }

    @Override
    protected void onMethodExit(int opcode) {
        StatisticsLog.info("||---onMethodExit: opcode=${StatisticsUtils.getOpName(opcode)}")

        if (opcode == RETURN) {
            // 满足条件说明使用了StatisticsPage注解
            if (this.cv.page.pkgName != null && this.cv.page.id > 0 && this.cv.page.name != null && this.cv.page.data != null) {
                if (this.cv.page.isActivity) {
                    // android.app.Activity#onCreate()
                    if ('onCreate'.equals(this.name) && '(Landroid/os/Bundle;)V'.equals(this.descriptor)) {
                        this.cv.onActivityCreateMissing = false

                        onActivityLifecycleCallbacksMethodExit('onCreate')
                    } else
                    // android.app.Activity#onStart(Bundle)
                    if ('onStart'.equals(this.name) && '()V'.equals(this.descriptor)) {
                        this.cv.onActivityStartMissing = false

                        onActivityLifecycleCallbacksMethodExit('onStart')
                    } else
                    // android.app.Activity#onResume()
                    if ('onResume'.equals(this.name) && '()V'.equals(this.descriptor)) {
                        this.cv.onActivityResumeMissing = false

                        onActivityLifecycleCallbacksMethodExit('onResume')
                    }
                } else if (this.cv.page.isFragment) {
                    // android.support.v4.app.Fragment#onViewCreated(View, Bundle)
                    if ('onViewCreated'.equals(this.name) && '(Landroid/view/View;Landroid/os/Bundle;)V'.equals(this.descriptor)) {
                        this.cv.onFragmentViewCreatedMissing = false

                        onFragmentLifecycleCallbacksMethodExit('onViewCreated')
                    } else
                    // android.support.v4.app.Fragment#onResume()
                    if ('onResume'.equals(this.name) && '()V'.equals(this.descriptor)) {
                        this.cv.onFragmentResumeMissing = false

                        onFragmentLifecycleCallbacksMethodExit('onResume')
                    } else
                    // android.support.v4.app.Fragment#setUserVisibleHint(boolean)
                    if ('setUserVisibleHint'.equals(this.name) && '(Z)V'.equals(this.descriptor)) {
                        this.cv.setUserVisibleHintMissing = false

                        onFragmentLifecycleCallbacksMethodExit('setUserVisibleHint')
                    } else
                    // android.support.v4.app.Fragment#onHiddenChanged(boolean)
                    if ('onHiddenChanged'.equals(this.name) && '(Z)V'.equals(this.descriptor)) {
                        this.cv.onHiddenChangedMissing = false

                        onFragmentLifecycleCallbacksMethodExit('onHiddenChanged')
                    }
                }
            }
        }
    }

    /**
     * 在Activity生命周期回调方法进入添加代码
     *
     * @param methodName
     */
    private void onActivityLifecycleCallbacksMethodExit(String methodName) {
        if (methodName == null) {
            return
        }

        mv.visitVarInsn(ALOAD, 0)
        mv.visitLdcInsn(this.cv.page.pkgName)
        mv.visitLdcInsn(new Integer(this.cv.page.id))
        mv.visitLdcInsn(this.cv.page.name)
        mv.visitLdcInsn(Pattern.matches('\\{.*}', this.cv.page.data) ? this.cv.page.data : '{}')
        mv.visitMethodInsn(INVOKESTATIC,
                StatisticsConsts.METHOD_VISITOR_OWNER_STATISTICS_ACTIVITY_HELPER,
                methodName,
                "(${StatisticsConsts.CLASS_VISITOR_DESCRIPTOR_ACTIVITY}Ljava/lang/String;ILjava/lang/String;Ljava/lang/String;)V",
                false)
    }

    /**
     * 在dispatchTouchEventEnter方法进入添加代码
     */
    private void dispatchTouchEventMethodEnter() {
        String methodName = 'dispatchTouchEvent'

        mv.visitVarInsn(ALOAD, 0)
        mv.visitLdcInsn(this.cv.page.pkgName)
        mv.visitVarInsn(ALOAD, 1)
        mv.visitMethodInsn(INVOKESTATIC,
                StatisticsConsts.METHOD_VISITOR_OWNER_STATISTICS_TOUCH_HELPER,
                methodName,
                "(${StatisticsConsts.CLASS_VISITOR_DESCRIPTOR_ACTIVITY}Ljava/lang/String;Landroid/view/MotionEvent;)V",
                false)
    }

    /**
     * 在Fragment生命周期回调方法进入或退出时添加代码
     *
     * @param methodName
     */
    private void onFragmentLifecycleCallbacksMethodExit(String methodName) {
        if (methodName == null) {
            return
        }

        mv.visitVarInsn(ALOAD, 0)
        mv.visitLdcInsn(this.cv.page.pkgName)
        mv.visitLdcInsn(new Integer(this.cv.page.id))
        mv.visitLdcInsn(this.cv.page.name)
        mv.visitLdcInsn(Pattern.matches('\\{.*}', this.cv.page.data) ? this.cv.page.data : '{}')
        mv.visitMethodInsn(INVOKESTATIC,
                StatisticsConsts.METHOD_VISITOR_OWNER_STATISTICS_FRAGMENT_HELPER,
                methodName,
                "(${StatisticsConsts.CLASS_VISITOR_DESCRIPTOR_FRAGMENT}Ljava/lang/String;ILjava/lang/String;Ljava/lang/String;)V",
                false)
    }

    /**
     * 进入view的各种回调方法插桩
     *
     * @param methodName 方法名
     * @param viewParamIndex view是第几个参数，从1开始索引
     * @param params 参数数组
     */
    private void onViewHandleMethodEnter(String methodName, int viewParamIndex, StatisticsMethodParam[] otherParams) {
        // 使用了StatisticsView注解，覆盖已有contentDescription
        if (this.view.pkgName != null && this.view.name != null) {
            // view.setContentDescription(this.view.name);
            mv.visitVarInsn(ALOAD, viewParamIndex)
            mv.visitLdcInsn(this.view.name == '' ? 'null' : this.view.name)
            mv.visitMethodInsn(INVOKEVIRTUAL,
                    "android/view/View",
                    "setContentDescription",
                    "(Ljava/lang/CharSequence;)V",
                    false)
        }
        // 使用了StatisticsView注解，覆盖已有tag
        if (this.view.data != null) {
            // view.setTag(this.view.data);
            mv.visitVarInsn(ALOAD, viewParamIndex)
            mv.visitLdcInsn(Pattern.matches('\\{.*}', this.view.data) ? this.view.data : '{}')
            mv.visitMethodInsn(INVOKEVIRTUAL,
                    "android/view/View",
                    "setTag",
                    "(Ljava/lang/Object;)V",
                    false)
        }
        if (otherParams != null) {
            otherParams.each { otherParam ->
                // view.setTag(otherParam.tagKey, otherParam.tagValueConvertedOwner.replace('/', '.').valueOf(xxx));
                mv.visitVarInsn(ALOAD, viewParamIndex)
                mv.visitLdcInsn(new Integer(otherParam.tagKey))
                mv.visitVarInsn(otherParam.opcode, otherParam.index)
                if (otherParam.tagValueDescriptor != null && otherParam.tagValueConvertedOwner != null) {
                    mv.visitMethodInsn(INVOKESTATIC,
                            otherParam.tagValueConvertedOwner,
                            "valueOf",
                            "(${otherParam.tagValueDescriptor})L${otherParam.tagValueConvertedOwner};",
                            false)
                }
                mv.visitMethodInsn(INVOKEVIRTUAL,
                        "android/widget/TextView",
                        "setTag",
                        "(ILjava/lang/Object;)V",
                        false)
            }
        }
        // com.levislv.statisticssdk.plugin.StatisticsViewHelper#${methodName}(String, int, String, View);
        mv.visitLdcInsn(this.cv.page.pkgName)
        mv.visitLdcInsn(new Integer(this.cv.page.id))
        mv.visitLdcInsn(this.cv.page.name)
        mv.visitVarInsn(ALOAD, 1)
        mv.visitMethodInsn(INVOKESTATIC,
                StatisticsConsts.METHOD_VISITOR_OWNER_STATISTICS_VIEW_HELPER,
                methodName,
                "(Ljava/lang/String;ILjava/lang/String;Landroid/view/View;)V",
                false)
    }

    /**
     * 是否为lambda表达式
     *
     * @param methodLambdaName
     * @param descriptor
     * @return
     */
    private boolean isLambdaExpression(String methodLambdaName, String descriptor) {
        if ((this.access & ACC_SYNTHETIC) != 0
                && (this.access & ACC_PRIVATE) != 0
                && (this.access & ACC_STATIC) != 0
                && this.name.startsWith('lambda$')
                && this.name.equals(methodLambdaName)
                && this.descriptor.equals(descriptor)
                && this.signature == null
                && this.exceptions == null) {
            return true
        }
        return false
    }

    /**
     * 进入view的各种回调方法(lambda表达式)插桩
     *
     * @param methodName 方法名
     * @param viewParamIndex view是第几个参数，从0开始索引
     * @param params 参数数组
     */
    private void onViewHandledMethodLambdaEnter(String methodName, int viewParamIndex, StatisticsMethodParam[] otherParams) {
        // 使用了StatisticsView注解，覆盖已有contentDescription
        if (this.view.pkgName != null && this.view.name != null) {
            // view.setContentDescription(this.view.name);
            mv.visitVarInsn(ALOAD, viewParamIndex)
            mv.visitLdcInsn(this.view.name == '' ? 'null' : this.view.name)
            mv.visitMethodInsn(INVOKEVIRTUAL,
                    "android/view/View",
                    "setContentDescription",
                    "(Ljava/lang/CharSequence;)V",
                    false)
        }
        // 使用了StatisticsView注解，覆盖已有tag
        if (this.view.data != null) {
            // view.setTag(this.view.data);
            mv.visitVarInsn(ALOAD, viewParamIndex)
            mv.visitLdcInsn(Pattern.matches('\\{.*}', this.view.data) ? this.view.data : '{}')
            mv.visitMethodInsn(INVOKEVIRTUAL,
                    "android/view/View",
                    "setTag",
                    "(Ljava/lang/Object;)V",
                    false)
        }
        if (otherParams != null) {
            otherParams.each { otherParam ->
                // view.setTag(otherParam.tagKey, otherParam.tagValueConvertedOwner.replace('/', '.').valueOf(xxx));
                mv.visitVarInsn(ALOAD, viewParamIndex)
                mv.visitLdcInsn(new Integer(otherParam.tagKey))
                mv.visitVarInsn(otherParam.opcode, otherParam.index)
                if (otherParam.tagValueDescriptor != null && otherParam.tagValueConvertedOwner != null) {
                    mv.visitMethodInsn(INVOKESTATIC,
                            otherParam.tagValueConvertedOwner,
                            "valueOf",
                            "(${otherParam.tagValueDescriptor})L${otherParam.tagValueConvertedOwner};",
                            false)
                }
                mv.visitMethodInsn(INVOKEVIRTUAL,
                        "android/widget/TextView",
                        "setTag",
                        "(ILjava/lang/Object;)V",
                        false)
            }
        }
        mv.visitLdcInsn(this.cv.page.pkgName)
        mv.visitLdcInsn(new Integer(this.cv.page.id))
        mv.visitLdcInsn(this.cv.page.name)
        mv.visitVarInsn(ALOAD, viewParamIndex)
        mv.visitMethodInsn(INVOKESTATIC,
                StatisticsConsts.METHOD_VISITOR_OWNER_STATISTICS_VIEW_HELPER,
                methodName,
                "(Ljava/lang/String;ILjava/lang/String;Landroid/view/View;)V",
                false)
    }
}
