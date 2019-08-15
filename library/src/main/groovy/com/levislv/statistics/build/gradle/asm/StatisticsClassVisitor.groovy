package com.levislv.statistics.build.gradle.asm

import com.levislv.statistics.build.gradle.bean.StatisticsPage
import com.levislv.statistics.build.gradle.constant.StatisticsConsts
import com.levislv.statistics.build.gradle.runtime.StatisticsConfig
import com.levislv.statistics.build.gradle.util.StatisticsLog
import com.levislv.statistics.build.gradle.util.StatisticsUtils
import org.objectweb.asm.*

import java.util.regex.Pattern

/**
 * @author levislv
 * @email  levislv@levislv.com
 * @blog   https://blog.levislv.com/
 * @github https://github.com/levislv/
 */
class StatisticsClassVisitor extends ClassVisitor implements Opcodes {

    String pkgName
    ClassWriter cw

    int version
    int access
    String name
    String signature
    String superName
    String[] interfaces

    StatisticsPage page
    String owner

    boolean onActivityCreateMissing = true
    boolean onActivityStartMissing = true
    boolean onActivityResumeMissing = true
    boolean onActivityPauseMissing = true
    boolean onActivityStopMissing = true
    boolean onActivityDestroyMissing = true
    boolean dispatchTouchEventMissing = true

    boolean onFragmentViewCreatedMissing = true
    boolean onFragmentResumeMissing = true
    boolean onFragmentPauseMissing = true
    boolean onFragmentDestroyMissing = true
    boolean setUserVisibleHintMissing = true
    boolean onHiddenChangedMissing = true

    String onClickLambdaName
    String onLongClickLambdaName
    String onTouchLambdaName
    String onFocusChangeLambdaName
    String onEditorActionLambdaName
    String onCompoundButtonCheckedChangedLambdaName
    String onRadioGroupCheckedChangedLambdaName
    String onRatingChangedLambdaName
    String onItemClickLambdaName
    String onItemLongClickLambdaName
    String onGroupClickLambdaName
    String onChildClickLambdaName

    StatisticsClassVisitor(final ClassVisitor classVisitor, final String pkgName) {
        super(Opcodes.ASM7, classVisitor)
        this.pkgName = pkgName
        this.cw = (ClassWriter) classVisitor
        this.page = new StatisticsPage()
    }

    /**
     * 该方法是当扫描类时第一个访问的方法
     *
     * @param version JDK版本
     *  比如51，代表JDK版本1.7。 各个JDK版本对应的数值如下：
     *      JDK版本      int数值
     *      J2SE 8      52
     *      J2SE 7      51
     *      J2SE 6.0    50
     *      J2SE 5.0    49
     *      JDK 1.4     48
     *      JDK 1.3     47
     *      JDK 1.2     46
     *      JDK 1.1     45
     * @param access 类的修饰符
     *  修饰符在ASM中是以"ACC_"开头的常量，可以作用到类级别上的修饰符有：
     *      修饰符           含义
     *      ACC_PUBLIC      public
     *      ACC_PRIVATE     private
     *      ACC_PROTECTED   protected
     *      ACC_FINAL       final
     *      ACC_SUPER       extends
     *      ACC_INTERFACE   接口
     *      ACC_ABSTRACT    抽象类
     *      ACC_ANNOTATION  注解类型
     *      ACC_ENUM        枚举类型
     *      ACC_DEPRECATED  标记了@Deprecated注解的类
     *      ACC_SYNTHETIC   javac生成
     * @param name 类的名称
     *  通常我们会使用完整的包名+类名来表示类，比如:a.b.c.MyClass，但是在字节码中是以路径的形式表示，
     *  即: a/b/c/MyClass
     *  值得注意的是，虽然是路径表示法但是不需要写明类的".class"扩展名。
     * @param signature 泛型信息
     *  如果类并未定义任何泛型该参数为null
     * @param superName 所继承的父类
     *  由于Java的类是单根结构，即所有类都继承自java.lang.Object，
     *  因此可以简单的理解为任何类都会具有一个父类。
     *  虽然在编写Java程序时我们没有去写extends关键字去明确继承的父类，
     *  但是JDK在编译时总会为我们加上"extends Object"。
     * @param interfaces 类实现的接口
     *  在Java中，类是可以实现多个不同的接口，因此该参数是一个数组。
     */
    @Override
    void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        StatisticsLog.info("||---开始扫描类: version=${version}, access=${StatisticsUtils.accCode2String(access)}, name=${name}, signature=${signature}, superName=${superName}, interfaces=${interfaces.toArrayString()}")

