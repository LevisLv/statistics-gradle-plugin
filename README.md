## Statistics Gradle Plugin(Android全埋点插件)
![](https://jitpack.io/v/LevisLv/statistics-gradle-plugin.svg)
============

* 该插件需要配合 [统计SDK](https://github.com/LevisLv/statistics-sdk) 使用，且两者版本号需一致。
* 统计SDK 内部使用 [Countly SDK](https://github.com/Countly/countly-sdk-android) 实现，所以版本号与之一致，你也可改用其他方式。

## 一、说明
--------

该插件在class转dex的前一步对其进行插桩，在特定切入点进行代码嵌入，从而满足统一埋点的需求。

### 1、该插件统计的事件包括：
#### ① 应用（Application）进入、退出、前台、后台
#### 事件名为：
* common_AppEnter、common_AppExit
* common_AppToFg、common_AppToBg
#### ② 页面（android.app.Activity和android.support.v4.app.Fragment）的进入、退出
#### 事件名为：
* common_PageEnter、common_PageExit
#### ③ 控件（View及其子类）的操作
#### 事件名为：
* common_ViewClick、common_ViewLongClick、common_ViewTouch、common_ViewFocusChange
* common_TextViewEditorAction
* common_CompoundButtonCheckedChanged
* common_RadioGroupCheckedChanged
* common_SeekBarProgressChanged、common_SeekBarStartTrackingTouch、common_SeekBarStopTrackingTouch
* common_RatingBarRatingChanged
* common_AdapterViewItemClick、common_AdapterViewItemLongClick、common_AdapterViewItemSelected
* common_ExpandableListViewGroupClick、common_ExpandableListViewChildClick
#### 监听的回调如下：
* View.OnClickListener#onClick(View)及其lambda表达式<font color='red'>（单击）</font>
* View.OnLongClickListener#onLongClick(View)及其lambda表达式<font color='red'>（长按）</font>
* View.OnTouchListener#onTouch(View, MotionEvent)及其lambda表达式<font color='red'>（触摸，无特殊需求不加）</font>
* View.OnFocusChangeListener#onFocusChange(View, boolean)及其lambda表达式<font color='red'>（焦点状态改变）</font>
* TextView.OnEditorActionListener#onEditorAction(TextView, int, KeyEvent)及其lambda表达式<font color='red'>（输入框键盘回车键的动作）</font>
* CompoundButton.OnCheckedChangeListener#onCheckedChanged(CompoundButton, boolean)及其lambda表达式<font color='red'>（开关或复选框选中状态改变）</font>
* RadioGroup.OnCheckedChangeListener#onCheckedChanged(RadioGroup, int)及其lambda表达式<font color='red'>（单选框选择某一项）</font>
* SeekBar.OnSeekBarChangeListener#onProgressChanged(SeekBar, int, boolean)<font color='red'>（进度条进度改变）</font>
* SeekBar.OnSeekBarChangeListener#onStartTrackingTouch(SeekBar)<font color='red'>（进度条开始滑动）</font>
* SeekBar.OnSeekBarChangeListener#onStopTrackingTouch(SeekBar)<font color='red'>（进度条结束滑动）</font>
* RatingBar.OnRatingBarChangeListener#onRatingChanged(RatingBar, float, boolean)及其lambda表达式<font color='red'>（星级评分条值改变）</font>
* AdapterView.OnItemClickListener#onItemClick(AdapterView<?>, View, int, long)及其lambda表达式<font color='red'>（列表某一项单击）</font>
* AdapterView.OnItemLongClickListener#onItemLongClick(AdapterView<?>, View, int, long)及其lambda表达式<font color='red'>（列表某一项长按）</font>
* AdapterView.OnItemSelectedListener#onItemSelected(AdapterView<?>, View, int, long)<font color='red'>（列表某一项选中）</font>
* ExpandableListView.OnGroupClickListener#onGroupClick(ExpandableListView, View, int, long)及其lambda表达式<font color='red'>（可扩展列表某一组单击）</font>
* ExpandableListView.OnChildClickListener#onChildClick(ExpandableListView, View, int, int, long)及其lambda表达式<font color='red'>（可扩展列表某一组下的某一项单击）</font>

以下使用ButterKnife注解修饰的方法，其返回类型、参数个数、类型、顺序必须遵循以下规则
* @butterknife.OnClick(R.id.xxx) void onClick(View)
* @butterknife.OnLongClick(R.id.xxx) boolean onLongClick(View)
* @butterknife.OnTouch(R.id.xxx) boolean onTouch(View, MotionEvent)
* @butterknife.OnFocusChange(R.id.xxx) void onFocusChange(View, boolean)
* @butterknife.OnEditorAction(R.id.xxx) boolean onEditorAction(TextView, int, KeyEvent)
* @butterknife.OnCheckedChanged(R.id.xxx) void onCheckedChanged(CompoundButton, boolean)
* @butterknife.OnItemClick(R.id.xxx) void onItemClick(AdapterView<?>, View, int, long)
* @butterknife.OnItemLongClick(R.id.xxx) boolean onItemLongClick(AdapterView<?>, View, int, long)
* @butterknife.OnItemSelected(value = R.id.xxx, callback = OnItemSelected.Callback.ITEM_SELECTED) void onItemSelected(AdapterView<?>, View, int, long)

### 2、热力图统计
#### 事件名为：
* common_HeatMap

### 3、查看插桩后的class文件
查看 build/intermediates/transforms/statistics 目录

## 二、配置
--------

### 1、添加 maven 地址及 classpath（build.gradle in project）
```groovy
buildscript {
    repositories {
        ······
        maven { url 'https://www.jitpack.io' }
    }

    dependencies {
        ······
        // 要求gradle插件版本最低3.1.0
        classpath 'com.github.LevisLv:statistics-gradle-plugin:19.02.3'
    }
}
```

### 2、引用插件并添加依赖（build.gradle in every module）
<font color='red'>所有的Android Application和Android Library模块都需要加如下配置</font>

```groovy
······
apply plugin: 'com.levislv.statistics'

statistics {
    enableCompileLog false // 是否打印编译日志（默认false）
    enableHeatMap true // 是否开启热力图功能（默认true）
    enableViewOnTouch false // 是否允许view的onTouch回调全埋点（默认false）
}

android {
    ······
    // 全埋点插件需要 Java 8
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    ······
    implementation 'com.github.LevisLv:statistics-sdk:19.02.3'
}
```

## 三、使用说明（必须遵循使用规则）
--------

### 1、必须在 Application 中初始化 Statistics：
```java
Statistics.sharedInstance().init(context, serverUrl, appKey);
```

### 2、添加页面注解：
#### 注：data属性对应的json文本对应的各个key保留值如下（切勿使用）：
<font color='red'>startup_type、is_first_enter、from_last_enter_time、page_exposure_type、last_page_id、last_page_name、page_id、page_name、page_type</font>
* 在每个具有对控件进行操作的类添加如下注解：
```
@StatisticsPage(
        type = StatisticsPage.Type.xxx, // 必填，如果该类是实现具体业务逻辑的类，继承自android.app.Activity则申明为ACTIVITY，继承自android.support.v4.app.Fragment则申明为FRAGMENT
        id = R.layout.xxx, // 必填，页面layout id（子模块使用StatisticsR.layout.xxx）
        name = "xxx", // 必填，页面名称（建议填写中文，例如：首页、设置页、关于页）
        data = "{'x':'x', 'xx':'xx'}" // 选填，页面其他数据，必须遵循json规范，key、value均为String类型
)
```

### 3、添加控件信息，以下三种方式等价：
#### 注：data属性、setTag参数、android:tag对应的json文本对应的各个key保留值如下（切勿使用）：
<font color='red'>motion_event、has_focus、action_id、key_event、is_checked、checked_id、progress、from_user、rating、parent_id、parent_name、item_position、item_id、item_group_position、item_child_position、page_id、page_name、id、name、type、location、text</font>
* 1、在每个对控件设置回调的方法添加如下注解，这种方式优先级最高，会覆盖其他方式：
```
@StatisticsView(
        parentName = "xxx", // 选填，父控件名称，目前只给onItemClick、onItemLongClick、onItemSelected、onGroupClick、onChildClick这几个方法使用
        name = "xxx", // 必填，控件名称（建议填写中文，例如：登录按钮、账号输入框）
        data = "{'x':'x', 'xx':'xx'}" // 选填，控件其他数据，必须遵循json规范，key、value均为String类型
)
```
或
* 2、在回调触发之前手动设置parentName、name和data，例如：
```
parent.setContentDescription("xxx"); // 设置parentName，parent即为onItemClick、onItemLongClick、onItemSelected、onGroupClick、onChildClick这几个方法的parent参数
view.setContentDescription("xxx"); // 设置name
view.setTag("{'x':'x', 'xx':'xx'}"); // 设置data，也可以是org.json.JSONObject类型
```
或
* 3、在xml布局文件中申明name和data
```
android:contentDescription="xxx" // 设置name
android:tag="{'x':'x', 'xx':'xx'}" // 设置data
```