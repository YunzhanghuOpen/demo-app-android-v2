# 融云

![Mou icon](http://www.rongcloud.cn/images/logo_1.png)


##融云 Demo 2.0 运行方式

###Eclipse 环境

#####1. 下载：
  下载融云 demo 2.0 到自己电脑。
#####2. 导入项目：
 操作步骤：打开 Eclipse 选择 file—>import—>General—>Existing Projects into WorkSpace
     选择融云 demo 目录，勾选 RongDemo、RongIMKit、android-support-v7-appcompat 项目，然后点finish按钮完成项目导入。
#####3. 设置Java Complier:
设置 JAVA 编译版本 jdk 1.7 以上。<br/>
操作步骤：在 Eclipse 中分别右击  RongDemo、RongIMKit、android-support-v7-appcompat 项目 在菜单中选择 Properties->java Compiler->compiler complinace level 选择 1.7 以上 JDK 版本。
 
#####4. 完成导入 build apk。
<font color="#0069d6">注：如果 clean 项目后发现还是不能正常运行，找到 Eclipse 下的 Problems 标签删除红色提示后方就正常运行。</font>


###Android studio 环境
#####1. 下载
 下载融云 demo 2.0 到自己电脑。
#####2. 导入项目
打开 Android studio 选择 open an existing Android Studio project 导入项目。
#####3. 删除引用
为了更方便的 Eclipse 开发者我们引用了 appcompat-v7 包，Android studio 开发者需要做两步操作：<BR/>
 &nbsp;&nbsp;&nbsp;1. 删除 settings.gradle 中的 " appcompat-v7 "。 <BR/>
 &nbsp;&nbsp;&nbsp;2. 删除 appcompat-v7 Module 。
#####4. 完成导入 build apk。


<BR/><BR/>
##代码结构

####App 类：
程序入口，做 RongIM.init 操作。

####DemoApi 类：
Demo 网络请求类

####DemoContext 类：
Demo 缓存类

####RongCloudEvent 类：
融云事件监听类

####database 包：
Demo 数据缓存

####message 包:
如何使用融云自定义消息，以及注册模板。
#####1，ContactsProvider 会话扩展功能自定义
#####2，AgreedFriendRequestMessage 演示如何自定义消息
#####3，ContactNotificationMessageProvider 如何自定义消息模板
#####4，NewDiscussionConversationProvider 讨论组 @ 消息展示

####model 包：
Demo model 模块

####parser 包：
Demo 使用的是 gson 作为解析工具，这个包下是对 json 的数据解析

####utils 包：
一些 Demo 中用到的 工具类

####ui 包：
包含了 Activity 、Fragment 以及 widget


<BR/><BR/>
##Demo 接口文档


<BR/><BR/>
#### 联系我们
商务合作
Email：<bd@rongcloud.cn>

新浪微博 [@融云RongCloud](http://weibo.com/rongcloud)

客服 QQ 2948214065

公众帐号
融云RongCloud RongCloud 公众账号二维码

![Smaller icon](http://www.rongcloud.cn/images/code1.png "RongCloud")