        super.visit(version, access, name, signature, superName, interfaces)
        this.version = version
        this.access = access
        this.name = name
        this.signature = signature
        this.superName = superName
        this.interfaces = interfaces
    }

    @Override
    void visitOuterClass(String owner, String name, String descriptor) {
        StatisticsLog.info("||---visitOuterClass：owner=${owner}, name=${name}, descriptor=${descriptor}")

        super.visitOuterClass(owner, name, descriptor)

        // 只有匿名内部类会走该回调方法
        this.owner = owner
    }

    @Override
    void visitInnerClass(String name, String outerName, String innerName, int access) {
        StatisticsLog.info("||---visitInnerClass：name=${name}, outerName=${outerName}, innerName=${innerName}, access=${StatisticsUtils.accCode2String(access)}")

        super.visitInnerClass(name, outerName, innerName, access)
        if (name.equals(this.name)) {
            this.page.isActivity = false
            this.page.isFragment = false

            String className
            if (this.owner == null) { // 非匿名类
                className = outerName
            } else { // 匿名类
                className = this.owner
            }
            StatisticsClassVisitor ownerStatisticsClassVisitor = StatisticsConfig.statisticsClassVisitorMap.get(className)
            if (ownerStatisticsClassVisitor != null) {
                if (!StatisticsConfig.statisticsClassVisitorMap.containsKey(this.name)) {
                    StatisticsConfig.statisticsClassVisitorMap.put(this.name, this)
                }
                this.page.pkgName = ownerStatisticsClassVisitor.page.pkgName
                this.page.id = ownerStatisticsClassVisitor.page.id
                this.page.name = ownerStatisticsClassVisitor.page.name
                this.page.data = ownerStatisticsClassVisitor.page.data
            }
        }
    }

    /**
     * 当扫描器扫描到类注解声明时进行调用
     *
     * @param descriptor 注解的类型
     *  它使用的是("L"+"类型路径"+";")形式表述
     *  例如：Lcom/levislv/statisticsannotation/StatisticsClassAnnotation;
     * @param visible 该注解是否在JVM中可见
     *  1.RetentionPolicy.SOURCE：声明注解只保留在Java源程序中，在编译Java类时注解信息不会被写入到Class。如果使用的是这个配置ASM也将无法探测到这个注解。
     *  2.RetentionPolicy.CLASS：声明注解仅保留在Class文件中，JVM运行时并不会处理它，这意味着ASM可以在visitAnnotation时候探测到它，但是通过Class反射无法获取到注解信息。
     *  3.RetentionPolicy.RUNTIME：这是最常用的一种声明，ASM可以探测到这个注解，同时Java反射也可以取得注解的信息。所有用到反射获取的注解都会用到这个配置，就是这个原因。
     * @return
     */
    @Override
    AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        AnnotationVisitor av = super.visitAnnotation(descriptor, visible)
        return av == null ? null : new StatisticsClassAnnotationVisitor(this, av, descriptor, visible)
    }

    /**
     * 当扫描器扫描到类的方法时进行调用
     *
     * @param access 方法的修饰符
     *  可以作用到方法级别上的修饰符有:
     *      修饰符               含义
     *      ACC_PUBLIC          public
     *      ACC_PRIVATE         private
     *      ACC_PROTECTED       protected
     *      ACC_STATIC          static
     *      ACC_FINAL           final
     *      ACC_SYNCHRONIZED    同步的
     *      ACC_VARARGS         不定参数个数的方法
     *      ACC_NATIVE          native类型方法
     *      ACC_ABSTRACT        抽象的方法
     *      ACC_DEPRECATED      标记了@Deprecated注解的类
     *      ACC_SYNTHETIC       javac生成
     * @param name 方法名
     * @param descriptor 方法签名
     *  方法签名的格式如下："(参数列表)返回值类型"。在ASM中不同的类型对应不同的代码：
     *      代码     类型
     *      I       int
     *      B       byte
     *      C       char
     *      D       double
     *      F       float
     *      J       long
     *      S       short
     *      Z       boolean
     *      V       void
     *      [...;   数组
     *      [[...;  二维数组
     *      [[[...; 三维数组
     *  下面举几个方法参数列表对应的方法签名示例：
     *      参数列表                                 方法参数
     *      String[]                                [Ljava/lang/String;
     *      String[][]                              [[Ljava/lang/String;
     *      int, String, String[]                   ILjava/lang/String;[Ljava/lang/String;
     *      int, boolean, long, String[], double    IZJ[Ljava/lang/String;D
     *      Class<?>, String, Object... paramType   Ljava/lang/Class;Ljava/lang/String;[Ljava/lang/Object;
     *      int[]                                   [I
     * @param signature 泛型相关的信息
     * @param exceptions 将会抛出的异常
     *  如果方法不会抛出异常，该参数为null
     * @return
     */
    @Override
    MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions)
        return mv == null ? null : new StatisticsMethodVisitor(this, mv, access, name, descriptor, signature, exceptions)
    }

    @Override
    void visitEnd() {
        StatisticsLog.info("||---结束扫描类：${this.name}")

        if (this.page.isActivity) {
            if (this.onActivityCreateMissing) {
                addActivityLifecycleCallbackExitMethod('onCreate')
            }
            if (this.onActivityStartMissing) {
                addActivityLifecycleCallbackExitMethod('onStart')
            }
            if (this.onActivityResumeMissing) {
                addActivityLifecycleCallbackExitMethod('onResume')
            }
            if (this.onActivityPauseMissing) {
                addActivityLifecycleCallbackEnterMethod('onPause')
            }
            if (this.onActivityStopMissing) {
                addActivityLifecycleCallbackEnterMethod('onStop')
            }
            if (this.onActivityDestroyMissing) {
                addActivityLifecycleCallbackEnterMethod('onDestroy')
            }
            if (this.dispatchTouchEventMissing
                    && StatisticsConfig.enableHeatMap()) {
                addDispatchTouchEventEnterMethod()
            }
        } else if (this.page.isFragment) {
            if (this.onFragmentViewCreatedMissing) {
                addFragmentLifecycleCallbackExitMethod('onViewCreated')
            }
            if (this.onFragmentResumeMissing) {
                addFragmentLifecycleCallbackExitMethod('onResume')
            }
            if (this.onFragmentPauseMissing) {
                addFragmentLifecycleCallbackEnterMethod('onPause')
            }
            if (this.onFragmentDestroyMissing) {
                addFragmentLifecycleCallbackEnterMethod('onDestroy')
            }
            if (this.setUserVisibleHintMissing) {
                addFragmentLifecycleCallbackExitMethod('setUserVisibleHint')
            }
            if (this.onHiddenChangedMissing) {
                addFragmentLifecycleCallbackExitMethod('onHiddenChanged')
            }
        }

        super.visitEnd()
    }

    /**
     * 添加Activity生命周期回调进入方法
     *
     * @param methodName
     */
    private void addActivityLifecycleCallbackEnterMethod(String methodName) {
        if (methodName == null) {
            return
        }

        MethodVisitor mv = cw.visitMethod(ACC_PROTECTED,
                methodName,
                "()V",
                null,
                null)
        mv.visitCode()

        mv.visitVarInsn(ALOAD, 0)
        mv.visitLdcInsn(this.page.pkgName)
        mv.visitLdcInsn(new Integer(this.page.id))
        mv.visitLdcInsn(this.page.name)
        mv.visitLdcInsn(Pattern.matches('\\{.*}', this.page.data) ? this.page.data : '{}')
        mv.visitMethodInsn(INVOKESTATIC,
                StatisticsConsts.METHOD_VISITOR_OWNER_STATISTICS_ACTIVITY_HELPER,
                methodName,
                "(${StatisticsConsts.CLASS_VISITOR_DESCRIPTOR_ACTIVITY}Ljava/lang/String;ILjava/lang/String;Ljava/lang/String;)V",
                false)

        mv.visitVarInsn(ALOAD, 0)
        mv.visitMethodInsn(INVOKESPECIAL,
                this.superName,
                methodName,
                "()V",
                false)

        mv.visitInsn(RETURN)
        mv.visitMaxs(5, 1)
        mv.visitEnd()
    }

    /**
     * 添加Activity生命周期回调退出方法
     *
     * @param methodName
     */
    private void addActivityLifecycleCallbackExitMethod(String methodName) {
        if (methodName == null) {
            return
        }

        String descriptor = '()V'
        int maxLocals = 1
        if (methodName.startsWith('on')) {
            if ('onCreate'.equals(methodName)) {
                descriptor = '(Landroid/os/Bundle;)V'
                maxLocals = 2
            }
        }

        MethodVisitor mv = cw.visitMethod(ACC_PROTECTED,
                methodName,
                descriptor,
                null,
                null)
        mv.visitCode()

        if (methodName.startsWith('on')) {
            if ('onCreate'.equals(methodName)) {
                mv.visitVarInsn(ALOAD, 0)
                mv.visitVarInsn(ALOAD, 1)
            } else {
                mv.visitVarInsn(ALOAD, 0)
            }
        }
        mv.visitMethodInsn(INVOKESPECIAL,
                this.superName,
                methodName,
                descriptor,
                false)

        mv.visitVarInsn(ALOAD, 0)
        mv.visitLdcInsn(this.page.pkgName)
        mv.visitLdcInsn(new Integer(this.page.id))
        mv.visitLdcInsn(this.page.name)
        mv.visitLdcInsn(Pattern.matches('\\{.*}', this.page.data) ? this.page.data : '{}')
        mv.visitMethodInsn(INVOKESTATIC,
                StatisticsConsts.METHOD_VISITOR_OWNER_STATISTICS_ACTIVITY_HELPER,
                methodName,
                "(${StatisticsConsts.CLASS_VISITOR_DESCRIPTOR_ACTIVITY}Ljava/lang/String;ILjava/lang/String;Ljava/lang/String;)V",
                false)

        mv.visitInsn(RETURN)
        mv.visitMaxs(5, maxLocals)
        mv.visitEnd()
    }

    /**
     * 添加Fragment dispatchTouchEvent进入方法
     */
    private void addDispatchTouchEventEnterMethod() {
        String methodName = 'dispatchTouchEvent'

        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC,
                methodName,
                "(Landroid/view/MotionEvent;)Z",
                null,
                null)
        mv.visitCode()
        mv.visitVarInsn(ALOAD, 0)
        mv.visitLdcInsn(this.page.pkgName)
        mv.visitVarInsn(ALOAD, 1)
        mv.visitMethodInsn(INVOKESTATIC,
                StatisticsConsts.METHOD_VISITOR_OWNER_STATISTICS_TOUCH_HELPER,
                methodName,
                "(${StatisticsConsts.CLASS_VISITOR_DESCRIPTOR_ACTIVITY}Ljava/lang/String;Landroid/view/MotionEvent;)V",
                false)
        mv.visitVarInsn(ALOAD, 0)
        mv.visitVarInsn(ALOAD, 1)
        mv.visitMethodInsn(INVOKESPECIAL,
                this.superName,
                methodName,
                "(Landroid/view/MotionEvent;)Z",
                false)
        mv.visitInsn(IRETURN)
        mv.visitMaxs(3, 2)
        mv.visitEnd()
    }

    /**
     * 添加Fragment生命周期回调进入方法
     *
     * @param methodName
     */
    private void addFragmentLifecycleCallbackEnterMethod(String methodName) {
        if (methodName == null) {
            return
        }

        MethodVisitor mv = cw.visitMethod(ACC_PROTECTED,
                methodName,
                "()V",
                null,
                null)
        mv.visitCode()

        mv.visitVarInsn(ALOAD, 0)
        mv.visitLdcInsn(this.page.pkgName)
        mv.visitLdcInsn(new Integer(this.page.id))
        mv.visitLdcInsn(this.page.name)
        mv.visitLdcInsn(Pattern.matches('\\{.*}', this.page.data) ? this.page.data : '{}')
        mv.visitMethodInsn(INVOKESTATIC,
                StatisticsConsts.METHOD_VISITOR_OWNER_STATISTICS_FRAGMENT_HELPER,
                methodName,
                "(${StatisticsConsts.CLASS_VISITOR_DESCRIPTOR_FRAGMENT}Ljava/lang/String;ILjava/lang/String;Ljava/lang/String;)V",
                false)

        mv.visitVarInsn(ALOAD, 0)
        mv.visitMethodInsn(INVOKESPECIAL,
                this.superName,
                methodName,
                "()V",
                false)

        mv.visitInsn(RETURN)
        mv.visitMaxs(4, 1)
        mv.visitEnd()
    }

    /**
     * 添加Fragment生命周期回调退出方法
     *
     * @param methodName
     */
    private void addFragmentLifecycleCallbackExitMethod(String methodName) {
        if (methodName == null) {
            return
        }

        String descriptor = '()V'
        int maxLocals = 1
        if (methodName.startsWith('on')) {
            if ('onViewCreated'.equals(methodName)) {
                descriptor = '(Landroid/view/View;Landroid/os/Bundle;)V'
                maxLocals = 3
            } else if ('onHiddenChanged'.equals(methodName)) {
                descriptor = '(Z)V'
                maxLocals = 2
            }
        } else {
            descriptor = '(Z)V'
            maxLocals = 2
        }

        MethodVisitor mv = cw.visitMethod(ACC_PROTECTED,
                methodName,
                descriptor,
                null,
                null)
        mv.visitCode()

        if (methodName.startsWith('on')) {
            if ('onViewCreated'.equals(methodName)) {
                mv.visitVarInsn(ALOAD, 0)
                mv.visitVarInsn(ALOAD, 1)
                mv.visitVarInsn(ALOAD, 2)
            } else if ('onHiddenChanged'.equals(methodName)) {
                mv.visitVarInsn(ALOAD, 0)
                mv.visitVarInsn(ILOAD, 1)
            } else {
                mv.visitVarInsn(ALOAD, 0)
            }
        } else {
            mv.visitVarInsn(ALOAD, 0)
            mv.visitVarInsn(ILOAD, 1)
        }
        mv.visitMethodInsn(INVOKESPECIAL,
                this.superName,
                methodName,
                descriptor,
                false)

        mv.visitVarInsn(ALOAD, 0)
        mv.visitLdcInsn(this.page.pkgName)
        mv.visitLdcInsn(new Integer(this.page.id))
        mv.visitLdcInsn(this.page.name)
        mv.visitLdcInsn(Pattern.matches('\\{.*}', this.page.data) ? this.page.data : '{}')
        mv.visitMethodInsn(INVOKESTATIC,
                StatisticsConsts.METHOD_VISITOR_OWNER_STATISTICS_FRAGMENT_HELPER,
                methodName,
                "(${StatisticsConsts.CLASS_VISITOR_DESCRIPTOR_FRAGMENT}Ljava/lang/String;ILjava/lang/String;Ljava/lang/String;)V",
                false)

        mv.visitInsn(RETURN)
        mv.visitMaxs(4, maxLocals)
        mv.visitEnd()
    }
}
